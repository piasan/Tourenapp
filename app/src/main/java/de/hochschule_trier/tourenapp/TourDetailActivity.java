package de.hochschule_trier.tourenapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class TourDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewTourName;
    private TextView textViewTourDescription;

    private ArrayList<Waypoint> waypoints;

    private DatabaseReference mDatabase;
    private String tourID;

    private boolean WPComplete;

    private static final String TAG = "TourDetailActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_detail);

        Intent intent = getIntent();
        tourID = intent.getStringExtra("TourID");

        textViewTourName = (TextView) findViewById(R.id.textViewTourName);
        textViewTourDescription = (TextView) findViewById(R.id.textViewTourDescription);
        findViewById(R.id.buttonMaps).setOnClickListener(this);


        // Get an instance of the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();


        //Read tour data from the database
        mDatabase.child("Touren").child(tourID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Tour tour = dataSnapshot.getValue(Tour.class);
                textViewTourName.setText(tour.getTourName());
                textViewTourDescription.setText(tour.getDescription());


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                finish();
            }
        });


        //Retrieve Waypoint Data
        mDatabase.child("Waypoints").child("Tour"+tourID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                waypoints = new ArrayList<Waypoint>();
                WPComplete = false;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Waypoint waypoint = snapshot.getValue(Waypoint.class);
                    waypoints.add(waypoint);

                }

                WPComplete = true;

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                finish();
            }
        });


    }







    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.buttonMaps:

                if (WPComplete) {

                    WaypointWrapper wrapper = new WaypointWrapper(waypoints);

                    Intent mapsIntent = new Intent(this, MapsActivity.class);
                    mapsIntent.putExtra("WPList", wrapper);
                    startActivity(mapsIntent);
                    break;

                }
        }


    }
}
