package com.ruleengine;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class BidRequest {
    private long id;
    private Gender userGender;
    private String geoCountry;
    private String appCategory;
    private double lat;
    private double lon;
    private String deviceModel;
    private long eventTs;
    private static AtomicInteger ctr = new AtomicInteger(0);

    public BidRequest(long id, Gender userGender, String geoCountry, String appCategory,
                      double lat, double lon,
                      String deviceModel, long eventTs) {
        this.id = id;
        this.userGender = userGender;
        this.geoCountry = geoCountry;
        this.appCategory = appCategory;
        this.lat = lat;
        this.lon = lon;
        this.deviceModel = deviceModel;
        this.eventTs = eventTs;
    }

    public long getId() {
        return id;
    }

    public Gender getUserGender() {
        return userGender;
    }

    public String getGeoCountry() {
        return geoCountry;
    }

    public double getLatitude() {
        return lat;
    }

    public double getLongitude() {
        return lon;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public long getEventTimestamp() {
        return eventTs;
    }

    public String getAppCategory() {
        return appCategory;
    }


    public static BidRequest parseBidRequest(File bidRequestFile) throws Exception {
        JsonNode bidRequest = new ObjectMapper().
                readTree(bidRequestFile);
        Gender userGender = Gender.valueOf(Gender.class,
                bidRequest.get("user").get("gender").textValue());
        String geoCountry = bidRequest.get("geo").get("country").textValue();
        double lat = Double.parseDouble(bidRequest.get("geo").get("lat").textValue());
        double lon = Double.parseDouble(bidRequest.get("geo").get("lon").textValue());
        String deviceModel = bidRequest.get("device").get("model").textValue();
        String appCategory = bidRequest.get("app").get("cat").textValue();
        long eventTs = Long.parseLong(bidRequest.get("eventTs").textValue());
        return new BidRequest(ctr.getAndIncrement(), userGender, geoCountry, appCategory, lat, lon, deviceModel, eventTs);
    }



}
