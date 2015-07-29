package com.peppermint.peppermint.net.handler;

import android.content.Context;
import android.util.Log;

import com.peppermint.peppermint.Config;
import com.peppermint.peppermint.model.User;
import com.peppermint.peppermint.net.callback.UserCallback;
import com.peppermint.peppermint.net.service.AuthenticationService;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static com.peppermint.peppermint.util.LogUtils.LOGI;

/**
 * Created by mark on 7/28/15.
 */
public class UserHandler {
    private RestAdapter restAdapter;
    private Context context;
    private UserCallback UsersCallback;
    private AuthenticationService UserService;

    public UserHandler(Context context) {
        this.context = context;
        RestAdapter.Builder builder = new RestAdapter.Builder().setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(Config.API);

        restAdapter = builder.build();

        UserService = restAdapter.create(AuthenticationService.class);
    }
    
    public  void setUsersCallback(UserCallback UsersCallback){
        this.UsersCallback = UsersCallback;
    }

    public void getUsers(){

        UserService.getUsers(new GetUsersCallback());

    }

    public void getUser(int id){

        UserService.getUser(id, new GetUserCallback());

    }

    public void registerUser(User user){

        UserService.regsiterUser(user, new RegisterUserCallback());

    }
    
    
    private class RegisterUserCallback implements Callback<User> {
        @Override
        public void success(User User, Response response) {
            UsersCallback.registerUserResponseReceived(User);
        //void registerUserResponseReceived(com.peppermint.peppermint.model.User user);
        //void getUserResponseReceived(com.peppermint.peppermint.model.User user);
        //void getUsersResponseReceived(List< com.peppermint.peppermint.model.User > users);
        }

        @Override
        public void failure(RetrofitError error) {

            Log.i("ERROR", "ERROR" + error.getMessage());
        }
    }

    private class GetUsersCallback implements Callback<List<User>> {
        @Override
        public void success(List<User> users, Response response) {

            UsersCallback.getUsersResponseReceived(users);
        }

        @Override
        public void failure(RetrofitError error) {

            Log.i("ERROR", "ERROR" + error.getMessage());
            //LOGI(this.class, "ERROR" + error.getMessage());
        }
    }
    private class GetUserCallback implements Callback<User> {
        @Override
        public void success(User User, Response response) {
            UsersCallback.getUserResponseReceived(User);

        }

        @Override
        public void failure(RetrofitError error) {

            Log.i("ERROR", "ERROR" + error.getMessage());
        }
    }
    
    
    
    }
