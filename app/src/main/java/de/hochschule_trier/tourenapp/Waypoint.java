package de.hochschule_trier.tourenapp;

import java.io.Serializable;

class Waypoint implements Serializable {

    private double latitude;
    private double longitude;
    private String comment;
    private String stationID;
    private String imageURL;
    private boolean unlocking;

    private static final long serialVersionUID = 1L;


    //Default Construktor
    public Waypoint() {
    }

    //Constructor for standard way points
    Waypoint(double latitude, double longitude) {

        this.latitude = latitude;
        this.longitude = longitude;

    }

    Waypoint(double latitude, double longitude, String comment) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.comment = comment;
    }

    Waypoint(double latitude, double longitude, String comment, String stationID, boolean unlocking) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.comment = comment;
        this.stationID = stationID;
        this.unlocking = unlocking;

    }

    Waypoint(String imageURL, double latitude, double longitude) {

        this.imageURL = imageURL;
        this.latitude = latitude;
        this.longitude = longitude;

    }


    //Getters
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getComment() {
        return comment;
    }

    public String getStationID() {
        return stationID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public boolean isUnlocking(){
        return unlocking;
    }


    //Setters
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setUnlocking(boolean unlocking){
        this.unlocking = unlocking;
    }
}
