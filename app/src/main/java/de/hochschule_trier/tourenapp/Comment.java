package de.hochschule_trier.tourenapp;

import android.os.Parcel;
import android.os.Parcelable;

public class Comment implements Parcelable{

    private String commentary;
    private long rating;
    private long timestamp;
    private String author;

    public Comment(){}

    public Comment(String commentary, long rating, long timestamp, String author){

        this.commentary = commentary;
        this.rating = rating;
        this.timestamp = timestamp;
        this.author = author;

    }

    public Comment(long rating, String author){
        this.rating = rating;
        this.author = author;
    }

    //Constructor from parcel
    public Comment(Parcel parcel) {
        this.commentary = parcel.readString();
        this.timestamp = parcel.readLong();
        this.author = parcel.readString();
        this.rating = parcel.readLong();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(timestamp);
        dest.writeString(commentary);
        dest.writeString(author);
        dest.writeLong(rating);

    }

    public static final Parcelable.Creator<Comment> CREATOR
            = new Parcelable.Creator<Comment>() {
        public Comment createFromParcel(Parcel in) {
            return new Comment(in);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };


    //Getters
    public String getCommentary() {
        return commentary;
    }

    public long getRating() {
        return rating;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getAuthor() {
        return author;
    }


    //Setters
    public void setCommentary(String commentary) {
        this.commentary = commentary;
    }

    public void setRating(long rating) {
        this.rating = rating;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
