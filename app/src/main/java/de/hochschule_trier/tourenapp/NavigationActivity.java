package de.hochschule_trier.tourenapp;

import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class NavigationActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        SensorEventListener,
        View.OnClickListener {

    private final static String TAG = "NAVIGATION_ACTIVITY";

    private GoogleMap mMap;
    private ArrayList<Waypoint> waypoints;
    private ArrayList<Waypoint> stationWaypoints;
    private ArrayList<Station> stations;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private FirebaseStorage storage;

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
    private Location currentLocation;
    private float tilt;
    private float bearing;
    private float zoom;

    private boolean started;
    private int stationNumber;

    private Button startTour;
    private Button cancelTour;
    private Button showStation;
    private Button endTour;
    private Button finishTour;
    private Button showMission;
    private Button endStation;

    private LinearLayout stationLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        WaypointWrapper wrapper = (WaypointWrapper) getIntent().getSerializableExtra("WPList");
        WaypointWrapper stationWaypointWrapper
                = (WaypointWrapper) getIntent().getSerializableExtra("StationWPList");
        StationWrapper stationWrapper = (StationWrapper) getIntent().getSerializableExtra("StationList");
        waypoints = wrapper.getWaypoints();
        stationWaypoints = stationWaypointWrapper.getWaypoints();
        stations = stationWrapper.getStations();


        tourID = getIntent().getStringExtra("TourID");


        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        storage = FirebaseStorage.getInstance();

        if (mGoogleApiClient == null)

            buildGoogleApiClient();

        createLocationRequest();

        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !requestProgress)

        {
            requestProgress = true;
            mGoogleApiClient.connect();
        }

        mSensorManager = (SensorManager)

                getSystemService(SENSOR_SERVICE);

        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        startTour = (Button) findViewById(R.id.startTour);
        startTour.setOnClickListener(this);
        cancelTour = (Button) findViewById(R.id.cancelTour);
        cancelTour.setOnClickListener(this);
        endTour = (Button) findViewById(R.id.endTour);
        endTour.setOnClickListener(this);
        showStation = (Button) findViewById(R.id.showStation);
        showStation.setOnClickListener(this);
        endStation = (Button) findViewById(R.id.endStation);
        endStation.setOnClickListener(this);


        if (savedInstanceState != null)

        {
            latitude = savedInstanceState.getDouble("Latitude");
            longitude = savedInstanceState.getDouble("Longitude");
            tilt = savedInstanceState.getFloat("Tilt");
            zoom = savedInstanceState.getFloat("Zoom");
            bearing = savedInstanceState.getFloat("Bearing");
            started = savedInstanceState.getBoolean("Started");
            if (started) {
                startTour.setVisibility(View.GONE);
                cancelTour.setVisibility(View.VISIBLE);

                if (stations.size() > stationNumber) {

                    if (isInRange(stations.get(stationNumber).getWaypoint())) {
                        showStation.setEnabled(true);
                    } else showStation.setEnabled(false);

                }

                //no more stations available
                else if (isInRange(waypoints.get(waypoints.size() - 1))) {
                    endTour.setEnabled(true);
                } else
                    endTour.setEnabled(false);
            }
            stationNumber = savedInstanceState.getInt("StationNumber");

        } else {

            latitude = waypoints.get(0).getLatitude();
            longitude = waypoints.get(0).getLongitude();
            tilt = 0;
            zoom = 18;
            bearing = 0;
            started = false;
            stationNumber = 0;
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

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
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
        outState.putBoolean("Started", started);
        outState.putInt("StationNumber", stationNumber);

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
        mMap.getUiSettings().setMapToolbarEnabled(false);
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

        //Add Marker to first waypoint
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(
                        waypoints.get(0).getLatitude(),
                        waypoints.get(0).getLongitude()))
                .title("Start")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));


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

            if (waypoints.get(z).getComment() != null) {

                if (waypoints.get(z).getComment().equals("Station")) {

                } else {
                    //Add Marker to first waypoint
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(
                                    waypoints.get(z).getLatitude(),
                                    waypoints.get(z).getLongitude()))
                            .title(getResources().getString(R.string.comment))
                            .snippet(waypoints.get(z).getComment())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.comment)));
                }

            }

            if (waypoints.get(z).getImageURL() != null){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(
                                waypoints.get(z).getLatitude(),
                                waypoints.get(z).getLongitude()))
                        .title(getResources().getString(R.string.photo))
                        .snippet(waypoints.get(z).getComment())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.photo_marker)));
            }

            //check if current Waypoint unlocks another part of the tour. if it does hide the rest of the tour.
            if (waypoints.get(z).isUnlocking())
                break;
        }

        //connect Station and Waypoint information
        addStations();


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

        currentLocation = location;
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

        if (!started) {
            if (isInRange(waypoints.get(0))) {
                startTour.setEnabled(true);
            } else startTour.setEnabled(false);
        }


    }

    //Station information and Waypoints get connected
    public void addStations() {
        for (int i = 0; i < stations.size(); i++) {

            //station index and waypoint index should always be the same. Check anyway to prevent errors.
            if (stations.get(i).getId().equals(stationWaypoints.get(i).getStationID())) {
                stations.get(i).setWaypoint(stationWaypoints.get(i));
            }

        }

        for (int i = 0; i < stations.size(); i++) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(
                            stationWaypoints.get(i).getLatitude(),
                            stationWaypoints.get(i).getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .title(stations.get(i).getName())
                    .snippet(stations.get(i).getDescription()));

            if (stations.get(i).getMission() != null) {
                if (stations.get(i).getMission().isUnlocking())
                    break;
            }
        }


    }


    public boolean isInRange(Waypoint wp) {

        Location waypoint = new Location("WP");
        waypoint.setLatitude(wp.getLatitude());
        waypoint.setLongitude(wp.getLongitude());
        float distance = currentLocation.distanceTo(waypoint);

        if (distance < 30)
            return true;

        else return false;
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.startTour:

                startTour.setVisibility(View.GONE);
                if (stations.size() > 0)
                    showStation.setVisibility(View.VISIBLE);
                else
                    endTour.setVisibility(View.VISIBLE);
                cancelTour.setVisibility(View.VISIBLE);
                started = true;
                break;

            case R.id.cancelTour:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.cancel_tour_message)
                        .setTitle(R.string.cancel_rec_title);

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        finish();

                    }
                });

                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                break;

            case R.id.showStation:

                stationLayout = (LinearLayout) findViewById(R.id.stationLayout);
                stationLayout.setVisibility(View.VISIBLE);
                TextView stationName = (TextView) findViewById(R.id.stationName);
                stationName.setText(stations.get(stationNumber).getName());
                TextView stationDescription = (TextView) findViewById(R.id.stationDescription);
                stationDescription.setText(stations.get(stationNumber).getDescription());
                if (stations.get(stationNumber).getImageURL() != null) {

                    ImageView image = (ImageView) findViewById(R.id.stationImage);

                    // Reference to an image file in Firebase Storage
                    StorageReference storageRef = storage.getReference().child(stations.get(stationNumber).getImageURL());


                    // Load the image using Glide
                    Glide.with(this /* context */)
                            .using(new FirebaseImageLoader())
                            .load(storageRef)
                            .into(image);
                }



                break;

            case R.id.endTour:

                finish();
                break;

            case R.id.endStation:

                stationNumber++;
                stationLayout.setVisibility(View.GONE);

                break;
        }


    }
}
