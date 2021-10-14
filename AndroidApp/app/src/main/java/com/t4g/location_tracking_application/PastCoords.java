package com.t4g.location_tracking_application;

import com.google.gson.annotations.SerializedName;

public class PastCoords {
    @SerializedName("0")
    private Double lat;
    @SerializedName("1")
    private Double lon;
    @SerializedName("2")
    private String timestamp;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
