package com.example.admin.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, SensorEventListener {
    // Maps variables
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    double updateLat = 0.0;
    double updateLong = 0.0;
    Location location;
    double startingLat = 0.0;
    double startingLong = 0.0;
    // Sensors
    private SensorManager mSensorManager;
    private Sensor mGravity;
    // Hanlder for delays
    final Handler handler = new Handler();
    // Firebase
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mBooleanReference;
    private ChildEventListener mChildEventListener;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Log.d("Maps", "Running onCreate");
        getSupportActionBar().setTitle("Map Travel Activity");
        // creating map fragment
        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
        // initiating sensor manager
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // creating an instance for gravity sensor, the sensor used
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        // creating reference to firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Location");
        // Child event listener for firebase to be called when app starts
        if (mChildEventListener == null) { // create an instance of the child event listener if it does not exist
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                    Place place = dataSnapshot.getValue(Place.class); // instance of the Place class
                    LatLng newLocation = new LatLng(
                        place.latitude, place.longitude
                    ); // Latitude and longitude object used for markers

                    mGoogleMap.addMarker(new MarkerOptions()
                            .position(newLocation)
                            .title(dataSnapshot.getKey())
                            .snippet(place.dish)); // creating markers for each of the location in the firebase's JSON file
                    Log.d("Database", "Successfully placed pre-determined markers.");
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }
                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    @Override
    // listener event to be called when the sensor values change
    public void onSensorChanged(SensorEvent sensorEvent) {
        // the 2 sensor values used here are x and y values of the gravity sensor
        float x = sensorEvent.values[0];
        float y = sensorEvent.values[1];

        // update longitude accordingly
        // as phone is tilted more, the change in coordinates are faster
        if (x > 2){updateLong = -0.5;}
        else if (x > 3){updateLong = -0.75;}
        else if (x > 5){updateLong = -1.0;}
        else if (x < -2){updateLong = 0.5;}
        else if (x < -3){updateLong = 0.75;}
        else if (x < -5){updateLong = 1.0;}
        else {updateLong = 0.0;}

        if (y > 2){updateLat = -0.5;}
        else if (y > 3){updateLat = -0.75;}
        else if (y > 5){updateLat = -1.0;}
        else if (y < -2){updateLat = 0.5;}
        else if (y < -3){updateLat = 0.75;}
        else if (y < -5){updateLat = 1.0;}
        else {updateLat = 0.0;};
        Log.d("Sensors", "Updating sensor values");
        // delay function between the
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                // Do something after 5s = 5000ms
                updateMarkerPosition(location);
            }
        }, 200);
    }

    public void updateMarkerPosition(Location location){ // function to update the location of the camera
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        // adjusting the current coordinates according to sensor values
        double adjustedLat = startingLat + updateLat;
        double adjustedLong = startingLong + updateLong;

        // options for marker display
        LatLng adjustedLatLng = new LatLng (adjustedLat, adjustedLong);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(adjustedLatLng);
        markerOptions.title("You are here");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

        // the new starting location of the marker is updated
        startingLat = adjustedLat;
        startingLong = adjustedLong;
        // draw the marker at the current coordinates
        // omitted due to high memory consumption
//        if (updateLong != 0 || updateLat != 0){
        //mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
//         }
        // moving camera to the location of the marker instead
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(adjustedLatLng,5));
        Log.d("Update", "Updating marker position");
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        Log.d("Pause", "App paused, stop location updates.");
    }
    @Override
    protected void onResume() {
        super.onResume();
        // register the Sensor event listener as the activity resumes
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) // function to call when GoogleMaps is ready
    {
        mGoogleMap=googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission in case it was not already granted
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) { // function to be called when the Google Location services connects
        mLocationRequest = new LocationRequest(); // intiating an instance of the location request
        mLocationRequest.setInterval(1000); // interval set to 1s
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        // as the location services have been granted permission, request to update locations
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (mCurrLocationMarker != null) { // remove marker if the location is not found, to avoid crashing
            mCurrLocationMarker.remove();
        }

        // stop requesting for the current latitude and longitude after the user's location has been found
        // this is to avoid the case when the user moves while using the app
        if (startingLat != 0 && startingLong != 0){}
        else{
        startingLat = location.getLatitude();
        startingLong = location.getLongitude();}

        Log.d("Location", "Placing marker at current location");
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() { // function to be called when requesting permission for location services
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) { // function to be called after the permission has been
        // or not been granted
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    Log.d("Location Request","Successful");
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                    Log.d("Location request", "Failed");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Maps tag", "now running onStart");
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("Maps tag", "now running onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Maps tag", "now running onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Maps tag", "now running onDestroy");
    }
}