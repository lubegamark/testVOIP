package com.peppermint.peppermint.model;

/**
 * Created by mark on 7/28/15.
 */
public class Network {
    private int id;
    private String name;
    private String SSID;
    private String BSSID;
    private String address;
    private Float latitude;
    private Float longitude;
    private String passphrase;
    private int callserver;

    public int getCallserver() {
        return callserver;
    }

    public void setCallserver(int callserver) {
        this.callserver = callserver;
    }

    public Network(int id, String name, String SSID, String BSSID) {
        this.id = id;
        this.name = name;
        this.SSID = SSID;
        this.BSSID = BSSID;
    }

    public Network(int id, String name, String SSID, String BSSID, String address, Float latitude, Float longitude, String passphrase, int callserver) {
        this.id = id;
        this.name = name;
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.passphrase = passphrase;
        this.callserver = callserver;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }
}
