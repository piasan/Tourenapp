package de.hochschule_trier.tourenapp;

public class Station {

    private String name;
    private String description;
    private String type;
    private String missionID;


    public Station(){}

    public Station (String name, String description, String type){

        this.name = name;
        this.description = description;
        this.type = type;

    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getMissionID() {
        return missionID;
    }


    //Setters
    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMissionID(String missionID) {
        this.missionID = missionID;
    }
}
