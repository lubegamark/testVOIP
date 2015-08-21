package com.peppermint.peppermint.net.service;

import com.peppermint.peppermint.model.Network;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by mark on 7/28/15.
 */
public interface NetworkService {
    @GET("/networks/")
    void getNetworks(Callback<List<Network>> networks);

    @GET("/networks/{network_id}")
    void getNetwork(@Path("network_id") int network_id, Callback<Network> network);


    @POST("/networks/")
    void regsiterNetwork(@Body Network network, Callback<Network> networkCallBack);

}
