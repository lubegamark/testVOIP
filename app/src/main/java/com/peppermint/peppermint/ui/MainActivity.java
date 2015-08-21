package com.peppermint.peppermint.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.peppermint.peppermint.model.Network;
import com.peppermint.peppermint.model.Subscription;
import com.peppermint.peppermint.net.callback.NetworkCallback;
import com.peppermint.peppermint.net.callback.SubscriptionCallback;
import com.peppermint.peppermint.net.handler.NetworkHandler;
import com.peppermint.peppermint.net.handler.SubscriptionHandler;

import java.util.List;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;

public class MainActivity extends Activity implements NetworkCallback, SubscriptionCallback {
    private static final String TAG = makeLogTag(MainActivity.class);

    WifiManager wifiManager;
    WifiConfiguration wifiConfiguration;
    String wifissids[];
    String wifibssids[];
    List<Network> networksList;
    NetworkHandler networkHandler;
    SubscriptionHandler subscriptionHandler;
    List<ScanResult> wifiScanList;
    Intent openStartingPoint;
    AccountManager am;
    Account account;
    @Override
	protected void onCreate(Bundle Splash) {
		super.onCreate(Splash);
		//setContentView(R.layout.splash);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        wifiScanList = wifiManager.getScanResults();

        wifissids = new String[wifiScanList.size()];
        wifibssids = new String[wifiScanList.size()];
		am= (AccountManager) MainActivity.this.getSystemService(Context.ACCOUNT_SERVICE);
        Account [] accounts = am.getAccountsByType("com.peppermint.peppermint");
        account =null;
        if (accounts.length > 0) {
        account = accounts[0];
        Log.i("Account Name", account.name);
        }
               
        Intent openStartingPoint;   
        if (account == null) {
           	openStartingPoint = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(openStartingPoint);
           }else{
            networkHandler = new NetworkHandler(this);
            networkHandler.setNetworkCallback(this);
            networkHandler.getNetworks();

           	//openStartingPoint = new Intent(MainActivity.this, UserListActivity.class);
           }



	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.mevibe, menu);
		return true;
	}

    @Override
    public void registerNetworkResponseReceived(Network network) {

    }

    @Override
    public void getNetworkResponseReceived(Network network) {
        wifiConfiguration = new WifiConfiguration();
        LOGD(TAG, network.getSSID());
        wifiConfiguration.SSID = "\"" + network.getSSID() + "\"";
        wifiConfiguration.preSharedKey = "\"" + network.getPassphrase() + "\"";
        wifiManager.addNetwork(wifiConfiguration);


        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for( WifiConfiguration i : list ) {
            if(i.SSID != null && i.SSID.equals("\"" + network.getSSID() + "\"")) {


                int callserver = network.getCallserver();

                if(account!=null) {
                    int id = Integer.parseInt(am.getUserData(account, "id"));

                    subscriptionHandler = new SubscriptionHandler(this);
                    subscriptionHandler.setSubscriptionCallback(this);
                    subscriptionHandler.registerSubscription(id, callserver);
                    break;
                }
                //wifiManager.disconnect();
                //LOGD(TAG, "Disconnected from network");
                //wifiManager.enableNetwork(i.networkId, true);
                //LOGD(TAG, "Enabled network");
                //wifiManager.reconnect();
                //LOGD(TAG, "Connected to network");
            }
        }
    }

    @Override
    public void getNetworksResponseReceived(List<Network> networks) {
        LOGD(TAG, "Hsdfmsdof odfsid found");

            for(int i =0; i< wifiScanList.size(); i++) {

            wifissids[i] = (wifiScanList.get(i).SSID);
                wifibssids[i] = (wifiScanList.get(i).BSSID);

        }
        Network matchedNetwork = null;

        for(int i= 0; i< networks.size(); i++) {
            for (int j = 0; j < wifissids.length; j++) {
                LOGD(TAG, wifissids[j] +" "+ wifibssids[j]);
                if (networks.get(i).getSSID().equals(wifissids[j])){
                    matchedNetwork = networks.get(i);
                    //LOGD(TAG, matchedNetwork.getSSID()+" found");
                    //LOGD(TAG, "BSSID "+networks.get(i).getBSSID()+ " vs "+wifibssids[j]);

                    if(matchedNetwork.getBSSID().equals(wifibssids[j])){
                        //LOGD(TAG, "BSSID "+networks.get(i).getBSSID()+" equal ");
                        networkHandler.getNetwork(matchedNetwork.getId());
                        LOGD(TAG, "Network found");
                    }


                }

                LOGD(TAG, "No network found");

            }
        }

    }

    @Override
    public void registerSubscriptionResponseReceived(Subscription subscription) {
        openStartingPoint = new Intent(MainActivity.this, UserListActivity.class);
        openStartingPoint.putExtra("sipUsername", am.getUserData(account, "username"));
        openStartingPoint.putExtra("sipDomain", subscription.getLocal_ip());
        openStartingPoint.putExtra("sipPassword", subscription.getPassword());
        LOGD(TAG, "Subscription Registered");
        openStartingPoint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(openStartingPoint);

    }

    @Override
    public void getSubscriptionResponseReceived(Subscription subscription) {

    }

    @Override
    public void getSubscriptionsResponseReceived(List<Subscription> subscriptions) {

    }
}
