package com.system.model;

public class Ambulance {
    private final String id;
    private final AmbulanceType type;
    private AmbulanceState state;
    private final String driverDetails;
    private double currentLongitude;
    private double currentLatitude;

    public Ambulance(String id, AmbulanceType type, String driverDetails, double currentLatitude, double currentLongitude) {
        this.id = id;
        this.type = type;
        this.driverDetails = driverDetails;
        this.state = AmbulanceState.AVAILABLE;
        this.currentLatitude = currentLatitude;
        this.currentLongitude = currentLongitude;
    }

    public String getId() { return id; }
    public AmbulanceType getType() { return type; }
    public AmbulanceState getState() { return state; }
    public void setState(AmbulanceState state) { this.state = state; }
    public String getDriverDetails() { return driverDetails; }
    public double getCurrentLatitude() { return currentLatitude; }
    public double getCurrentLongitude() { return currentLongitude; }
    public void setCoordinates(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
    }
}

