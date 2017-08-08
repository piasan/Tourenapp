package de.hochschule_trier.tourenapp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Tour implements Parcelable {


    private String tourID;
    private String tourName;
    private String authorName;
    private double distance;
    private long timestamp;
    private long lastUpdate;
    private boolean active;
    private double averageRating;
    private String description;


    public Tour() {
    }

    //Constructor
    public Tour(String tourName, String authorName, long timestamp, String tourID, String tourDescription) {
        this.tourName = tourName;
        this.authorName = authorName;
        this.timestamp = timestamp;
        this.lastUpdate = timestamp;
        this.active = true;
        this.tourID = tourID;
        this.description = tourDescription;
    }

    //Constructor from parcel
    public Tour(Parcel parcel) {
        this.tourID = parcel.readString();
        this.tourName = parcel.readString();
        this.authorName = parcel.readString();
        this.distance = parcel.readDouble();
        this.timestamp = parcel.readLong();
        this.lastUpdate = parcel.readLong();
        this.active = parcel.readInt() != 0;
        this.averageRating = parcel.readDouble();
        this.description = parcel.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(tourID);
        dest.writeString(tourName);
        dest.writeString(authorName);
        dest.writeDouble(distance);
        dest.writeLong(timestamp);
        dest.writeLong(lastUpdate);
        dest.writeInt((active ? 1 : 0));
        dest.writeDouble(averageRating);
        dest.writeString(description);
    }

    public static final Parcelable.Creator<Tour> CREATOR
            = new Parcelable.Creator<Tour>() {
        public Tour createFromParcel(Parcel in) {
            return new Tour(in);
        }

        public Tour[] newArray(int size) {
            return new Tour[size];
        }
    };


    //Getters
    public String getAuthorName() {
        return authorName;
    }

    public String getTourName() {
        return tourName;
    }

    public double getDistance() {
        return distance;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public boolean isActive() {
        return active;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public String getTourID() {
        return tourID;
    }

    public String getDescription() {
        return description;
    }


    //Setters
    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public void setTourID(String tourID) {
        this.tourID = tourID;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}