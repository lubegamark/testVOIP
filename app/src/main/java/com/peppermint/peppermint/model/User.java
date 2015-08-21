package com.peppermint.peppermint.model;

/**
 * Created by mark on 7/28/15.
 */
public class User {
    private int id;
    private String username;
    private String email;
    private String password;

    public User(int id, String email, String username) {
        this.email = email;
        this.id = id;
        this.username = username;
    }

    public User(String email,  String username) {
        this.email = email;
        this.username = username;
    }

    public User(String email,  String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
