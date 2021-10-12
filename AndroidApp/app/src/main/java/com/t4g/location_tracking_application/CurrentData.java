package com.t4g.location_tracking_application;

import java.util.ArrayList;

public class CurrentData {
    private double currentLat, currentLon;
    private boolean fallStatus;
    private ArrayList<ArrayList<Double>> pastLocation;

    public double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public double getCurrentLon() {
        return currentLon;
    }

    public void setCurrentLon(double currentLon) {
        this.currentLon = currentLon;
    }

    public boolean isFallStatus() {
        return fallStatus;
    }

    public void setFallStatus(boolean fallStatus) {
        this.fallStatus = fallStatus;
    }

    public ArrayList<ArrayList<Double>> getPastLocation() {
        return pastLocation;
    }

    public void setPastLocation(ArrayList<ArrayList<Double>> pastLocation) {
        this.pastLocation = pastLocation;
    }
}
