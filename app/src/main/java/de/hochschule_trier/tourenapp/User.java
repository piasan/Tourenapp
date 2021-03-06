package de.hochschule_trier.tourenapp;

import java.util.Map;

class User {

    private String name;
    private String status;
    private long registrationDate;
    private long lastLogin;
    private Map<String, Double> finishedTours;

    public User() {
    }

    User(String name, long registrationDate){

        this.name = name;
        this.status = "User";
        this.registrationDate = registrationDate;
        this.lastLogin = registrationDate;

    }


    //Getters
    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public long getRegistrationDate() {
        return registrationDate;
    }

    public Map<String, Double> getFinishedTours() {
        return finishedTours;
    }

    public long getLastLogin(){
        return lastLogin;
    }


    //Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRegistrationDate(long registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setFinishedTours(Map<String, Double> finishedTours) {
        this.finishedTours = finishedTours;
    }

    public void setLastLogin(long lastLogin){
        this.lastLogin = lastLogin;
    }

}
