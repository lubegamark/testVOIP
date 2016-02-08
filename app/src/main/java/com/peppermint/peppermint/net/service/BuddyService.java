package com.peppermint.peppermint.net.service;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;

/**
 * Created by mark on 7/28/15.
 */
public interface BuddyService {
    @GET("/peers")
    void getBuddies(Callback<List<String>> buddies);

}
