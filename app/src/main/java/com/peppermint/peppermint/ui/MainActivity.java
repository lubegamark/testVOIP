package com.peppermint.peppermint.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.peppermint.peppermint.call.MyAccount;
import com.peppermint.peppermint.call.MyApp;
import com.peppermint.peppermint.call.MyAppObserver;
import com.peppermint.peppermint.call.MyBuddy;
import com.peppermint.peppermint.call.MyCall;
import com.peppermint.peppermint.model.Network;
import com.peppermint.peppermint.model.Subscription;
import com.peppermint.peppermint.net.callback.NetworkCallback;
import com.peppermint.peppermint.net.callback.SubscriptionCallback;
import com.peppermint.peppermint.net.handler.NetworkHandler;
import com.peppermint.peppermint.net.handler.SubscriptionHandler;
import com.peppermint.peppermint.util.AccountUtils;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;

public class MainActivity extends Activity implements NetworkCallback, SubscriptionCallback, Handler.Callback, MyAppObserver {
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
    Account androidAccount;


    public static MyApp app = null;
    public static MyCall currentCall = null;
    public static MyAccount account = null;
    public static AccountConfig accCfg = null;
    private ListView buddyListView;
    private SimpleAdapter buddyListAdapter;
    private int buddyListSelectedIdx = -1;
    ArrayList<Map<String, String>> buddyList;
    private String lastRegStatus = "";
    private final Handler handler = new Handler(this);
    public class MSG_TYPE
    {
        public final static int INCOMING_CALL = 1;
        public final static int CALL_STATE = 2;
        public final static int REG_STATE = 3;
        public final static int BUDDY_STATE = 4;
        public final static int CALL_MEDIA_STATE = 5;
    }

    private HashMap<String, String> putData(String uri, String status)
    {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("uri", uri);
        item.put("status", status);
        return item;
    }

