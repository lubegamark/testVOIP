package com.peppermint.peppermint.model;

/**
 * Created by mark on 7/28/15.
 */
public class Subscription {

    private int id;
    private int user;
    private int call_server;
    private String first_registered;
    private String started;
    private String ended;
    private String password;
    private String local_ip;



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser() {
        return user;
    }

    public void setUser(int user) {
        this.user = user;
    }

    public int getCall_server() {
        return call_server;
    }

    public void setCall_server(int call_server) {
        this.call_server = call_server;
    }

    public String getFirst_registered() {
        return first_registered;
    }

    public void setFirst_registered(String first_registered) {
        this.first_registered = first_registered;
    }

    public String getStarted() {
        return started;
    }

    public void setStarted(String started) {
        this.started = started;
    }

    public String getEnded() {
        return ended;
    }

    public void setEnded(String ended) {
        this.ended = ended;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocal_ip() {
        return local_ip;
    }

    public void setLocal_ip(String local_ip) {
        this.local_ip = local_ip;
    }
}
