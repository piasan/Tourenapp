package de.hochschule_trier.tourenapp;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GPSService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //Firebase database
    private DatabaseReference mDatabase;

    private boolean requestProgress;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private Location lastLocation;

    private String tourID;


    //Location Manager
    private static final String TAG = "GPSService";


    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        tourID = intent.getStringExtra("TOURID");

        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting() && !requestProgress) {
            requestProgress = true;
            mGoogleApiClient.connect();
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {

        // Get an instance of the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        if (mGoogleApiClient == null)
            buildGoogleApiClient();
        createLocationRequest();


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
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    @Override
    public void onConnected(Bundle bundle) {

        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient,
                mLocationRequest, this);

    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Log.d(TAG, msg);

        Log.d(TAG, "Accuracy: " + location.getAccuracy());

        if (location.getAccuracy() < 30) {


            if (lastLocation != null) {

                if (lastLocation.distanceTo(location) >= 10) {

                    lastLocation = location;
                    Log.d(TAG, "saved");

                    //Write last location into the database
                    Waypoint waypoint = new Waypoint(location.getLatitude(), location.getLongitude());
                    mDatabase.child("Waypoints").child("Tour" + tourID).push().setValue(waypoint);
                } else
                    Log.d(TAG, "not saved");

            } else {
                //Write last location into the database
                Waypoint waypoint = new Waypoint(location.getLatitude(), location.getLongitude());
                mDatabase.child("Waypoints").child("Tour" + tourID).push().setValue(waypoint);

                lastLocation = location;
                Log.d(TAG, "saved");

            }
        } else
            Log.d(TAG, "not saved (accuracy)");
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

}
