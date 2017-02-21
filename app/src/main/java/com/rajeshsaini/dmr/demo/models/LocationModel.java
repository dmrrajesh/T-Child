package com.rajeshsaini.dmr.demo.models;

import java.io.Serializable;

/**
 * Created by DMR on 2/20/2017.
 */

public class LocationModel implements Serializable {
    private String latitude;
    private String longitude;
    private String address;

    public String getLatitude() {
        if (latitude == null) {
            return "";
        }
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        if (longitude == null) {
            return "";
        }
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        if (address == null) {
            return "";
        }
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
