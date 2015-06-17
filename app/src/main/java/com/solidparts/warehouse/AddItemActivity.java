package com.solidparts.warehouse;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.print.PrintHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.solidparts.warehouse.dto.ItemDTO;
import com.solidparts.warehouse.service.ItemService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public class AddItemActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    static public int MARGIN_AUTOMATIC = -1;
    public static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    public static final int CAMERA_REQUEST = 1;
    public static final int IMAGE_GALLERY_REQUEST = 2;
    public static final int QR_REQUEST = 3;
    public final static int MILLISECONDS_PER_SECOND = 1000;
    public final static int MINUTE = 60 * MILLISECONDS_PER_SECOND;

    private ImageView itemImage;
    private ImageView qrCodeImage;
    private ItemService itemService;
    private Bitmap itemImageBitmap;
    private Bitmap qrCodeImageBitmap;
    private ItemDTO intentItemDTO;
    private long cacheId = 0;

    private boolean update = false;

    private FusedLocationProviderApi locationProvicer = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;


    LocationManager locationManager ;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        itemImage = ((ImageView) findViewById(R.id.itemImage));
        qrCodeImage = ((ImageView) findViewById(R.id.qrCodeImage));
        itemService = new ItemService(this);

        intentItemDTO = (ItemDTO) getIntent().getSerializableExtra("intentItemDTO");

        if (intentItemDTO != null) {
            cacheId = intentItemDTO.getCacheID();
            ((TextView) findViewById(R.id.itemName)).setText(intentItemDTO.getName());
            ((TextView) findViewById(R.id.saveUpdate)).setText("Update");
            ((EditText) findViewById(R.id.name)).setText(intentItemDTO.getName());
            ((EditText) findViewById(R.id.description)).setText(intentItemDTO.getDescription());
            ((EditText) findViewById(R.id.amount)).setText("" + intentItemDTO.getCount());
            ((EditText) findViewById(R.id.location)).setText(intentItemDTO.getLocation());

            Bitmap image = BitmapFactory.decodeByteArray(intentItemDTO.getImage(), 0, intentItemDTO.getImage().length);
            showImage(image);

            Bitmap qrCodeImage = BitmapFactory.decodeByteArray(intentItemDTO.getQrCode(), 0, intentItemDTO.getQrCode().length);
            showQRCodeImage(qrCodeImage);
            new AsyncGenerateQRCode().execute(-1);
            update = true;
        }

        // GPS
        /*googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = new LocationRequest();

        locationRequest.setInterval(MINUTE);
        locationRequest.setFastestInterval(15 * MILLISECONDS_PER_SECOND);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);*/

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
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    public void onSave(View view) {
        ItemDTO itemDTO = getItemDTO();

        if (itemDTO == null) {
            return;
        }



        if(update){
            ItemUpdateTask itemUpdateTask = new ItemUpdateTask();
            ItemDTO[] items = new ItemDTO[1];
            items[0] = itemDTO;
            itemUpdateTask.execute(items);
            /*try {
                itemService.updateItem(itemDTO);
            } catch (Exception e) {
                e.printStackTrace();
            }
            showMessage("Item Updated!", false);
            startActivity(new Intent(AddItemActivity.this, SearchActivity.class));
            */
        } else {
            ItemAddTask itemAddTask = new ItemAddTask();
            ItemDTO[] items = new ItemDTO[1];
            items[0] = itemDTO;
            itemAddTask.execute(items);
            /*
            itemService.addItem(itemDTO);
            showMessage("Item Saved!", true);
            */
        }
    }




    class ItemUpdateTask extends AsyncTask<ItemDTO, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(ItemDTO... itemDTO) {
            try {
                itemService.updateItem(itemDTO[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        /**
         * This method will be called when doInBackground completes.
         * The paramter result is populated from the return values of doInBackground.
         * This method runs on the UI thread, and therefore can update UI components.
         */

        @Override
        protected void onPostExecute(Boolean success) {
            if(success){
                showMessage("Item Updated!", false);
                startActivity(new Intent(AddItemActivity.this, SearchActivity.class));
            } else {
                showMessage("Item not updated!", false);
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }


    class ItemAddTask extends AsyncTask<ItemDTO, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(ItemDTO... itemDTO) {
            try {
                itemService.addItem(itemDTO[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        /**
         * This method will be called when doInBackground completes.
         * The paramter result is populated from the return values of doInBackground.
         * This method runs on the UI thread, and therefore can update UI components.
         */

        @Override
        protected void onPostExecute(Boolean success) {
            if(success)
                showMessage("Item Saved!", true);
            else
                showMessage("Item not saved!", true);
        }

        @Override
        protected void onPreExecute() {
        }
    }



    private ItemDTO getItemDTO() {
        String name = ((EditText) findViewById(R.id.name)).getText().toString();
        String description = ((EditText) findViewById(R.id.description)).getText().toString();
        String amount = ((EditText) findViewById(R.id.amount)).getText().toString();
        String location = ((EditText) findViewById(R.id.location)).getText().toString();

        /*if (name.equals("") || description.equals("") || amount.equals("") || location.equals("") ||
                itemImageBitmap == null || qrCodeImage == null) {

            showMessage("ERROR: You need to fill in the complete form!", false);

            return null;
        }*/

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setCacheID(cacheId);
        itemDTO.setOnlineid(intentItemDTO.getOnlineid());
        itemDTO.setName(name);
        itemDTO.setDescription(description);
        itemDTO.setCount(Integer.parseInt(amount));
        itemDTO.setLocation(location);

        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();

        itemImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos1);
        qrCodeImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos2);
        byte[] itemImg = bos1.toByteArray();
        byte[] qrCodeImg = bos2.toByteArray();

        itemDTO.setImage(itemImg);
        itemDTO.setQrCode(qrCodeImg);
        return itemDTO;
    }

    public void onRemove(View view) {

        ItemDTO itemDTO = getItemDTO();

        if (itemDTO == null) {
            return;
        }
        ItemRemoveTask itemRemoveTask = new ItemRemoveTask();
        ItemDTO[] items = new ItemDTO[1];
        items[0] = itemDTO;
        itemRemoveTask.execute(items);
    }

    class ItemRemoveTask extends AsyncTask<ItemDTO, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(ItemDTO... itemDTO) {
            try {
                itemService.removeItem(itemDTO[0].getOnlineid());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        /**
         * This method will be called when doInBackground completes.
         * The paramter result is populated from the return values of doInBackground.
         * This method runs on the UI thread, and therefore can update UI components.
         */

        @Override
        protected void onPostExecute(Boolean success) {
            if(success)
                showMessage("Item removed!", true);
            else
                showMessage("Item not removed!", true);
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private void showMessage(String message, boolean goBack) {
        Context context = getApplicationContext();
        CharSequence text = message;
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
        toast.show();

        if (goBack) {
            super.onBackPressed();
        }
    }

    public void onCancle(View view) {
        super.onBackPressed();
    }

    public void onScan(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, QR_REQUEST);
        } catch (ActivityNotFoundException anfe) {
            showMessage("ERROR: Something went wrong!", false);
        }
    }

    public void onGenerateQRCode(View view) {
        new AsyncGenerateQRCode().execute(-1);
    }

    public void onAddExistingImage(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);

        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);

        intent.setDataAndType(data, "image/*");

        startActivityForResult(intent, IMAGE_GALLERY_REQUEST);
    }

    public void onTakePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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

        if (resultCode == RESULT_OK) {
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
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                }
            }

            if (requestCode == QR_REQUEST) {
                String contents = data.getStringExtra("SCAN_RESULT");
                ((EditText) findViewById(R.id.location)).setText(contents);
                new AsyncGenerateQRCode().execute(-1);
            }
        }
    }

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

    private void showImage(Bitmap image) {
        itemImageBitmap = image;
        itemImage.setImageBitmap(itemImageBitmap);
    }

    private void showQRCodeImage(Bitmap image) {
        qrCodeImage.setImageBitmap(image);
    }

    @Override
    public void onConnected(Bundle bundle) {
        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, 9000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            showMessage("Location services connection failed with code " + connectionResult.getErrorCode(), false);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
//        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
       // googleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();

       //if(googleApiClient.isConnected()){
       //     requestLocationUpdates();
       // }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    // https://www.youtube.com/watch?v=ZpwivlI7tzo&index=5&list=PL73qvSDlAVVhoVW6-_TMGWLQbAOAg-yod

    @Override
    public void onLocationChanged(Location location) {
        showMessage("Location changed: " + location.getLatitude() + " " + location.getLongitude() , false);
    }

    /**
     * AsyncTask to generate QR Code image
     */
    private class AsyncGenerateQRCode extends AsyncTask<Integer, Void, Integer> {

        /**
         * Background thread function to generate image
         *
         * @param params margin to use in creating QR Code
         * @return non zero for success
         * <p/>
         * Note that is margin is not in pixels.  See the zxing api for details about the margin
         * for QR code generation
         */
        @Override
        protected Integer doInBackground(Integer... params) {
            if (params.length != 1) {
                throw new IllegalArgumentException("Must pass QR Code margin value as argument");
            }

            try {

                String stringToEncode = ((EditText) findViewById(R.id.location)).getText().toString();

                final int colorQR = Color.BLACK;
                final int colorBackQR = Color.WHITE;
                final int marginSize = params[0];
                final int width = 300;
                final int height = 300;

                qrCodeImageBitmap = generateQRCodeBitmap(stringToEncode, width, height,
                        marginSize, colorQR, colorBackQR);
            } catch (IllegalArgumentException iae) {
                Log.e("TAG", "Invalid arguments for encoding QR");
                iae.printStackTrace();
                return 0;
            } catch (WriterException we) {
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

            } else {
                //mTextDesc.setText(getString(R.string.encode_error));
            }
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public void onPrint(View view) {
        PrintHelper printHelper = new PrintHelper(this);
        printHelper.setScaleMode(printHelper.SCALE_MODE_FIT);
        printHelper.setColorMode(printHelper.COLOR_MODE_MONOCHROME);

        printHelper.printBitmap("Printing QR code", qrCodeImageBitmap);
    }


}
