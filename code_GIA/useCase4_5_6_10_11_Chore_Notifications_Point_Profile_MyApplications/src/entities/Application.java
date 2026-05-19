package entities;

import javafx.collections.ObservableList;

public class Application {
    private String title;
    private String location;
    private String houseAddress;
    private double rent;
    private int roommatesWanted;
    private String description;
    private String status;

    public Application(String title, String location, String houseAddress, double rent, int roommatesWanted, String description, String status) {
        this.title = title;
        this.location = location;
        this.houseAddress = houseAddress;
        this.rent = rent;
        this.roommatesWanted = roommatesWanted;
        this.description = description;
        this.status = status;
    }

    public void saveApplication(ObservableList<Application> db) {
        db.add(this);
    }

    public void cancelApplication(ObservableList<Application> db) {
        db.remove(this);
    }

    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public double getRent() { return rent; }
    public int getRoommatesWanted() { return roommatesWanted; }
    public String getStatus() { return status; }
}