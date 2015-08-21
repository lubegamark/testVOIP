package com.peppermint.peppermint.net.service;

import com.peppermint.peppermint.model.Subscription;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by mark on 7/28/15.
 */
public interface SubscriptionService {
    @GET("/users/{user_id}/subscriptions/")
    void getSubscriptions(@Path("user_id") int user_id, Callback<List<Subscription>> subscriptions);

    @GET("/users/{user_id}/subscriptions/{subscription_id}")
    void getSubscription(@Path("user_id") int user_id, @Path("subscription_id") int subscription_id, Callback<Subscription> subscription);

    @POST("/users/{user_id}/subscriptions/{subscription_id}")
    void registerSubscription(@Path("user_id") int user_id, @Path("subscription_id") int subscription_id, Callback<Subscription> subscriptionCallBack);

}
