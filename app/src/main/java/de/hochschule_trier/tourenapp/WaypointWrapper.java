package de.hochschule_trier.tourenapp;

import java.io.Serializable;
import java.util.ArrayList;

public class WaypointWrapper implements Serializable{

    private static final long serialVersionUID = 1L;
    private ArrayList<Waypoint> waypoints;

    public WaypointWrapper(ArrayList<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }

    public ArrayList<Waypoint> getWaypoints() {
        return waypoints;
    }
}
