package com.peppermint.peppermint.net.handler;

import android.content.Context;
import android.util.Log;

import com.peppermint.peppermint.net.callback.BuddyCallback;
import com.peppermint.peppermint.net.service.BuddyService;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by mark on 7/28/15.
 */
public class BuddyHandler {
    private RestAdapter restAdapter;
    private Context context;
    private BuddyCallback buddyCallback;
    private BuddyService buddyService;

    public BuddyHandler(Context context, String api) {
        this.context = context;
        RestAdapter.Builder builder = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(api);

        restAdapter = builder.build();

        buddyService = restAdapter.create(BuddyService.class);
    }
    
    public  void setBuddyCallback(BuddyCallback buddyCallback){
        this.buddyCallback = buddyCallback;
    }

    public void geBuddies(){

        buddyService.getBuddies(new GetBuddiesCallback());

    }


    private class GetBuddiesCallback implements Callback<List<String>> {
        @Override
        public void success(List<String> buddies, Response response) {
            buddyCallback.getBudddiesResponseReceived(buddies);
        }

        @Override
        public void failure(RetrofitError error) {

            Log.i("ERROR", "ERROR" + error.getMessage());
        }
    }
    
    }