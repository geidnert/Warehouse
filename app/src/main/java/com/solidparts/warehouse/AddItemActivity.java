package com.solidparts.warehouse;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.print.PrintHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.solidparts.warehouse.dto.ItemDTO;
import com.solidparts.warehouse.service.ItemService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;


public class AddItemActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    static public int MARGIN_AUTOMATIC = -1;
    public static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    public static final int CAMERA_REQUEST = 1;
    public static final int IMAGE_GALLERY_REQUEST = 2;
    public static final int QR_REQUEST = 3;
    public final static int MILLISECONDS_PER_SECOND = 1000;
    public final static int MINUTE = 60 * MILLISECONDS_PER_SECOND;
    public final static String EXTRA_FROM_ACTIVITY = "fromActivity";
    public final static String EXTRA_LONGITUDE = "longitude";
    public final static String EXTRA_LATITUDE = "latitude";

    private ImageView itemImage;
    private ImageView qrCodeImage;
    private ItemService itemService;
    private Bitmap itemImageBitmap;
    private Bitmap qrCodeImageBitmap;
    private ItemDTO intentItemDTO;
    private long cacheId = 0;
    private boolean update = false;
    private String lastSearchWorkd;
    private Location itemLocation;

    private FusedLocationProviderApi locationProvicer = LocationServices.FusedLocationApi;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private MessageManager messageManager;



    LocationManager locationManager;
    String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        messageManager = new MessageManager();

        itemImage = ((ImageView) findViewById(R.id.itemImage));
        qrCodeImage = ((ImageView) findViewById(R.id.qrCodeImage));
        itemService = new ItemService(this);

        intentItemDTO = (ItemDTO) getIntent().getSerializableExtra("intentItemDTO");
        lastSearchWorkd = getIntent().getStringExtra("searchWord");

        if (intentItemDTO != null) {
            cacheId = intentItemDTO.getCacheID();
            ((TextView) findViewById(R.id.remove)).setVisibility(View.VISIBLE);

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
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = new LocationRequest();

        locationRequest.setInterval(MINUTE);
        locationRequest.setFastestInterval(15 * MILLISECONDS_PER_SECOND);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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

        return super.onOptionsItemSelected(item);
    }

    public void onSave(View view) {
        ItemDTO itemDTO = getItemDTO();

        if (itemDTO == null) {
            return;
        }

        if (update) {
            ItemUpdateTask itemUpdateTask = new ItemUpdateTask();
            ItemDTO[] items = new ItemDTO[1];
            items[0] = itemDTO;
            itemUpdateTask.execute(items);
        } else {
            ItemAddTask itemAddTask = new ItemAddTask();
            ItemDTO[] items = new ItemDTO[1];
            items[0] = itemDTO;
            itemAddTask.execute(items);
        }
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

    public void onCancle(View view) {
        if (lastSearchWorkd != null) {
            Intent intent = new Intent(AddItemActivity.this, SearchActivity.class);
            String[] params = {"removedItem", lastSearchWorkd};
            intent.putExtra(EXTRA_FROM_ACTIVITY, params);
            startActivity(intent);
        } else {
            startActivity(new Intent(AddItemActivity.this, MainActivity.class));
        }
    }

    public void onScan(View v) {
        try {
            //start the scanning activity from the com.google.zxing.client.android.SCAN intent
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, QR_REQUEST);
        } catch (ActivityNotFoundException anfe) {
            messageManager.show(getApplicationContext(), "ERROR: Something went wrong!", false);
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

    public void onShowImage(View view) {
        hideButtons();
        ((ImageView) findViewById(R.id.fullImage)).setImageBitmap(itemImageBitmap);
        (findViewById(R.id.fullImage)).setVisibility(View.VISIBLE);
    }

    public void onHideImage(View view) {
        showButtons();
        (findViewById(R.id.fullImage)).setVisibility(View.INVISIBLE);
    }

    public void onUpdateGps(View view){
        if(googleApiClient.isConnected()){
            requestLocationUpdates();
        }
    }

    public void onShowGps(View view) {
        if(itemLocation != null) {
            Intent intent = new Intent(AddItemActivity.this, GPSActivity.class);
            intent.putExtra(EXTRA_LONGITUDE, itemLocation.getLongitude());
            intent.putExtra(EXTRA_LATITUDE, itemLocation.getLatitude());
            startActivity(intent);
        }
    }

    private void hideButtons() {
        findViewById(R.id.addImage).setVisibility(View.INVISIBLE);
        findViewById(R.id.saveUpdate).setVisibility(View.INVISIBLE);
        findViewById(R.id.button6).setVisibility(View.INVISIBLE);
        findViewById(R.id.button4).setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_print).setVisibility(View.INVISIBLE);
        findViewById(R.id.button7).setVisibility(View.INVISIBLE);
        findViewById(R.id.button8).setVisibility(View.INVISIBLE);
        findViewById(R.id.remove).setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_show_gps).setVisibility(View.INVISIBLE);
        findViewById(R.id.btn_up_gps).setVisibility(View.INVISIBLE);
    }

    private void showButtons() {
        findViewById(R.id.addImage).setVisibility(View.VISIBLE);
        findViewById(R.id.saveUpdate).setVisibility(View.VISIBLE);
        findViewById(R.id.button6).setVisibility(View.VISIBLE);
        findViewById(R.id.button4).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_print).setVisibility(View.VISIBLE);
        findViewById(R.id.button7).setVisibility(View.VISIBLE);
        findViewById(R.id.button8).setVisibility(View.VISIBLE);
        findViewById(R.id.remove).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_show_gps).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_up_gps).setVisibility(View.VISIBLE);
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
                try {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    showImage(bitmap);
                } catch (Exception e) {
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

    public static Bitmap generateQRCodeBitmap(@NonNull String contentsToEncode,
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

    @Override
    public void onConnected(Bundle bundle) {
        //requestLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                googleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((AddItemActivity)getActivity()).onDialogDismissed();
        }
    }



   /* @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, 9000);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            messageManager.show(getApplicationContext(), "Location services connection failed with code " + connectionResult.getErrorCode(), false);
        }
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        messageManager.show(getApplicationContext(), "Location changed: " + location.getLatitude() + " " + location.getLongitude(), false);
        itemLocation = location;
    }




    public void onPrint(View view) {
        PrintHelper printHelper = new PrintHelper(this);
        printHelper.setScaleMode(printHelper.SCALE_MODE_FIT);
        printHelper.setColorMode(printHelper.COLOR_MODE_MONOCHROME);

        printHelper.printBitmap("Printing QR code", qrCodeImageBitmap);
    }

    //---------------------------------------------------------------------------------------------
    //---------------------------- PRIVATE --------------------------------------------------------

    private void showImage(Bitmap image) {
        itemImageBitmap = image;
        itemImage.setImageBitmap(itemImageBitmap);
    }

    private void showQRCodeImage(Bitmap image) {
        qrCodeImage.setImageBitmap(image);
    }

    private void requestLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private ItemDTO getItemDTO() {
        String name = ((EditText) findViewById(R.id.name)).getText().toString();
        String description = ((EditText) findViewById(R.id.description)).getText().toString();
        String amount = ((EditText) findViewById(R.id.amount)).getText().toString();
        String location = ((EditText) findViewById(R.id.location)).getText().toString();

        if (name.equals("") || description.equals("") || amount.equals("") || location.equals("") ||
                itemImageBitmap == null || qrCodeImage == null) {

            messageManager.show(getApplicationContext(), "ERROR: You need to fill in the complete form and generate a qr code and add a image!", false);

            return null;
        }

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setCacheID(cacheId);
        itemDTO.setOnlineid(intentItemDTO != null ? intentItemDTO.getOnlineid() : -1);
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


    //---------------------------------------------------------------------------------------------
    // -------------------------- ASYNC -----------------------------------------------------------

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

    private class ItemRemoveTask extends AsyncTask<ItemDTO, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(ItemDTO... itemDTO) {
            try {
                itemService.removeItem(itemDTO[0]);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                messageManager.show(getApplicationContext(), "Item removed!", false);
            } else {
                messageManager.show(getApplicationContext(), "Item not removed!", false);
            }

            Intent intent = new Intent(AddItemActivity.this, SearchActivity.class);
            String[] params = {"removedItem", lastSearchWorkd};
            intent.putExtra(EXTRA_FROM_ACTIVITY, params);
            startActivity(intent);
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class ItemAddTask extends AsyncTask<ItemDTO, Integer, Boolean> {

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

        @Override
        protected void onPostExecute(Boolean success) {
            if (success)
                messageManager.show(getApplicationContext(), "Item Saved!", false);
            else
                messageManager.show(getApplicationContext(), "Item not saved!", false);

            startActivity(new Intent(AddItemActivity.this, MainActivity.class));
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private class ItemUpdateTask extends AsyncTask<ItemDTO, Integer, Boolean> {

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

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                messageManager.show(getApplicationContext(), "Item Updated!", false);
            } else {
                messageManager.show(getApplicationContext(), "Item not updated!", false);
            }

            Intent intent = new Intent(AddItemActivity.this, SearchActivity.class);
            String[] params = {"removedItem", lastSearchWorkd};
            intent.putExtra(EXTRA_FROM_ACTIVITY, params);
            startActivity(intent);
        }

        @Override
        protected void onPreExecute() {
        }
    }
}
