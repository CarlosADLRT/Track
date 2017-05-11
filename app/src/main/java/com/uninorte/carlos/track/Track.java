package com.uninorte.carlos.track;

/**
 * Created by carlos on 8/05/17.
 */

public class Track {
    String lat;
    String lon;

    public Track(String lat, String lon) {

        this.lat = lat;
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
}
