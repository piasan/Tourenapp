package de.hochschule_trier.tourenapp;

import java.io.Serializable;

public class Station implements Serializable{

    private String name;
    private String description;
    private String imageURL;
    private Mission mission;
    private Waypoint waypoint;
    private String id;

    public Station(){}

    public Station (String name, String description, String id){

        this.name = name;
        this.description = description;
        this.id = id;

    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public Mission getMission(){
        return mission;
    }

    public Waypoint getWaypoint(){
        return waypoint;
    }

    public String getId(){
        return id;
    }

    //Setters
    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setMission (Mission mission){
        this.mission = mission;
    }

    public void setWaypoint (Waypoint wp){
        this.waypoint = wp;
    }

    public void setId(String id){
        this.id = id;
    }

}
