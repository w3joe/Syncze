package com.t4g.location_tracking_application;
public class CurrentData {
    private double currentLat, currentLon;
    private boolean fallStatus;
    private PastLocation pastLocation;

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
}
