package de.hochschule_trier.tourenapp;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Waypoint> waypoints;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private String tourID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        WaypointWrapper wrapper = (WaypointWrapper) getIntent().getSerializableExtra("WPList");
        waypoints = wrapper.getWaypoints();

        tourID = getIntent().getStringExtra("TourID");

        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
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

        // Add waypoints and draw a polyline
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(false);



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

            if(waypoints.get(z).getStationID() != null){
                mMap.addCircle(new CircleOptions()
                    .center(new LatLng(
                            waypoints.get(z).getLatitude(),
                            waypoints.get(z).getLongitude()))
                    .radius(0.1)
                .strokeWidth(10)
                .strokeColor(Color.BLUE));

                final Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(
                                waypoints.get(z).getLatitude(),
                                waypoints.get(z).getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

                mDatabase.child("Stations").child("Tour"+tourID).child(waypoints.get(z).getStationID()).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        marker.setTitle(dataSnapshot.getValue(String.class));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        finish();
                    }
                });

            }

            //check if current Waypoint unlocks another part of the tour. if it does hide the rest of the tour.
            if(waypoints.get(z).isUnlocking())
                break;
        }

        mMap.addPolyline(options);

        // Move the camera to first waypoint and zoom in
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
                waypoints.get(0).getLatitude(),
                waypoints.get(0).getLongitude())));

        mMap.moveCamera(CameraUpdateFactory.zoomTo(17));
    }

}

