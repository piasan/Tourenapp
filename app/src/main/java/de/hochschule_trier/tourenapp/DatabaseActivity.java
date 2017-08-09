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
import android.widget.EditText;
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
    private final static int SEARCH_REQUEST_CODE = 4;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private Location currentLocation;
    private Waypoint waypoint;

    //Database Snapshot Array List
    private static ArrayList<Tour> touren;
    private static ArrayList<Tour> resultList;
    private Tour tour;

    private int radius;
    private EditText editRadius;

    private String orderBy;
    private String direction;
    private String tourName;
    private String authorName;

    private boolean used;

    private TourNameAdapter tourNameAdapter;
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
        findViewById(R.id.refresh_button).setOnClickListener(this);
        findViewById(R.id.searchTextView).setOnClickListener(this);

        // Text Field
        editRadius = (EditText) findViewById(R.id.radiusText);

        touren = new ArrayList<>();
        resultList = new ArrayList<>();


        // Get an instance of the database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();


        //Read User Data
        mDatabase.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Check if current User already exists.
                // If not, create new database entry for this user
                User currentUser = dataSnapshot.getValue(User.class);

                if (currentUser == null) {
                    createNewUser();
                } else {
                    //if User exists, update last Login
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


        if (savedInstanceState != null) {
            if (!used)
                resultList = savedInstanceState.getParcelableArrayList("Touren");
            orderBy = savedInstanceState.getString("OrderBy");
            direction = savedInstanceState.getString("Direction");
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        // Set up ListView and Adapter
        tourNameAdapter = new TourNameAdapter(resultList, this);

        listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(tourNameAdapter);
        listView.setOnItemClickListener(this);

        if (orderBy == null) {
            orderBy = "tourID";
        }
        if (direction == null) {
            direction = "ascending";
        }

        if (resultList.size() > 0) {
            tourNameAdapter.notifyDataSetChanged();
            used = true;
        } else {

            radius = Integer.parseInt(editRadius.getText().toString()) * 1000;
            loadDatabase(radius, orderBy, direction, tourName, authorName);
        }


    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("Touren", resultList);
        outState.putBoolean("saved", true);
        outState.putString("OrderBy", orderBy);
        outState.putString("Direction", direction);

        super.onSaveInstanceState(outState);
    }


    //Add new user data to the database
    private void createNewUser() {

        String name = user.getDisplayName();
        User newUser = new User(name, System.currentTimeMillis());


        mDatabase.child("Users").child(user.getUid()).setValue(newUser);

    }

    public void loadDatabase(int r, final String orderBy, final String direction, String tourName, String authorName) {

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

        while (currentLocation == null)
            currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        radius = r;

        long index = TourIndex.getIndex(currentLocation);


        // Read from the database
        mDatabase.child("Touren").orderByChild(orderBy).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                tourNameAdapter.clear();
                touren.clear();
                resultList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    tour = snapshot.getValue(Tour.class);

                    touren.add(tour);
                }

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
                                    if (dist < radius) {

                                        touren.get(j).setDistance(dist);

                                        if (orderBy.equals("tourID")) {
                                            sortByDistance(resultList, touren.get(j));
                                        } else {
                                            if (direction.equals("descending"))
                                                resultList.add(0, touren.get(j));
                                            else
                                                resultList.add(touren.get(j));
                                        }
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

    public ArrayList<Tour> sortByDistance(ArrayList<Tour> list, Tour tour) {

        int i = 0;

        if (direction.equals("ascending")) {

            while (i < list.size() && tour.getDistance() > list.get(i).getDistance())
                i++;
        } else

            while (i < list.size() && tour.getDistance() < list.get(i).getDistance()) {
                i++;
            }


        list.add(i, tour);

        return list;
    }


    // Get message from other Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case SEARCH_REQUEST_CODE:

                if (resultCode == RESULT_OK) {

                    int r = data.getIntExtra("Radius", 10);
                    direction = data.getStringExtra("Direction");
                    orderBy = data.getStringExtra("OrderBy");
                    tourName = data.getStringExtra("TourName");
                    authorName = data.getStringExtra("AuthorName");

                    editRadius.setText("" + r);
                    loadDatabase(r * 1000, orderBy, direction, tourName, authorName);
                }


        }


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
                finish();
                break;

            case R.id.refresh_button:

                used = true;
                radius = Integer.parseInt(editRadius.getText().toString()) * 1000;
                loadDatabase(radius, orderBy, direction, tourName, authorName);
                break;

            case R.id.searchTextView:

                Intent searchIntent = new Intent(this, SearchActivity.class);
                String r = editRadius.getText().toString();
                searchIntent.putExtra("Radius", r);

                startActivityForResult(searchIntent, SEARCH_REQUEST_CODE);

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
