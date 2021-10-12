package com.t4g.location_tracking_application;

import java.util.ArrayList;

public class PastLocation {
private ArrayList<Double> PastCoordinates;
private String Test;

    public String getTest() {
        return Test;
    }

    public void setTest(String test) {
        Test = test;
    }

    public ArrayList<Double> getPastCoordinates() {
        return PastCoordinates;
    }

    public void setPastCoordinates(ArrayList<Double> pastCoordinates) {
        this.PastCoordinates = pastCoordinates;
    }
}
