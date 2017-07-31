package de.hochschule_trier.tourenapp;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static android.R.id.list;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Waypoint> waypoints;


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
        for (int z = 0; z < waypoints.size(); z++) {

            options.add(new LatLng(
                    waypoints.get(z).getLatitude(),
                    waypoints.get(z).getLongitude()));
        }

        mMap.addPolyline(options);

        // Move the camera to first waypoint and zoom in
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
                waypoints.get(0).getLatitude(),
                waypoints.get(0).getLongitude())));

        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    }
}

