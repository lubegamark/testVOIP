package com.peppermint.peppermint.net.callback;

import com.peppermint.peppermint.model.Network;
import com.peppermint.peppermint.model.Subscription;

import java.util.List;

/**
 * Created by mark on 7/28/15.
 */
public interface SubscriptionCallback {
    void registerSubscriptionResponseReceived(Subscription subscription);
    void getSubscriptionResponseReceived(Subscription subscription);
    void getSubscriptionsResponseReceived(List<Subscription>  subscriptions);

}
