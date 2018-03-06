package com.demo_on_off.pulkit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener
        , LocationListener {

    private Toolbar toolbar;

    TextView tv_lat_long;
    ToggleButton tb_wifi, tb_gps;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public static final int TIME_INTERVAL = 30000;

    private GoogleApiClient mgoogleApiClient;
    private LocationRequest mlocationRequest;
    public Location mlocation;

    LatLng latLng;

    int delay = 0; // delay for 0 sec.
    int period = 2000; // repeat every 10 sec.
    Timer timer = new Timer();

    WifiManager wifi;

    boolean GpsStatus;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findIds();
        init();

    }

    private void findIds() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tb_wifi = (ToggleButton) findViewById(R.id.tb_wifi);
        tb_gps = (ToggleButton) findViewById(R.id.tb_gps);
        tv_lat_long = (TextView) findViewById(R.id.tv_lat_long);
    }

    private void init() {

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        wifi_on_off_button();
        gps_enabled();
    }

    private void wifi_on_off_button() {

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifi.isWifiEnabled()) {
            tb_wifi.setChecked(true);
        } else {
            tb_wifi.setChecked(false);
        }

        tb_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!tb_wifi.isChecked()) {

                    wifi.setWifiEnabled(false);
                    tb_wifi.setChecked(false);

                } else {

                    wifi.setWifiEnabled(true);
                    tb_wifi.setChecked(true);
                }
            }
        });
    }

    private void gps_enabled() {

        CheckGpsStatus();

        tb_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!tb_gps.isChecked()) {

                    Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent1);
                    CheckGpsStatus();

                } else {

                    Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent1);
                    CheckGpsStatus();

                }
            }
        });
    }

    public void CheckGpsStatus() {

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (GpsStatus == true) {

            tb_gps.setChecked(true);
        } else {
            tb_gps.setChecked(false);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        CheckGpsStatus();
    }

    MenuItem action_get_location;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_get_location, menu);
        action_get_location = menu.findItem(R.id.action_get_location);


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        action_get_location = menu.findItem(R.id.action_get_location);

        action_get_location.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_get_location:

                /*Get Location in everytwo seconds.*/
                timer.scheduleAtFixedRate(new TimerTask() {
                    public void run() {
                        onStartAppPermission();
                    }
                }, delay, period);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onStartAppPermission() {
        /*check permissions*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private synchronized void buildGoogleApiClient() {
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        /*update location in every 2 sec.*/
        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(2000);
        mlocationRequest.setFastestInterval(2000);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (latLng != null) {
            tv_lat_long.append("\n" + String.valueOf(latLng));
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient, mlocationRequest, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        mlocation = location;

       /*Place Current Location*/
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Log.v("latitude: ", latitude + "\n");
        Log.v("longitude: ", longitude + "");

        latLng = new LatLng(latitude, longitude);

        /*stop location update*/
//        if (mgoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleApiClient, this);
//        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onStartAppPermission();

        } else {
            onStartAppPermission();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}
