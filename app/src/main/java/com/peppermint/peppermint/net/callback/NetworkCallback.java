package com.peppermint.peppermint.net.callback;

import com.peppermint.peppermint.model.Network;
import com.peppermint.peppermint.model.Network;

import java.util.List;

/**
 * Created by mark on 7/28/15.
 */
public interface NetworkCallback {
    void registerNetworkResponseReceived(Network network);
    void getNetworkResponseReceived(Network network);
    void getNetworksResponseReceived(List<Network> networks);

}
