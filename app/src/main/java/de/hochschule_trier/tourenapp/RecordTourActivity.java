package de.hochschule_trier.tourenapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordTourActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RECORD_TOUR_ACTIVITY";
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;

    private static final int NEW_TOUR_REQUEST_CODE = 2;
    private static final int NEW_COMMENT_REQUEST_CODE = 3;
    private static final int NEW_STATION_REQUEST_CODE = 4;
    private static final int REQUEST_IMAGE_CAPTURE = 5;

    //Firebase database
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private StorageReference storageRef;

    private String tourID;
    private Waypoint waypoint;
    private boolean recording;

    private Location commentLocation;
    private Location stationLocation;
    private Location photoLocation;

    private Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_record_tour);


        if (savedInstanceState != null) {

            tourID = savedInstanceState.getString("tourID");

            if (savedInstanceState.getBoolean("recording")) {

                findViewById(R.id.layout_rec_inactive).setVisibility(View.GONE);
                findViewById(R.id.layout_rec_active).setVisibility(View.VISIBLE);
                findViewById(R.id.textView).setVisibility(View.VISIBLE);

                recording = true;
            }

            imageBitmap = savedInstanceState.getParcelable("image");
        }


        // Button listeners
        findViewById(R.id.button_start_rec).setOnClickListener(this);
        findViewById(R.id.button_stop_rec).setOnClickListener(this);
        findViewById(R.id.button_cancel_rec).setOnClickListener(this);
        findViewById(R.id.button_comment).setOnClickListener(this);
        findViewById(R.id.button_photo).setOnClickListener(this);
        findViewById(R.id.button_station).setOnClickListener(this);

        // Get an instance of the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

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
        outState.putParcelable("image", imageBitmap);
        super.onSaveInstanceState(outState);
    }


    // Get message from other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {


            case NEW_TOUR_REQUEST_CODE:

                if (resultCode == RESULT_OK) {

                    final String tourName = data.getStringExtra("TOUR_NAME");
                    final String tourDescription = data.getStringExtra("TOUR_DESCRIPTION");
                    final boolean[] tags = data.getBooleanArrayExtra("TAGS");

                    //get index nr for first waypoint

                    //Get first waypoint
                    mDatabase.child("Waypoints").child("Tour" + tourID).limitToFirst(1).
                            addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    waypoint = new Waypoint(0, 0);

                                    for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                                        waypoint = snapshot1.getValue(Waypoint.class);
                                    }

                                    long indexNr = TourIndex.getIndex(waypoint.getLatitude(), waypoint.getLongitude());

                                    createNewTour(tourName, tourDescription, user.getUid(), tags, indexNr);

                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {

                                    // Failed to read value
                                    Log.w(TAG, "Failed to read value.", error.toException());
                                    finish();
                                }
                            });


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

            case NEW_STATION_REQUEST_CODE:

                if (resultCode == RESULT_OK) {

                    String stationName = data.getStringExtra("STATION_NAME");
                    String stationDescription = data.getStringExtra("STATION_DESCRIPTION");
                    String imageName = data.getStringExtra("IMAGE");

                    if (stationLocation != null) {

                        String stationID = mDatabase.child("Stations").child("Tour" + tourID).push().getKey();
                        Waypoint wp = new Waypoint(stationLocation.getLatitude(), stationLocation.getLongitude(),
                                "Station", stationID, false);

                        Station station = new Station(stationName, stationDescription, stationID);

                        if (imageName != null) {
                            station.setImageURL(imageName);
                        }

                        mDatabase.child("Waypoints").child("Tour" + tourID).push().setValue(wp);
                        mDatabase.child("Stations").child("Tour" + tourID).child(stationID).setValue(station);

                        if (data.getBooleanExtra("MISSION", false)) {

                            String question = data.getStringExtra("QUESTION");
                            String answer = data.getStringExtra("ANSWER");
                            Boolean multi = data.getBooleanExtra("MULTI", false);

                            String attempts = data.getStringExtra("ATTEMPTS");
                            long numAttempts = -1;

                            //check if TextField ist empty
                            if (attempts.length() > 0) {

                                //check if number > 0. If not, number will be set to infinite attempts
                                if (Long.parseLong(attempts) > 0) {
                                    numAttempts = Long.parseLong(attempts);
                                }
                            }

                            Mission mission = new Mission(question, answer, multi, numAttempts);

                            wp.setUnlocking(multi);

                            mDatabase.child("Stations").child("Tour" + tourID)
                                    .child(stationID).child("Mission").setValue(mission);
                        }


                    } else
                        Toast.makeText(this, "Station konnte nicht gespeichert werden", Toast.LENGTH_LONG).show();
                }

                break;

            case REQUEST_IMAGE_CAPTURE:

                if (resultCode == RESULT_OK) {

                    LocationManager mLocationManager =
                            (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                    //Check Permissions for GPS Usage
                    //If permission is not granted, service can't be started.
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(this,
                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSION_ACCESS_FINE_LOCATION);

                    }

                    photoLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (photoLocation != null) {
                        Bundle extras = data.getExtras();
                        imageBitmap = (Bitmap) extras.get("data");

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        byte[] imageData = baos.toByteArray();

                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = timeStamp + "_" + user.getUid() + ".jpg";


                        Waypoint wp = new Waypoint(imageFileName, photoLocation.getLatitude(), photoLocation.getLongitude());
                        mDatabase.child("Waypoints").child("Tour" + tourID).push().setValue(wp);

                        UploadTask uploadTask = storageRef.child(imageFileName).putBytes(imageData);
                        uploadTask.addOnFailureListener(new OnFailureListener() {

                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        });


                    } else
                        Toast.makeText(this, "Foto konnte nicht gespeichert werden", Toast.LENGTH_LONG).show();
                }

                break;

        }


    }

    private void createNewTour(String tourName, String tourDescription, String authorName, boolean[] tags, long indexNr) {

        long timestamp = System.currentTimeMillis();

        Tour tour = new Tour(tourName, authorName, timestamp, tourID, tourDescription, indexNr);

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

    @Override
    public void onBackPressed() {

        if (!recording)
            super.onBackPressed();

        else
            Toast.makeText(this, getResources().getString(R.string.no_back_button), Toast.LENGTH_SHORT).show();
    }


    // On Click Listener
    @Override
    public void onClick(View v) {

        LocationManager mLocationManager;
        //Check Permissions for GPS Usage
        //If permission is not granted, service can't be started.
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);

        }

        mLocationManager =
                (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);


        switch (v.getId()) {

            case R.id.button_start_rec:

                recording = true;

                //Create new tour ID when Service is started
                tourID = mDatabase.child("Touren").push().getKey();

                findViewById(R.id.layout_rec_inactive).setVisibility(View.GONE);
                findViewById(R.id.layout_rec_active).setVisibility(View.VISIBLE);
                findViewById(R.id.textView).setVisibility(View.VISIBLE);

                Intent startIntent = new Intent(this, GPSService.class);
                startIntent.putExtra("TOURID", tourID);
                startService(startIntent);
                break;

            case R.id.button_stop_rec:

                recording = false;

                findViewById(R.id.layout_rec_inactive).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_rec_active).setVisibility(View.GONE);
                findViewById(R.id.textView).setVisibility(View.GONE);

                Intent stopIntent = new Intent(this, GPSService.class);
                stopService(stopIntent);

                Intent newTourIntent = new Intent(this, CreateNewTourActivity.class);
                startActivityForResult(newTourIntent, NEW_TOUR_REQUEST_CODE);

                break;


            case R.id.button_station:

                stationLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                Intent newStationIntent = new Intent(this, StationActivity.class);
                startActivityForResult(newStationIntent, NEW_STATION_REQUEST_CODE);

                break;

            case R.id.button_photo:

                photoLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

                }

                break;

            case R.id.button_comment:

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
                        findViewById(R.id.textView).setVisibility(View.GONE);


                        //If creating Tour is cancelled, existing data will be deleted
                        mDatabase.child("Waypoints").child("Tour" + tourID).removeValue();
                        mDatabase.child("Stations").child("Tour" + tourID).removeValue();
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
