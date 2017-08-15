package de.hochschule_trier.tourenapp;

public class Station {

    private String name;
    private String description;

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


    //Setters
    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

}
