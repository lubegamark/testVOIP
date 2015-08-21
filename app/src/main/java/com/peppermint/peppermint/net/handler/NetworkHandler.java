package com.peppermint.peppermint.net.handler;

import android.content.Context;
import android.util.Log;

import com.peppermint.peppermint.Config;
import com.peppermint.peppermint.model.Network;
import com.peppermint.peppermint.net.callback.NetworkCallback;
import com.peppermint.peppermint.net.service.NetworkService;
import com.peppermint.peppermint.ui.UserListActivity;


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
public class NetworkHandler {

    private static final String TAG = makeLogTag(NetworkHandler.class);
    private RestAdapter restAdapter;
    private Context context;
    private NetworkCallback networksCallback;
    private NetworkService networkService;

    public NetworkHandler(Context context) {
        this.context = context;
        RestAdapter.Builder builder = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Config.API);

        restAdapter = builder.build();

        networkService = restAdapter.create(NetworkService.class);
    }
    
    public  void setNetworkCallback(NetworkCallback networkCallback){
        this.networksCallback = networkCallback;
    }

    public void getNetworks(){

        networkService.getNetworks(new GetNetworksCallback());

    }

    public void getNetwork(int id){

        networkService.getNetwork(id, new GetNetworkCallback());

    }

    public void registerNetwork(Network network){

        networkService.regsiterNetwork(network, new RegisterNetworkCallback());

    }
    
    
    private class RegisterNetworkCallback implements Callback<Network> {
        @Override
        public void success(Network Network, Response response) {
            networksCallback.registerNetworkResponseReceived(Network);
        //void registerNetworkResponseReceived(com.peppermint.peppermint.model.Network Network);
        //void getNetworkResponseReceived(com.peppermint.peppermint.model.Network Network);
        //void getNetworksResponseReceived(List< com.peppermint.peppermint.model.Network > Networks);
        }

        @Override
        public void failure(RetrofitError error) {

            Log.i("ERROR", "ERROR" + error.getMessage());
        }
    }

    private class GetNetworksCallback implements Callback<List<Network>> {
        @Override
        public void success(List<Network> networks, Response response) {
            //LOGD(TAG, "ERROR " + " Don't know yet");
            networksCallback.getNetworksResponseReceived(networks);
        }

        @Override
        public void failure(RetrofitError error) {

            LOGD(TAG, "ERROR " + error.getMessage());
            //LOGI(this.class, "ERROR" + error.getMessage());
        }
    }
    private class GetNetworkCallback implements Callback<Network> {
        @Override
        public void success(Network network, Response response) {
            networksCallback.getNetworkResponseReceived(network);

        }

        @Override
        public void failure(RetrofitError error) {

            Log.i("ERROR", "ERROR" + error.getMessage());
        }
    }
    
    
    
    }
