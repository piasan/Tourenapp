package de.hochschule_trier.tourenapp;


import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NavigationActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SensorEventListener {

    private final static String TAG = "NAVIGATION_ACTIVITY";

    private GoogleMap mMap;
    private ArrayList<Waypoint> waypoints;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private String tourID;

    private boolean requestProgress;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    Marker mPositionMarker;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    float[] mGravity;
    float[] mGeomagnetic;

    private float azimut;

    private double latitude;
    private double longitude;
    private float tilt;
    private float bearing;
    private float zoom;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        WaypointWrapper wrapper = (WaypointWrapper) getIntent().getSerializableExtra("WPList");
        waypoints = wrapper.getWaypoints();

        tourID = getIntent().getStringExtra("TourID");


        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        if (mGoogleApiClient == null)
            buildGoogleApiClient();
        createLocationRequest();

        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !requestProgress) {
            requestProgress = true;
            mGoogleApiClient.connect();
        }

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        if (savedInstanceState != null) {
            latitude = savedInstanceState.getDouble("Latitude");
            longitude = savedInstanceState.getDouble("Longitude");
            tilt = savedInstanceState.getFloat("Tilt");
            zoom = savedInstanceState.getFloat("Zoom");
            bearing = savedInstanceState.getFloat("Bearing");
        } else {
            latitude = waypoints.get(0).getLatitude();
            longitude = waypoints.get(0).getLongitude();
            tilt = 0;
            zoom = 18;
            bearing = 0;
        }

    }

    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    protected void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble("Latitude", latitude);
        outState.putDouble("Longitude", longitude);
        outState.putFloat("Tilt", tilt);
        outState.putFloat("Bearing", bearing);
        outState.putFloat("Zoom", zoom);

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        this.requestProgress = false;

        if (this.mGoogleApiClient != null) {
            this.mGoogleApiClient.unregisterConnectionCallbacks(this);
            this.mGoogleApiClient.unregisterConnectionFailedListener(this);
            this.mGoogleApiClient.disconnect();
            // Destroy the current location client
            this.mGoogleApiClient = null;
        }
        super.onDestroy();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setMaxZoomPreference(20);
        mMap.setMinZoomPreference(14);

        // Add waypoints and draw a polyline
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(false);

        if (mPositionMarker == null) {

            mPositionMarker = mMap.addMarker(new MarkerOptions()
                    .flat(true)
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.arrow))
                    .anchor(0.5f, 0.5f)
                    .position(
                            new LatLng(latitude, longitude)));
        }


        for (int z = 0; z < waypoints.size(); z++) {

            options.add(new LatLng(
                    waypoints.get(z).getLatitude(),
                    waypoints.get(z).getLongitude()));

            if (waypoints.get(z).getStationID() != null) {
                mMap.addCircle(new CircleOptions()
                        .center(new LatLng(
                                waypoints.get(z).getLatitude(),
                                waypoints.get(z).getLongitude()))
                        .radius(0.1)
                        .strokeWidth(10)
                        .strokeColor(Color.BLUE));

            }

            //check if current Waypoint unlocks another part of the tour. if it does hide the rest of the tour.
            if (waypoints.get(z).isUnlocking())
                break;
        }

        mMap.addPolyline(options);


        CameraPosition cameraPosition = new CameraPosition(new LatLng(
                waypoints.get(0).getLatitude(),
                waypoints.get(0).getLongitude()), zoom, tilt, bearing);


        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    @Override
    public void onConnected(Bundle bundle) {

        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,
                mLocationRequest, this);

    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        zoom = mMap.getCameraPosition().zoom;
        tilt = mMap.getCameraPosition().tilt;
        bearing = mMap.getCameraPosition().bearing;

        if (mPositionMarker != null)
            mPositionMarker.setPosition(new LatLng(latitude, longitude));

        CameraPosition cameraPosition = new CameraPosition(
                new LatLng(latitude, longitude), zoom, tilt, bearing);


        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    @Override
    public void onConnectionSuspended(int i) {
        // Turn off the request flag
        requestProgress = false;
        // Destroy the current location client
        mGoogleApiClient = null;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        requestProgress = false;

        if (connectionResult.hasResolution()) {

            // If no resolution is available, display an error dialog
        } else {

            //

        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = (float) Math.toDegrees(orientation[0]);
                if (mPositionMarker != null)
                    if (Math.abs(mPositionMarker.getRotation() - azimut) > 5)
                        mPositionMarker.setRotation(azimut);

            }
        }
    }
}
