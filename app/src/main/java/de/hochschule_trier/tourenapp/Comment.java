package de.hochschule_trier.tourenapp;

public class Comment {

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
