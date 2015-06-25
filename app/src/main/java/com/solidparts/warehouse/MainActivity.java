package com.solidparts.warehouse;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;


import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.solidparts.warehouse.dto.DataDTO;
import com.solidparts.warehouse.service.ItemService;

import java.io.File;
import java.io.IOException;


public class MainActivity extends FragmentActivity {
    public static final int APP_VERSION = 3;
    public static final int IMAGE_GALLERY_REQUEST = 1;

    private ItemService itemService;
    MessageManager messageManager;
    RelativeLayout layout;
    private int backgroundClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageManager = new MessageManager();

        itemService = new ItemService(this);

        backgroundClicked = 0;

        //Sync data from local database to online database
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        Uri backgroundUri = Uri.parse(sharedPref.getString("mainBackground", ""));

        if(sharedPref.getString("mainBackground", "") != "" && backgroundUri != null){
            setBackgroundImage(backgroundUri);
        }

        String[] args = new String[]{"syncToOnlineDb"};
        ItemSyncTask itemSyncTask = new ItemSyncTask();
        itemSyncTask.execute(args);

        String[] appArgs = new String[]{};
        AppSyncTask appSyncTask = new AppSyncTask();
        appSyncTask.execute(appArgs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sync) {
            //Sync data from database
            String[] args = new String[]{"fromOnlineDb"};
            ItemSyncTask itemSyncTask = new ItemSyncTask();
            itemSyncTask.execute(args);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddItem(View view) {
        //setContentView(R.layout.activity_add);

        startActivity(new Intent(MainActivity.this, AddItemActivity.class));
    }

    public void onSearch(View view) {
        startActivity(new Intent(MainActivity.this, SearchActivity.class));
    }

    public void onChangeBackground(View view){
        backgroundClicked++;

        if(backgroundClicked == 4) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            String pictureDirectoryPath = pictureDirectory.getPath();
            Uri data = Uri.parse(pictureDirectoryPath);
            intent.setDataAndType(data, "image/*");
            startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
            backgroundClicked = 0;
        }
    }

    // --------------------------------------------------------------------

    class ItemSyncTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... searchTerms) {
            try {
                if (searchTerms[0].equals("fromOnlineDb")) {
                    return itemService.syncFromOnlineDB();
                } else {
                    return itemService.syncToOnlineDB();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return -1;
        }

        /**
         * This method will be called when doInBackground completes.
         * The paramter result is populated from the return values of doInBackground.
         * This method runs on the UI thread, and therefore can update UI components.
         */

        @Override
        protected void onPostExecute(Integer from) {
            if (from == 1)
                messageManager.show(getApplicationContext(), "Items are now synced with the online database.", false);
            //else if (from == -1)
                //messageManager.show(getApplicationContext(),"Items did not sync correctly.", false);
        }


        @Override
        protected void onPreExecute() {
        }
    }

    class AppSyncTask extends AsyncTask<String, DataDTO, DataDTO> {

        @Override
        protected DataDTO doInBackground(String... searchTerms) {
            try {
                return itemService.getAppData();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }


        /**
         * This method will be called when doInBackground completes.
         * The paramter result is populated from the return values of doInBackground.
         * This method runs on the UI thread, and therefore can update UI components.
         */

        @Override
        protected void onPostExecute(DataDTO dataDTO) {
            if (dataDTO != null && APP_VERSION < dataDTO.getLatestAppVersion()){
                UpdateDialogFragment updateDialogFragment = new UpdateDialogFragment();
                updateDialogFragment.show(getFragmentManager(),"updateDialog");
            }
        }


        @Override
        protected void onPreExecute() {
        }
    }

    public static class UpdateDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("New version of Warehouse is available, please download it now!")
                    .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String url = "http://solidparts.se/warehouse/install/warehouse_1_1.apk";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);

                            dialog.dismiss();
                        }

                    })
                    .setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.dismiss();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_GALLERY_REQUEST) {
                try {
                    Uri imageUri = data.getData();
                    setBackgroundImage(imageUri);

                    // save uri to disk
                    SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("mainBackground", imageUri.toString());
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void setBackgroundImage(Uri imageUri){
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
        layout = (RelativeLayout) findViewById(R.id.mainBackground);
        layout.setBackground(bitmapDrawable);
    }
}
