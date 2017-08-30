package de.hochschule_trier.tourenapp;

import java.io.Serializable;
import java.util.ArrayList;

public class StationWrapper implements Serializable

{
    private static final long serialVersionUID = 1L;
    private ArrayList<Station> stations;

    public StationWrapper(ArrayList<Station> stations) {
        this.stations = stations;
    }

    public ArrayList<Station> getStations() {
        return stations;
    }
}
