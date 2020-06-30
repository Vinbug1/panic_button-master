package com.example.ubfac.panicbutton.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Emem on 10/31/18.
 */
public class PollingUnit {
    String latitude;
    String longitude;
    String name;
    @SerializedName("desc")
    String description;
    double distance;


    public PollingUnit() {
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "PollingUnit{" +
                "description='" + description + '\'' +
                ", distance=" + distance +
                '}';
    }
}