    @Override
	protected void onCreate(Bundle Splash) {
		super.onCreate(Splash);
        System.loadLibrary("pjsua2");
		//setContentView(R.layout.splash);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        wifiScanList = wifiManager.getScanResults();

        wifissids = new String[wifiScanList.size()];
        wifibssids = new String[wifiScanList.size()];
		am= (AccountManager) MainActivity.this.getSystemService(Context.ACCOUNT_SERVICE);
        Account [] accounts = am.getAccountsByType("com.peppermint.peppermint");
        androidAccount =null;
        if (accounts.length > 0) {
            androidAccount = accounts[0];
        }
               
        Intent openStartingPoint;   
        if (androidAccount == null) {
           	openStartingPoint = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(openStartingPoint);
           }else{
            networkHandler = new NetworkHandler(this);
            networkHandler.setNetworkCallback(this);
            networkHandler.getNetworks();
           }


        if (app == null) {
            app = new MyApp();
            // Wait for GDB to init, for native debugging only
            if (false &&
                    (getApplicationInfo().flags &
                            ApplicationInfo.FLAG_DEBUGGABLE) != 0)
            {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {}
            }

            app.init(this, getFilesDir().getAbsolutePath());
        }

        if (app.accList.size() == 0) {
            accCfg = new AccountConfig();
            accCfg.setIdUri("sip:localhost");
            accCfg.getNatConfig().setIceEnabled(true);
            accCfg.getVideoConfig().setAutoTransmitOutgoing(true);
            accCfg.getVideoConfig().setAutoShowIncoming(true);
            account = app.addAcc(accCfg);
            LOGD(TAG, "Graahh"+account.cfg.getIdUri());



        } else {
            account = app.accList.get(0);
            accCfg = account.cfg;
            LOGD(TAG, "Blahh"+account.cfg.getIdUri());
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

                if(androidAccount!=null) {
                    int id = Integer.parseInt(am.getUserData(androidAccount, "id"));
                    subscriptionHandler = new SubscriptionHandler(this);
                    subscriptionHandler.setSubscriptionCallback(this);
                    subscriptionHandler.registerSubscription(id, callserver);
                    break;
                }
            }
        }
    }

    @Override
    public void getNetworksResponseReceived(List<Network> networks) {

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
        openStartingPoint.putExtra("sipUsername", am.getUserData(androidAccount, "username"));
        openStartingPoint.putExtra("sipDomain", subscription.getLocal_ip());
        openStartingPoint.putExtra("sipPassword", subscription.getPassword());
        openStartingPoint.putExtra("local_api", subscription.getLocal_api());
        LOGD(TAG, "Subscription Registered");


        String username = am.getUserData(androidAccount, "username");
        String domain = subscription.getLocal_ip();
        String password = subscription.getPassword();
        accCfg.setIdUri(AccountUtils.makeSipUri(username, domain));
        accCfg.getNatConfig().setIceEnabled(true);
        accCfg.getRegConfig().setRegistrarUri("sip:" + domain);
        AuthCredInfoVector creds = accCfg.getSipConfig().
                getAuthCreds();
        creds.clear();
        if (username.length() != 0) {
            creds.add(new AuthCredInfo("Digest", "*", username, 0, password));
        }
        StringVector proxies = accCfg.getSipConfig().getProxies();
        proxies.clear();

		    /* Enable ICE */
        accCfg.getNatConfig().setIceEnabled(true);

		    /* Finally */
        lastRegStatus = "";
        try {
            account.modify(accCfg);
        } catch (Exception e) {}

        openStartingPoint.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(openStartingPoint);

    }

    @Override
    public void getSubscriptionResponseReceived(Subscription subscription) {

    }

    @Override
    public void getSubscriptionsResponseReceived(List<Subscription> subscriptions) {

    }


    private void showCallActivity()
    {
        Intent intent = new Intent(this, CallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @Override
    public boolean handleMessage(Message m)
    {
        if (m.what == 0) {

            app.deinit();
            finish();
            Runtime.getRuntime().gc();
            android.os.Process.killProcess(android.os.Process.myPid());

        } else if (m.what == MSG_TYPE.CALL_STATE) {

            CallInfo ci = (CallInfo) m.obj;

	    /* Forward the message to CallActivity */
            if (CallActivity.handler_ != null) {
                Message m2 = Message.obtain(CallActivity.handler_,
                        MSG_TYPE.CALL_STATE, ci);
                m2.sendToTarget();
            }

        } else if (m.what == MSG_TYPE.CALL_MEDIA_STATE) {

	    /* Forward the message to CallActivity */
            if (CallActivity.handler_ != null) {
                Message m2 = Message.obtain(CallActivity.handler_,
                        MSG_TYPE.CALL_MEDIA_STATE,
                        null);
                m2.sendToTarget();
            }

        } else if (m.what == MSG_TYPE.BUDDY_STATE) {

            MyBuddy buddy = (MyBuddy) m.obj;
            int idx = account.buddyList.indexOf(buddy);

	    /* Update buddy status text, if buddy is valid and
	    * the buddy lists in account and UI are sync-ed.
	    */
            if (idx >= 0 && account.buddyList.size() == buddyList.size())
            {
                buddyList.get(idx).put("status", buddy.getStatusText());
                buddyListAdapter.notifyDataSetChanged();
                // TODO: selection color/mark is gone after this,
                //       dont know how to return it back.
                //buddyListView.setSelection(buddyListSelectedIdx);
                //buddyListView.performItemClick(buddyListView,
                //				     buddyListSelectedIdx,
                //				     buddyListView.
                //		    getItemIdAtPosition(buddyListSelectedIdx));

		/* Return back Call activity */
                notifyCallState(currentCall);
            }

        } else if (m.what == MSG_TYPE.REG_STATE) {

            String msg_str = (String) m.obj;
            lastRegStatus = msg_str;

        } else if (m.what == MSG_TYPE.INCOMING_CALL) {

	    /* Incoming call */
            final MyCall call = (MyCall) m.obj;
            CallOpParam prm = new CallOpParam();

	    /* Only one call at anytime */
            if (currentCall != null) {
		/*
		prm.setStatusCode(pjsip_status_code.PJSIP_SC_BUSY_HERE);
		try {
		call.hangup(prm);
		} catch (Exception e) {}
		*/
                // TODO: set status code
                call.delete();
                return true;
            }

	    /* Answer with ringing */
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_RINGING);
            try {
                call.answer(prm);
            } catch (Exception e) {}

            currentCall = call;
            showCallActivity();

        } else {

	    /* Message not handled */
            return false;

        }

        return true;
    }



    public static void makeCall(String uri)
    {
//        if (buddyListSelectedIdx == -1)
//            return;

	/* Only one call at anytime */
        if (currentCall != null) {
            return;
        }

//        HashMap<String, String> item = (HashMap<String, String>) buddyListView.
//                getItemAtPosition(buddyListSelectedIdx);
        String buddy_uri = uri;

        MyCall call = new MyCall(account, -1);
        CallOpParam prm = new CallOpParam(true);

        try {
            call.makeCall(buddy_uri, prm);
        } catch (Exception e) {
            call.delete();
            return;
        }

        call.OUTGOING = true;
        currentCall = call;

//        showCallActivity();
    }

    public static void acceptCall(View view)
    {
        CallOpParam prm = new CallOpParam();
        prm.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
        try {

            currentCall.answer(prm);

        } catch (Exception e) {
            System.out.println(e);
        }

        view.setVisibility(View.GONE);
    }


    public static void hangupCall()
    {

        if (currentCall != null) {
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
            try {
                currentCall.hangup(prm);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void delBuddy(View view) {
        if (buddyListSelectedIdx == -1)
            return;

        final HashMap<String, String> item = (HashMap<String, String>)
                buddyListView.getItemAtPosition(buddyListSelectedIdx);
        String buddy_uri = item.get("uri");

        DialogInterface.OnClickListener ocl =
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                account.delBuddy(buddyListSelectedIdx);
                                buddyList.remove(item);
                                buddyListAdapter.notifyDataSetChanged();
                                buddyListSelectedIdx = -1;
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(buddy_uri);
        adb.setMessage("\nDelete this buddy?\n");
        adb.setPositiveButton("Yes", ocl);
        adb.setNegativeButton("No", ocl);
        adb.show();
    }


    /*
    * === MyAppObserver ===
    *
    * As we cannot do UI from worker thread, the callbacks mostly just send
    * a message to UI/main thread.
    */

    public void notifyIncomingCall(MyCall call)
    {
        Message m = Message.obtain(handler, MSG_TYPE.INCOMING_CALL, call);
        m.sendToTarget();
    }

    public void notifyRegState(pjsip_status_code code, String reason,
                               int expiration)
    {
        String msg_str = "";
        if (expiration == 0)
            msg_str += "Unregistration";
        else
            msg_str += "Registration";

        if (code.swigValue()/100 == 2)
            msg_str += " successful";
        else
            msg_str += " failed: " + reason;

        Message m = Message.obtain(handler, MSG_TYPE.REG_STATE, msg_str);
        m.sendToTarget();
    }

    public void notifyCallState(MyCall call)
    {
        if (currentCall == null || call.getId() != currentCall.getId())
            return;

        CallInfo ci;
        try {
            ci = call.getInfo();
        } catch (Exception e) {
            ci = null;
        }
        Message m = Message.obtain(handler, MSG_TYPE.CALL_STATE, ci);
        m.sendToTarget();

        if (ci != null &&
                ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED)
        {
            currentCall = null;
        }
    }

    public void notifyCallMediaState(MyCall call)
    {
        Message m = Message.obtain(handler, MSG_TYPE.CALL_MEDIA_STATE, null);
        m.sendToTarget();
    }

    public void notifyBuddyState(MyBuddy buddy)
    {
        Message m = Message.obtain(handler, MSG_TYPE.BUDDY_STATE, buddy);
        m.sendToTarget();
    }

    /* === end of MyAppObserver ==== */

}
