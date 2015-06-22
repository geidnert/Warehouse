package com.solidparts.warehouse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.solidparts.warehouse.dto.ItemDTO;
import com.solidparts.warehouse.service.ItemService;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends Activity {
    public static final int QR_REQUEST = 5;
    static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    public final static String EXTRA_ITEMDTO = "intentItemDTO";
    public final static String EXTRA_SEARCHWORD = "searchWord";

    private Bitmap qrCodeImageBitmap;
    private ImageView qrCodeImage;
    private ItemService itemService;
    private String[] fromActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        itemService = new ItemService(this);
        qrCodeImage = ((ImageView) findViewById(R.id.qrCodeImage));

        fromActivity = getIntent().getStringArrayExtra("fromActivity");

        // TODO -- Fix this to auto search again after a delete
        if (fromActivity != null && fromActivity[0].equals("removedItem")) {
            ((EditText) findViewById(R.id.searchWord)).setText(fromActivity[1]);
            search(new String[]{fromActivity[1], "1"});
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        // }

        return super.onOptionsItemSelected(item);
    }

    public void onScan(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, QR_REQUEST);
        } catch (ActivityNotFoundException anfe) {
            //on catch, show the download dialog
            //showDialog(SearchActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    public void onCancle(View view) {
        startActivity(new Intent(SearchActivity.this, MainActivity.class));
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final ActionBarActivity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return downloadDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == QR_REQUEST) {
                //get the extras that are returned from the intent
                String contents = data.getStringExtra("SCAN_RESULT");
                ((EditText) findViewById(R.id.searchWord)).setText(contents);
                search(new String[]{contents, "1"});
            }
        } else {
            Context context = getApplicationContext();
            CharSequence text = "ERROR: Something went wrong when starting scanning!";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void onSearch(View view) {
        search(new String[]{((EditText) findViewById(R.id.searchWord)).getText().toString(), "1"});
    }

    private void search(String[] args) {
        ItemSearchTask itemSearchTask = new ItemSearchTask();
        itemSearchTask.execute(args);
    }

    class ItemSearchTask extends AsyncTask<String, Integer, List<ItemDTO>> {

        @Override
        protected List<ItemDTO> doInBackground(String... searchTerms) {
            try {
                return itemService.getItems(searchTerms[0], Integer.parseInt(searchTerms[1]));
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
        protected void onPostExecute(final List<ItemDTO> allItems) {
            // adapt the search results returned from doInBackground so that they can be presented on the UI.
            if (allItems != null && allItems.size() > 0) {

                List<String> allItemNames = new ArrayList<>(allItems.size());

                for (ItemDTO itemDAO : allItems) {
                    allItemNames.add(itemDAO.getName());
                }

                ArrayAdapter<String> itemAdaptor = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, allItemNames);
                // show the search resuts in the list.
                //setListAdapter(plantAdapter);

                //setProgressBarIndeterminateVisibility(false);
                //Intent intent = new Intent(SearchActivity.this, AddItemActivity.class);
                //intent.putExtra(EXTRA_ITEMDTO, allItems);
                //startActivity(intent);
                final ListView itemlistView = (ListView) findViewById(R.id.itemlistView);
                //ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this, android.R.layout.simple_list_item_1, android.R.id., allItems);
// Assign adapter to ListView
                itemlistView.setAdapter(itemAdaptor);

                // ListView Item Click Listener
                itemlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {

                        // ListView Clicked item index
                        int itemPosition = position;

                        Intent intent = new Intent(SearchActivity.this, AddItemActivity.class);
                        intent.putExtra(EXTRA_ITEMDTO, allItems.get(position));
                        intent.putExtra(EXTRA_SEARCHWORD, ((EditText) findViewById(R.id.searchWord)).getText().toString());
                        startActivity(intent);

                        // ListView Clicked item value
                        //String itemValue = (String) itemlistView.getItemAtPosition(position);

                        // Show Alert
                        //Toast.makeText(getApplicationContext(),
                        //        "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                        //        .show();

                    }

                });
            } else {
                showMessage("Did not find any matches!", false);
            }
        }

        private void showMessage(String message, boolean goBack) {
            Context context = getApplicationContext();
            CharSequence text = message;
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
            toast.show();
        }

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }
    }
}
