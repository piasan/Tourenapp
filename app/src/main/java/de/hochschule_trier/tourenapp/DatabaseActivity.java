package de.hochschule_trier.tourenapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DatabaseActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private final static String TAG = "DatabaseActivity";
    private final static int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private Location currentLocation;
    private Waypoint waypoint;

    //Database Snapshot Array List
    private static ArrayList<Tour> touren;

    private CustomAdapter tourNameAdapter;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        // Current Firebase User
        user = FirebaseAuth.getInstance().getCurrentUser();


        // Buttons
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.new_tour_button).setOnClickListener(this);

        touren = new ArrayList<>();

        // Set up ListView and Adapter
        tourNameAdapter = new CustomAdapter(touren, this);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(tourNameAdapter);
        listView.setOnItemClickListener(this);


        // Get an instance of the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();


        //Read User Data
        mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Check if current User already exists.
                // If not, create new database entry for this user
                User currentUser = dataSnapshot.child(user.getUid()).getValue(User.class);

                if (currentUser == null) {
                    createNewUser();
                } else {
                    mDatabase.child("Users").child(user.getUid()).child("lastLogin").setValue(System.currentTimeMillis());
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                finish();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        loadDatabase();

    }


    //Add new user data to the database
    private void createNewUser() {

        String name = user.getDisplayName();
        User newUser = new User(name, System.currentTimeMillis());


        mDatabase.child("Users").child(user.getUid()).setValue(newUser);

    }

    public void loadDatabase() {

        //Check Permissions for GPS Usage
        //If permission is not granted, service can't be started.
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION )
                != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_ACCESS_COARSE_LOCATION);

        }

        LocationManager mLocationManager =
                (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        long index = TourIndex.getIndex(currentLocation);



        // Read from the database
        mDatabase.child("Touren").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                tourNameAdapter.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Tour tour = snapshot.getValue(Tour.class);
                    String tourID = tour.getTourID();

                    touren.add(tour);
                }

                tourNameAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
                finish();
            }


        });


        /*
        for (int i = 0; i < touren.size(); i++) {

            final int j = i;
            String tourID = touren.get(i).getTourID();

            //Get first waypoint
            mDatabase.child("Waypoints").child("Tour" + tourID).limitToFirst(1).
                    addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            waypoint = new Waypoint(0, 0);


                            for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                                waypoint = snapshot1.getValue(Waypoint.class);
                            }

                            //Check distance to tour from currentLocation
                            Location loc = new Location("WP");
                            loc.setLatitude(waypoint.getLatitude());
                            loc.setLongitude(waypoint.getLongitude());

                            //add distance to tour
                            double dist = loc.distanceTo(currentLocation);
                            touren.get(j).setDistance(dist);

                            tourNameAdapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Failed to read value
                            Log.w(TAG, "Failed to read value.", error.toException());
                            finish();
                        }

                    });

        }*/


    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    // On Click Listener
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.new_tour_button:

                Intent newTourIntent = new Intent(this, RecordTourActivity.class);
                startActivity(newTourIntent);

                break;

            case R.id.sign_out_button:

                Intent signOutIntent = new Intent(this, SignInActivity.class);
                String message = "signout";

                signOutIntent.putExtra("EXTRA_MESSAGE", message);
                startActivity(signOutIntent);
                break;


        }
    }


    //OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Tour tour = tourNameAdapter.getItem(position);
        String tourID = tour.getTourID();
        Log.d(TAG, tourID);

        Intent TourDetailIntent = new Intent(this, TourDetailActivity.class);
        TourDetailIntent.putExtra("TourID", tourID);
        startActivity(TourDetailIntent);
    }

}
