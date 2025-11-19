package com.alexwan.csci310team36projectd.data;

public class Location {
    public double latitude;
    public double longitude;
    public String name;
    public float radius; // Added for geofence reminders

    public Location() {
        // No-arg constructor required by Room for embedded objects
    }

    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = ""; // Default name
        this.radius = 100; // Default radius in meters
    }

    public Location(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.radius = 100; // Default radius in meters
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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public static double distanceBetween (double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000.0; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) *
                        Math.sin(dLon / 2);

        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
