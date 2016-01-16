package com.peppermint.peppermint.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;

import com.peppermint.peppermint.R;
import com.peppermint.peppermint.util.NetUtils;

import java.text.ParseException;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.LOGI;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;


public class CallService extends Service {
    Context serviceContext = this;
    Boolean isOnline;
    BroadcastReceiver connectedReceiver;
    public static boolean isActive = false;

    public String sipAddress = null;
    public String sipUsername="";
    public String sipDomain="";
    public String sipPassword="";
    public int    sipPort = 5060;

    public SipManager manager = null;
    public SipProfile profile = null;
    public SipAudioCall call = null;
    //public IncomingCallReceiver callReceiver = null;

    public static final String TAG = makeLogTag(CallService.class);




    public CallService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LOGD(TAG, "Service created");
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.peppermint.peppermint.INCOMING_CALL");
        //callReceiver = new IncomingCallReceiver();
        //this.registerReceiver(callReceiver, filter);

        connectedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                isOnline = NetUtils.isDataConnected(serviceContext);
                LOGD(TAG, "Is Online Listener in Service -"+Boolean.toString(isOnline));


                if(isOnline){

                }

            }
        };

        initializeManager();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.registerReceiver((connectedReceiver), new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        isActive = true;
        LOGD(TAG, "Service Started");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(connectedReceiver);
        isActive = false;
        LOGD(TAG, "Service destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }


    public void initializeManager() {
        if(manager == null) {
            LOGD(TAG, "No manager available");
            manager = SipManager.newInstance(serviceContext);

            LOGD(TAG, " Is API supported " + Boolean.toString(SipManager.isApiSupported(this)));


        }else{
            LOGD(TAG, "Manager was available");
        }



        initializeLocalProfile();
    }

    /**
     * Logs you into your SIP provider, registering this device as the location to
     * send SIP calls to for your SIP address.
     */
    public void initializeLocalProfile() {
        if (manager == null) {
            LOGD(TAG, "");
            return;
        }

        if (profile != null) {
            closeLocalProfile();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sipUsername = prefs.getString("namePref", "");
        sipDomain = prefs.getString("domainPref", "");
        sipPassword = prefs.getString("passPref", "");
        sipPort = Integer.parseInt( prefs.getString( "portPref", "5060" ) );

        if (sipUsername.length() == 0 || sipDomain.length() == 0 || sipPassword.length() == 0) {
            //showDialog(R.id.SET_SIP_OPTIONS);
            LOGD(TAG, "Some user account settins missing");
            return;
        }

        try {
            SipProfile.Builder builder = new SipProfile.Builder(sipUsername, sipDomain);
            builder.setPassword(sipPassword);
            builder.setPort(sipPort);
            profile = builder.build();

            Intent i = new Intent();
            i.setAction("com.peppermint.peppermint.INCOMING_CALL");
            PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);
            manager.open(profile, pi, null);


            // This listener must be added AFTER manager.open is called,
            // Otherwise the methods aren't guaranteed to fire.

            manager.setRegistrationListener(profile.getUriString(), new SipRegistrationListener() {
                public void onRegistering(String localProfileUri) {
                    updateStatus("Registering with SIP Server...");
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    updateStatus("Ready");
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    updateStatus("Registration failed.  Please check settings.");
                }
            });
        } catch (ParseException pe) {
            updateStatus("Connection Error.");
        } catch (SipException se) {
            updateStatus("Connection error.");
        }
    }

    /**
     * Closes out your local profile, freeing associated objects into memory
     * and unregistering your device from the server.
     */
    public void closeLocalProfile() {
        if (manager == null) {
            return;
        }
        try {
            if (profile != null) {
                manager.close(profile.getUriString());
            }
        } catch (Exception ee) {
            LOGD(TAG, "Failed to close local profile.", ee);
        }
    }



    /**
     * Make an outgoing call.
     */
    public void initiateCall() {

        updateStatus(sipAddress);

        try {
            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                // Much of the client's interaction with the SIP Stack will
                // happen via listeners.  Even making an outgoing call, don't
                // forget to set up a listener to set things up once the call is established.
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    call.startAudio();
                    call.setSpeakerMode(true);
                    if(call.isMuted()){
                        call.toggleMute();
                    }
                    updateStatus("Call established.");
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    updateStatus("Ready.");
                }
            };

            //sipID correction (example: sipid@domain.com)
            if(!sipAddress.contains("@")){
                sipAddress = sipAddress + "@" + sipDomain;
            }

            call = manager.makeAudioCall(profile.getUriString(), sipAddress, listener, 30);

        }
        catch (Exception e) {
            LOGI(TAG, "Error when trying to close manager.", e);
            if (profile != null) {
                try {
                    manager.close(profile.getUriString());
                } catch (Exception ee) {
                    LOGI(TAG, "Error when trying to close manager.", ee);
                    ee.printStackTrace();
                }
            }
            if (call != null) {
                call.close();
            }
        }
    }

    /**
     * Updates the status box at the top of the UI with a messege of your choice.
     * @param status The String to display in the status box.
     */
    public void updateStatus(final String status) {
        // Be a good citizen.  Make sure UI changes fire on the UI thread.

        /*
        this.runOnUiThread(new Runnable() {
            public void run() {
                TextView labelView = (TextView) findViewById(R.id.sipLabel);
                labelView.setText(status);

                //the ImageView for illustrating the call-state

                ImageView callStateImage = (ImageView) findViewById(R.id.callStateImage);
                if((call != null) && call.isInCall()){
                    callStateImage.setBackgroundResource(R.drawable.btn_speak_pressed);
                }
                else {
                    callStateImage.setBackgroundResource(R.drawable.btn_speak_normal);
                }

            }
        });
        */
    }

    /**
     * Updates the status box with the SIP address of the current call.
     * @param incomingCall The current, active call.
     */
    public void updateCallStatus(SipAudioCall incomingCall) {
        if((call != null) && call.isInCall()){
            try {
                incomingCall.endCall();
            } catch (SipException se) {
                LOGD(TAG, "Error ending call.", se);
            }
            incomingCall.close();
            return;
        }

        this.call = incomingCall;
        String useName = call.getPeerProfile().getDisplayName();
        if(useName == null) {
            useName = call.getPeerProfile().getUserName();
        }
        updateStatus(useName + "@" + call.getPeerProfile().getSipDomain());
    }

    /**
     * Updates whether or not the user's voice is muted, depending on whether the button is pressed.
     * @param v The View where the touch event is being fired.
     * @param event The motion to act on.
     * @return boolean Returns false to indicate that the parent view should handle the touch event
     * as it normally would.
     */
    public boolean onTouch(View v, MotionEvent event) {

        switch( v.getId() )
        {

            case R.id.callStateImage:
                if ( event.getAction() == MotionEvent.ACTION_DOWN
                        && call != null
                        && call.isInCall() ) {

                    try {
                        call.endCall();
                    } catch (SipException se) {
                        LOGD(TAG, "Error ending call.", se);
                    }
                    call.close();
                    updateStatus("call closed. / ready again.");
                    return true;

                } else if( event.getAction() == MotionEvent.ACTION_DOWN
                        && ( call == null || ( call != null && !call.isInCall() ) ) ) {
                    LOGD(TAG, "Call");
                    //showDialog(R.id.CALL_ADDRESS);
                    return true;

                }
                return true;

        }

        return false;

    }



}
