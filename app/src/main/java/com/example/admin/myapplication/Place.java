package com.example.admin.myapplication;

public class Place { // public class used to display markers on fixed locations based on JSON
    public String dish;
    public Double latitude;
    public Double longitude;

    public Place() {}
    public Place(String dish, Boolean eaten, Double latitude, Double longitude) {
        this.dish = dish;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDish() {return dish;}

    public Double getLat(){return latitude;}
    public Double getLong() {return longitude;}
}
