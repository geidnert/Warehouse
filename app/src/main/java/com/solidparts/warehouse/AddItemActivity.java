package com.solidparts.warehouse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.solidparts.warehouse.dao.IItemDAO;
import com.solidparts.warehouse.dao.OnlineItemDAO;
import com.solidparts.warehouse.dto.ItemDTO;
import com.solidparts.warehouse.service.ItemService;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class AddItemActivity extends ActionBarActivity {

    public static final int CAMERA_REQUEST = 1;
    public static final int IMAGE_GALLERY_REQUEST = 2;

    private ImageView itemImage;
    private ItemService itemService;
    private Bitmap itemImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        itemImage = ((ImageView)findViewById(R.id.itemImage));
        itemService = new ItemService(this);

        //ItemSearchTask itemSearchTask = new ItemSearchTask();

        //itemSearchTask.execute("motor");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSave(View view){
        //String name = ((EditText)findViewById(R.id.name)).getText().toString();
        //String description = ((EditText)findViewById(R.id.description)).getText().toString();
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setGuid(2);
        itemDTO.setName(((EditText) findViewById(R.id.name)).getText().toString());
        itemDTO.setDescription(((EditText) findViewById(R.id.description)).getText().toString());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        itemImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] img = bos.toByteArray();

        itemDTO.setImage(img);
        itemDTO.setQrCode("uhde782ihdjksy78d9i3hu2dkwhde9si8yufhjeikuywsdf");

        itemService.addItem(itemDTO);
    }

    public void onAddExistingImage(View view){
        Intent intent = new Intent(Intent.ACTION_PICK);

        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);

        intent.setDataAndType(data, "image/*");

        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
    }

    public void onTakePhoto(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //String pictureName = getPictureName();
        //File imageFile = new File(pictureDirectory, pictureName);
        //Uri pictureUri = Uri.fromFile(imageFile);
        //intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private String getPictureName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());

        return "itemImage" + timestamp + ".jpg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                showImage(image);
            }

            if (requestCode == IMAGE_GALLERY_REQUEST) {
                Uri imageUri = data.getData();
                InputStream inputStream;

                try {
                    inputStream = getContentResolver().openInputStream(imageUri);
                    showImage(itemImageBitmap);
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // ---- Private ---

    private void generateQRCode(){

    }

    private void showImage(Bitmap image){
        itemImageBitmap = image;
        itemImage.setImageBitmap(itemImageBitmap);
    }


    class ItemSearchTask extends AsyncTask<String, Integer, ItemDTO> {

        @Override
        protected ItemDTO doInBackground(String... itemName) {
            // we're only getting one String, so let's access that one string.
            String searchTerm = itemName[0];
            // make a variable that will hold our plant DAO.
            // IPlantDAO plantDAO = new PlantDAOStub();
            IItemDAO itemDAO = new OnlineItemDAO();

            // fetch the plants from the DAO.
            ItemDTO item = null;
            try {
                item = itemDAO.getItem(itemName[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // return the matching plants.
            return item;
        }

        /**
         * This method will be called when doInBackground completes.
         * The paramter result is populated from the return values of doInBackground.
         * This method runs on the UI thread, and therefore can update UI components.
         */
        @Override
        protected void onPostExecute(ItemDTO allItems) {
            // adapt the search results returned from doInBackground so that they can be presented on the UI.
            //ArrayAdapter<ItemDTO> plantAdapter = new ArrayAdapter<Plant>(PlantResultsActivity.this, android.R.layout.simple_list_item_1, allPlants);
            // show the search resuts in the list.
            //setListAdapter(plantAdapter);

            //setProgressBarIndeterminateVisibility(false);
        }

        @Override
        protected void onPreExecute() {
            setProgressBarIndeterminateVisibility(true);
        }
    }
    // https://androidbycode.wordpress.com/2015/02/09/generating-and-displaying-qr-codes-on-android-wear-devices/
}
