package com.solidparts.warehouse;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

import com.solidparts.warehouse.service.ItemService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


public class AddItem extends ActionBarActivity {

    public static final int CAMERA_REQUEST = 1;
    public static final int IMAGE_GALLERY_REQUEST = 2;

    private ImageView itemImage;
    private ItemService itemService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        itemImage = ((ImageView)findViewById(R.id.itemImage));
        itemService = new ItemService();
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

        itemService.getItem("motor");
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
                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    showImage(image);
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    // ---- Private ---

    private void showImage(Bitmap image){
        itemImage.setImageBitmap(image);
    }
}
