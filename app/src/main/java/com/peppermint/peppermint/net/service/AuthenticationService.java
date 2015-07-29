package com.peppermint.peppermint.net.service;

import com.peppermint.peppermint.model.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by mark on 7/28/15.
 */
public interface AuthenticationService {
    @GET("/users")
    void getUsers(Callback<List<User>> users);

    @GET("/users/{id}")
    void getUser(@Path("user_id") int user_id,Callback<User> user);

    @POST("/users/")
    void regsiterUser(@Body User user, Callback<User> userCallBack);

}
