package com.solidparts.warehouse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class AddItemActivity extends ActionBarActivity {

    public static final int CAMERA_REQUEST = 1;
    public static final int IMAGE_GALLERY_REQUEST = 2;
    public static final int QR_REQUEST = 3;

    private ImageView itemImage;
    private ImageView qrCodeImage;
    private ItemService itemService;
    private Bitmap itemImageBitmap;
    private Bitmap qrCodeImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        itemImage = ((ImageView)findViewById(R.id.itemImage));
        qrCodeImage = ((ImageView)findViewById(R.id.qrCodeImage));
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

        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        itemImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos1);
        qrCodeImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos2);
        byte[] itemImg = bos1.toByteArray();
        byte[] qrCodeImg = bos2.toByteArray();

        itemDTO.setImage(itemImg);
        itemDTO.setQrCode(qrCodeImg);

        itemDTO = itemService.addItem(itemDTO);


    }

    public void generateQRCode(View view) {
        new AsyncGenerateQRCode().execute(-1);
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
    static public int MARGIN_AUTOMATIC = -1;
    static public int MARGIN_NONE = 0;

    static public Bitmap generateQRCodeBitmap(@NonNull String contentsToEncode,
                                        int imageWidth, int imageHeight,
                                        int marginSize, int color, int colorBack)
            throws WriterException, IllegalStateException {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Should not be invoked from the UI thread");
        }

        Map<EncodeHintType, Object> hints = null;
        if (marginSize != MARGIN_AUTOMATIC) {
            hints = new EnumMap<>(EncodeHintType.class);
            // We want to generate with a custom margin size
            hints.put(EncodeHintType.MARGIN, marginSize);
        }

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = writer.encode(contentsToEncode, BarcodeFormat.QR_CODE, imageWidth, imageHeight, hints);

        final int width = result.getWidth();
        final int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? color : colorBack;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private void showImage(Bitmap image){
        itemImageBitmap = image;
        itemImage.setImageBitmap(itemImageBitmap);
    }

    private void showQRCodeImage(Bitmap image){
        qrCodeImage.setImageBitmap(itemImageBitmap);
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

    /**
     * AsyncTask to generate QR Code image
     */
    private class AsyncGenerateQRCode extends AsyncTask<Integer, Void, Integer> {

        /**
         * Background thread function to generate image
         *
         * @param params margin to use in creating QR Code
         * @return non zero for success
         *
         * Note that is margin is not in pixels.  See the zxing api for details about the margin
         * for QR code generation
         */
        @Override
        protected Integer doInBackground(Integer... params) {
            if (params.length != 1) {
                throw new IllegalArgumentException("Must pass QR Code margin value as argument");
            }

            try {

                String stringToEncode = ((EditText) findViewById(R.id.name)).getText().toString() +
                        " " +
                        ((EditText) findViewById(R.id.description)).getText().toString();

                final int colorQR = Color.BLACK;
                final int colorBackQR = Color.WHITE;
                final int marginSize = params[0];
                final int width = 400;
                final int height = 400;

                qrCodeImageBitmap = generateQRCodeBitmap(stringToEncode, width, height,
                        marginSize, colorQR, colorBackQR);
            }
            catch (IllegalArgumentException iae) {
                Log.e("TAG", "Invalid arguments for encoding QR");
                iae.printStackTrace();
                return 0;
            }
            catch (WriterException we) {
                Log.e("TAG", "QR Writer unable to generate code");
                we.printStackTrace();
                return 0;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            //mProgress.setVisibility(View.GONE);
            if (result != 0) {
                qrCodeImage.setImageBitmap(qrCodeImageBitmap);

            }else {
                //mTextDesc.setText(getString(R.string.encode_error));

            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


}
