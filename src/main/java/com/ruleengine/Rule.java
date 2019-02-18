package com.ruleengine;

import java.util.Set;

enum Gender {
     m,
     f
}

public class Rule {

    private String id;
    private Gender userGender;
    private Set<String> geoCountry;
    private double lat;
    private double lon;
    private double radius;
    private Set<String> appCategory;
    private Set<String> deviceModel;
    private Set<Integer> hourOfDay;

    public Rule(String id, Gender userGender, Set<String> geoCountry, double lat,
                double lon, double radius, Set<String> appCategory,
                Set<String> deviceModel, Set<Integer> hourOfDay) {
        this.id = id;
        this.userGender = userGender;
        this.geoCountry = geoCountry;
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.appCategory = appCategory;
        this.deviceModel = deviceModel;
        this.hourOfDay = hourOfDay;
    }

    public String getId() {
        return id;
    }

    public Gender getUserGender() {
        return userGender;
    }

    public Set<String> getGeoCountry() {
        return geoCountry;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lon;
    }

    public double getRadius() {
        return radius;
    }

    public Set<String> getAppCategory() {
        return appCategory;
    }

    public Set<String> getDeviceModel() {
        return deviceModel;
    }

    public Set<Integer> getHourOfDay() {
        return hourOfDay;
    }


}