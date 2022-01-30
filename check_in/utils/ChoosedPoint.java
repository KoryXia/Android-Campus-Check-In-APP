package com.example.check_in.utils;

import com.amap.api.services.core.LatLonPoint;

import java.io.Serializable;

public class ChoosedPoint implements Serializable {
    private double latitude;
    private double longitude;
    private String locationName;

    public ChoosedPoint() {
    }

    public ChoosedPoint(double latitude, double longitude, String locationName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @Override
    public String toString() {
        return "ChoosedPoint{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", locationName='" + locationName + '\'' +
                '}';
    }
}
