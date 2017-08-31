package de.hochschule_trier.tourenapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private final static int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private final static int SEARCH_REQUEST_CODE = 4;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    private Location currentLocation;
    private Waypoint waypoint;
    private FusedLocationProviderClient mFusedLocationClient;

    private LocationListener locationListener;
    private LocationManager locationManager;


    //Database Snapshot Array List
    private static ArrayList<Tour> touren;
    private static ArrayList<Tour> resultList;
    private Tour tour;

    private int radius;
    private EditText editRadius;

    private String orderBy;
    private String direction;
    private ArrayList<String> tags;

    private boolean used;

    private TourNameAdapter tourNameAdapter;
    private ListView listView;

    private LinearLayout loadingLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        // Current Firebase User
        user = FirebaseAuth.getInstance().getCurrentUser();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Buttons
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.new_tour_button).setOnClickListener(this);
        findViewById(R.id.refresh_button).setOnClickListener(this);
        findViewById(R.id.searchTextView).setOnClickListener(this);

        // Text Field
        editRadius = (EditText) findViewById(R.id.radiusText);

        loadingLayout = (LinearLayout) findViewById(R.id.loadingLayout);

        touren = new ArrayList<>();
        resultList = new ArrayList<>();
        tags = new ArrayList<>();


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
            tags = savedInstanceState.getStringArrayList("Tags");
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

            loadingLayout.setVisibility(View.VISIBLE);
            radius = Integer.parseInt(editRadius.getText().toString()) * 1000;
            checkLocation();
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
        outState.putStringArrayList("Tags", tags);

        super.onSaveInstanceState(outState);
    }


    //Add new user data to the database
    private void createNewUser() {

        String name = user.getDisplayName();
        User newUser = new User(name, System.currentTimeMillis());


        mDatabase.child("Users").child(user.getUid()).setValue(newUser);

    }


    public void checkLocation() {

        //Check Permissions for GPS Usage
        //If permission is not granted, request Permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);

        } else {

            //If Permission is already granted, check the current location;


            //noinspection MissingPermission
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                currentLocation = location;
                                loadDatabase(radius, orderBy, direction, tags);

                            }
                            //if no last location is known, keep checking for location updates
                            else {

                                locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                                locationListener = new LocationListener() {

                                    @Override
                                    public void onLocationChanged(android.location.Location location) {
                                        double latitude = location.getLatitude();
                                        double longitude = location.getLongitude();
                                        String msg = "  Latitude: " + latitude + " Longitude: " + longitude + " Accuracy: " + location.getAccuracy();
                                        Log.d(TAG, msg);

                                        if (location.getAccuracy() < 20) {
                                            currentLocation = location;
                                            locationManager.removeUpdates(locationListener);
                                            locationManager = null;
                                            loadDatabase(radius, orderBy, direction, tags);
                                            Log.d(TAG, "Stopped tracking");
                                        }

                                    }

                                    @Override
                                    public void onStatusChanged(String provider, int status, Bundle extras) {

                                    }

                                    @Override
                                    public void onProviderEnabled(String provider) {

                                    }

                                    @Override
                                    public void onProviderDisabled(String provider) {

                                    }
                                };


                                //noinspection MissingPermission
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                        1000,
                                        0, locationListener);
                                //noinspection MissingPermission
                                locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,
                                        1000,
                                        0, locationListener);
                                //noinspection MissingPermission
                                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                        1000,
                                        0, locationListener);


                            }
                        }
                    });

        }
    }


    public void loadDatabase(int r, final String orderBy, final String direction, final ArrayList<String> tags) {

        tourNameAdapter.clear();
        loadingLayout.setVisibility(View.VISIBLE);


        radius = r;

        if (currentLocation != null) {
            long index = TourIndex.getIndex(currentLocation);
        }


        // Read from the database
        mDatabase.child("Touren").orderByChild(orderBy).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                tourNameAdapter.clear();
                touren.clear();
                resultList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    tour = snapshot.getValue(Tour.class);

                    boolean matching = true;

                    for (int i = 0; i < tags.size(); i++) {

                        if (tags.get(i).length() > 0) {

                            boolean b = snapshot.child("Tags").child(tags.get(i)).getValue(Boolean.class);
                            if (!b) {
                                matching = false;
                                break;
                            }

                        }

                    }

                    if (matching)
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

                                    if (currentLocation != null) {
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
                                    }


                                    loadingLayout.setVisibility(View.GONE);
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

                    String r = data.getStringExtra("Radius");
                    if (r.length() <= 0) {
                        r = "10";
                    }
                    radius = Integer.parseInt(r) * 1000;
                    direction = data.getStringExtra("Direction");
                    orderBy = data.getStringExtra("OrderBy");
                    tags = data.getStringArrayListExtra("TAGS");

                    editRadius.setText("" + r);
                    checkLocation();
                }


        }


    }


    // On Click Listener
    @Override
    public void onClick(View v) {

        String r;
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
                r = editRadius.getText().toString();
                if(r.length() <= 0){
                    r = "10";
                    editRadius.setText("10");
                }
                radius = Integer.parseInt(r) * 1000;
                checkLocation();
                break;

            case R.id.searchTextView:

                Intent searchIntent = new Intent(this, SearchActivity.class);
                r = editRadius.getText().toString();
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    checkLocation();


                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }
        }
    }
}