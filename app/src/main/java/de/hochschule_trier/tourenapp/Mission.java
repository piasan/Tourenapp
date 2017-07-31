package de.hochschule_trier.tourenapp;

public class Mission {

    private String description;
    private String solution;
    private boolean unlocking;
    private long attempts;


    public Mission(){}

    public Mission(String description, String solution, boolean unlocking, long attempts){

        this.description = description;
        this.solution = solution;
        this.unlocking = unlocking;
        this.attempts = attempts;

    }


    //Getters
    public String getDescription() {
        return description;
    }

    public String getSolution() {
        return solution;
    }

    public boolean isUnlocking() {
        return unlocking;
    }

    public long getAttempts() {
        return attempts;
    }


    //Setters
    public void setDescription(String description) {
        this.description = description;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public void setUnlocking(boolean unlocking) {
        this.unlocking = unlocking;
    }

    public void setAttempts(long attempts) {
        this.attempts = attempts;
    }
}
