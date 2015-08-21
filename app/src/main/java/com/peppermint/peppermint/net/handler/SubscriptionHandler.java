package com.peppermint.peppermint.net.handler;

import android.content.Context;
import android.util.Log;

import com.peppermint.peppermint.Config;
import com.peppermint.peppermint.model.Subscription;
import com.peppermint.peppermint.net.callback.SubscriptionCallback;
import com.peppermint.peppermint.net.service.SubscriptionService;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;

/**
 * Created by mark on 7/28/15.
 */
public class SubscriptionHandler {

    private static final String TAG = makeLogTag(SubscriptionHandler.class);
    private RestAdapter restAdapter;
    private Context context;
    private SubscriptionCallback subscriptionsCallback;
    private SubscriptionService subscriptionService;

    public SubscriptionHandler(Context context) {
        this.context = context;
        RestAdapter.Builder builder = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Config.API);

        restAdapter = builder.build();

        subscriptionService = restAdapter.create(SubscriptionService.class);
    }
    
    public  void setSubscriptionCallback(SubscriptionCallback subscriptionCallback){
        this.subscriptionsCallback = subscriptionCallback;
    }

    public void getSubscriptions(int id){

        subscriptionService.getSubscriptions(id, new GetSubscriptionsCallback());

    }

    public void getSubscription(int id, int callserver){

        subscriptionService.getSubscription(id, callserver, new GetSubscriptionCallback());

    }

    public void registerSubscription(int id, int callserver){

        subscriptionService.registerSubscription(id, callserver, new RegisterSubscriptionCallback());

    }
    
    private class RegisterSubscriptionCallback implements Callback<Subscription> {
        @Override
        public void success(Subscription subscription, Response response) {
            subscriptionsCallback.registerSubscriptionResponseReceived(subscription);
        }

        @Override
        public void failure(RetrofitError error) {

            Log.i("ERROR", "ERROR" + error.getMessage());
        }
    }

    private class GetSubscriptionsCallback implements Callback<List<Subscription>> {
        @Override
        public void success(List<Subscription> subscriptions, Response response) {
            //LOGD(TAG, "ERROR " + " Don't know yet");
            subscriptionsCallback.getSubscriptionsResponseReceived(subscriptions);
        }

        @Override
        public void failure(RetrofitError error) {

            LOGD(TAG, "ERROR " + error.getMessage());
            //LOGI(this.class, "ERROR" + error.getMessage());
        }
    }
    private class GetSubscriptionCallback implements Callback<Subscription> {
        @Override
        public void success(Subscription subscription, Response response) {
            subscriptionsCallback.getSubscriptionResponseReceived(subscription);

        }

        @Override
        public void failure(RetrofitError error) {

            Log.i("ERROR", "ERROR" + error.getMessage());
        }
    }
    
    
    
    }
