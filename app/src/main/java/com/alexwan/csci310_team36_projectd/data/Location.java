package com.alexwan.csci310_team36_projectd.data;

public class Location {
    public double latitude;
    public double longitude;
    public String name;

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = ""; // Default name
    }

    public Location(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public void setLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
