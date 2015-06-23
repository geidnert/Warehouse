package com.solidparts.warehouse;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class GPSActivity extends FragmentActivity implements GoogleMap.OnMapClickListener {


    private GoogleMap map;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setOnMapClickListener(this);

        double longitude =  getIntent().getDoubleExtra("longitude", 0.0d);
        double latitude =  getIntent().getDoubleExtra("latitude", 0.0d);
        Location targetLocation = new Location("");
        targetLocation.setLongitude(longitude);
        targetLocation.setLatitude(latitude);

        drawItemMarker(targetLocation);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gps, menu);
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

    private void drawItemMarker(Location location){

        map.clear();

        //  convert the location object to a LatLng object that can be used by the map API
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

        // zoom to the current location
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 16));

        // add a marker to the map indicating our current position
        map.addMarker(new MarkerOptions()
                .position(currentPosition)
                .snippet("Lat:" + location.getLatitude() + "Lng:"+ location.getLongitude()));

        map.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        //showButtons();
        super.onBackPressed();
    }
}
