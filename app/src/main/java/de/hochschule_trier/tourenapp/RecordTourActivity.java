package de.hochschule_trier.tourenapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordTourActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int NEW_TOUR_REQUEST_CODE = 2;
    private static final int NEW_COMMENT_REQUEST_CODE = 3;

    //Firebase database
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private String tourID;
    private boolean recording;

    private Location commentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record_tour);


        if (savedInstanceState != null) {

            tourID = savedInstanceState.getString("tourID");

            if (savedInstanceState.getBoolean("recording") == true) {

                findViewById(R.id.layout_rec_inactive).setVisibility(View.GONE);
                findViewById(R.id.layout_rec_active).setVisibility(View.VISIBLE);
                findViewById(R.id.button_start_rec).setVisibility(View.GONE);
                findViewById(R.id.button_stop_rec).setVisibility(View.VISIBLE);
                findViewById(R.id.button_cancel_rec).setVisibility(View.VISIBLE);

                recording = true;
            }
        }


        // Button listeners
        findViewById(R.id.button_start_rec).setOnClickListener(this);
        findViewById(R.id.button_stop_rec).setOnClickListener(this);
        findViewById(R.id.button_comment).setOnClickListener(this);
        findViewById(R.id.button_cancel_rec).setOnClickListener(this);

        // Get an instance of the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        // Current Firebase User
        user = FirebaseAuth.getInstance().getCurrentUser();


    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    //Save current recording state
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("recording", recording);
        outState.putString("tourID", tourID);
        super.onSaveInstanceState(outState);
    }


    // Get message from other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {


            case NEW_TOUR_REQUEST_CODE:

                if (resultCode == RESULT_OK) {

                    String tourName = data.getStringExtra("TOUR_NAME");
                    String tourDescription = data.getStringExtra("TOUR_DESCRIPTION");
                    boolean[] tags = data.getBooleanArrayExtra("TAGS");

                    createNewTour(tourName, tourDescription, user.getUid(), tags);

                    finish();

                }

                if (resultCode == RESULT_CANCELED) {

                    //If creating Tour is cancelled, existing data will be deleted
                    mDatabase.child("Waypoints").child("Tour" + tourID).removeValue();

                    finish();
                }

                break;

            case NEW_COMMENT_REQUEST_CODE:

                if (resultCode == RESULT_OK) {

                    String comment = data.getStringExtra("COMMENT");


                    if (commentLocation != null) {

                        Waypoint wp = new Waypoint(commentLocation.getLatitude(), commentLocation.getLongitude(), comment);
                        mDatabase.child("Waypoints").child("Tour" + tourID).push().setValue(wp);

                    } else
                        Toast.makeText(this, "Kommentar konnte nicht gespeichert werden", Toast.LENGTH_LONG).show();
                }

                break;

        }


    }

    private void createNewTour(String tourName, String tourDescription, String authorName, boolean[] tags) {

        long timestamp = System.currentTimeMillis();

        Tour tour = new Tour(tourName, authorName, timestamp, tourID, tourDescription);

        String[] tagList = getResources().getStringArray(R.array.tag_list);


        mDatabase.child("Touren").child(tourID).setValue(tour);

        for (int i = 0; i < tags.length; i++) {

            if (tagList[i].equals(getResources().getString(R.string.foot)))
                mDatabase.child("Touren").child(tourID).child("Tags").child("foot").setValue(tags[i]);
            else if (tagList[i].equals(getResources().getString(R.string.bike)))
                mDatabase.child("Touren").child(tourID).child("Tags").child("bike").setValue(tags[i]);
            else if (tagList[i].equals(getResources().getString(R.string.dogs)))
                mDatabase.child("Touren").child(tourID).child("Tags").child("dogs").setValue(tags[i]);
            else if (tagList[i].equals(getResources().getString(R.string.wheelchair)))
                mDatabase.child("Touren").child(tourID).child("Tags").child("wheelchair").setValue(tags[i]);
            else if (tagList[i].equals(getResources().getString(R.string.flat)))
                mDatabase.child("Touren").child(tourID).child("Tags").child("flat").setValue(tags[i]);
            else if (tagList[i].equals(getResources().getString(R.string.multi)))
                mDatabase.child("Touren").child(tourID).child("Tags").child("multi").setValue(tags[i]);
            else if (tagList[i].equals(getResources().getString(R.string.games)))
                mDatabase.child("Touren").child(tourID).child("Tags").child("games").setValue(tags[i]);
            else if (tagList[i].equals(getResources().getString(R.string.restricted)))
                mDatabase.child("Touren").child(tourID).child("Tags").child("restricted").setValue(tags[i]);

        }

    }


    // On Click Listener
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.button_start_rec:

                recording = true;

                //Check Permissions for GPS Usage
                //If permission is not granted, service can't be started.
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSION_ACCESS_COARSE_LOCATION);

                }

                //Create new tour ID when Service is started
                tourID = mDatabase.child("Touren").push().getKey();

                findViewById(R.id.layout_rec_inactive).setVisibility(View.GONE);
                findViewById(R.id.layout_rec_active).setVisibility(View.VISIBLE);
                findViewById(R.id.button_start_rec).setVisibility(View.GONE);
                findViewById(R.id.button_stop_rec).setVisibility(View.VISIBLE);
                findViewById(R.id.button_cancel_rec).setVisibility(View.VISIBLE);

                Intent startIntent = new Intent(this, GPSService.class);
                startIntent.putExtra("TOURID", tourID);
                startService(startIntent);
                break;

            case R.id.button_stop_rec:

                recording = false;

                findViewById(R.id.layout_rec_inactive).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_rec_active).setVisibility(View.GONE);
                findViewById(R.id.button_stop_rec).setVisibility(View.GONE);
                findViewById(R.id.button_start_rec).setVisibility(View.VISIBLE);
                findViewById(R.id.button_cancel_rec).setVisibility(View.INVISIBLE);

                Intent stopIntent = new Intent(this, GPSService.class);
                stopService(stopIntent);

                Intent newTourIntent = new Intent(this, CreateNewTourActivity.class);
                startActivityForResult(newTourIntent, NEW_TOUR_REQUEST_CODE);

                break;

            case R.id.button_comment:

                //Check Permissions for GPS Usage
                //If permission is not granted, service can't be started.
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            MY_PERMISSION_ACCESS_COARSE_LOCATION);

                }

                LocationManager mLocationManager =
                        (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                commentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                Intent newCommentIntent = new Intent(this, CommentActivity.class);
                startActivityForResult(newCommentIntent, NEW_COMMENT_REQUEST_CODE);
                break;




            case R.id.button_cancel_rec:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.cancel_rec_message)
                        .setTitle(R.string.cancel_rec_title);

                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {

                        recording = false;

                        findViewById(R.id.layout_rec_inactive).setVisibility(View.VISIBLE);
                        findViewById(R.id.layout_rec_active).setVisibility(View.GONE);
                        findViewById(R.id.button_stop_rec).setVisibility(View.GONE);
                        findViewById(R.id.button_start_rec).setVisibility(View.VISIBLE);
                        findViewById(R.id.button_cancel_rec).setVisibility(View.INVISIBLE);


                        //If creating Tour is cancelled, existing data will be deleted
                        mDatabase.child("Waypoints").child("Tour" + tourID).removeValue();
                        Intent stopIntent = new Intent(getApplicationContext(), GPSService.class);
                        stopService(stopIntent);

                    }
                });

                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {


                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                break;

        }
    }
}
