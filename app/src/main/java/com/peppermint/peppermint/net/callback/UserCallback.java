package com.peppermint.peppermint.net.callback;

import com.peppermint.peppermint.model.User;

import java.util.List;

/**
 * Created by mark on 7/28/15.
 */
public interface UserCallback {
    void registerUserResponseReceived(User user);
    void getUserResponseReceived(User user);
    void getUsersResponseReceived(List<User> users);

}
