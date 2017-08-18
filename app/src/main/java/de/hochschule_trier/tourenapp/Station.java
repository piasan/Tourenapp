package de.hochschule_trier.tourenapp;

public class Station {

    private String name;
    private String description;
    private String imageURL;
    private Mission mission;

    public Station(){}

    public Station (String name, String description){

        this.name = name;
        this.description = description;

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

}
