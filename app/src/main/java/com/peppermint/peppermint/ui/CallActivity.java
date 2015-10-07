package com.peppermint.peppermint.ui;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.peppermint.peppermint.R;
import com.peppermint.peppermint.call.SipSettings;
import com.peppermint.peppermint.receiver.IncomingCallReceiver;

import java.text.ParseException;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;

/**
 * Created by mark on 7/7/15.
 */
/**
* Handles all calling, receiving calls, and UI interaction in the WalkieTalkie app.
*/
    public class CallActivity extends AppCompatActivity implements View.OnTouchListener {
    private static final String TAG = makeLogTag(CallActivity.class);
    public String sipAddress = null;
    public String sipUsername=null;
    public String sipDomain=null;
    public String sipPassword=null;
    public int    sipPort = 5060;

    public SipManager manager = null;
    public SipProfile profile = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver = null;
    //private static final int CALL_ADDRESS = 1;
    //private static final int SET_AUTH_INFO = 2;
    //private static final int UPDATE_SETTINGS_DIALOG = 3;
    //private static final int HANG_UP = 4;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.walkietalkie);

/*
        sipAddress = null;
        sipUsername=getIntent().getExtras().getString("sipUsername");
        sipDomain=getIntent().getExtras().getString("sipDomain");
        sipPassword=getIntent().getExtras().getString("sipPassword");
*/

        // Set up the intent filter.  This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.peppermint.peppermint.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);

        // "Push to talk" can be a serious pain when the screen keeps turning off.
        // Let's prevent that.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        findViewById(R.id.callStateImage).setOnTouchListener(this);

        initializeManager();
    }




    @Override
    public void onStart() {
        super.onStart();
        // When we get back from the preference setting Activity, assume
        // settings have changed, and re-login with new auth info.
        initializeManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.close();
        }

        closeLocalProfile();

        if (callReceiver != null) {
            this.unregisterReceiver(callReceiver);
        }
    }

    public void initializeManager() {
        if(manager == null) {
            manager = SipManager.newInstance(this);
        }

        initializeLocalProfile();
    }

    /**
     * Logs you into your SIP provider, registering this device as the location to
     * send SIP calls to for your SIP address.
     */
    public void initializeLocalProfile() {
        if (manager == null) {
            return;
        }

        if (profile != null) {
            closeLocalProfile();
        }

        /*

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sipUsername = prefs.getString("namePref", "");
        sipDomain = prefs.getString("domainPref", "");
        sipPassword = prefs.getString("passPref", "");
        sipPort = Integer.parseInt( prefs.getString( "portPref", "5060" ) );

        */
        sipUsername=getIntent().getExtras().getString("sipUsername");
        sipDomain=getIntent().getExtras().getString("sipDomain");
        sipPassword=getIntent().getExtras().getString("sipPassword");
        LOGD(TAG, "password-"+sipPassword);
        if (sipUsername.length() == 0 || sipDomain.length() == 0 || sipPassword.length() == 0) {
            showDialog(R.id.SET_SIP_OPTIONS);
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
            Log.d("WalkieTalkieActivity/onDestroy", "Failed to close local profile.", ee);
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
            Log.i("WalkieTalkieActivity/InitiateCall", "Error when trying to close manager.", e);
            if (profile != null) {
                try {
                    manager.close(profile.getUriString());
                } catch (Exception ee) {
                    Log.i("WalkieTalkieActivity/InitiateCall",
                            "Error when trying to close manager.", ee);
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
                Log.d("WalkieTalkieActivity/onOptionsItemSelected",
                        "Error ending call.", se);
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
                        Log.d("WalkieTalkieActivity/onOptionsItemSelected",
                                "Error ending call.", se);
                    }
                    call.close();
                    updateStatus("call closed. / ready again.");
                    return true;

                } else if( event.getAction() == MotionEvent.ACTION_DOWN
                        && ( call == null || ( call != null && !call.isInCall() ) ) ) {

                    showDialog(R.id.CALL_ADDRESS);
                    return true;

                }
                return true;

        }

        return false;

    }

    @Override
    public void onPrepareDialog(int id, android.app.Dialog dialog) {

        switch( id )
        {

            case R.id.CALL_ADDRESS:
                //force keyboard
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                break;

        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sipphone_menu, menu);
        //menu.add(0, CALL_ADDRESS, 0, "Call someone");
        //menu.add(0, SET_AUTH_INFO, 0, "Edit your SIP Info.");
        //menu.add(0, HANG_UP, 0, "End Current Call.");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.CALL_ADDRESS:

                showDialog(R.id.CALL_ADDRESS);
                break;
            case R.id.SET_SIP_OPTIONS:
                updatePreferences();
                break;
            case R.id.HANG_UP:
                if(call != null) {
                    try {
                        call.endCall();
                    } catch (SipException se) {
                        Log.d("WalkieTalkieActivity/onOptionsItemSelected",
                                "Error ending call.", se);
                    }
                    call.close();
                    updateStatus("call closed. / ready again.");
                }
                break;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case R.id.CALL_ADDRESS:

                LayoutInflater factory = LayoutInflater.from(this);
                final View textBoxView = factory.inflate(R.layout.call_address_dialog, null);
                return new android.app.AlertDialog.Builder(this)
                        .setTitle("Call Someone.")
                        .setView(textBoxView)
                        .setPositiveButton(
                                android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        EditText textField = (EditText)
                                                (textBoxView.findViewById(R.id.calladdress_edit));
                                        sipAddress = textField.getText().toString();
                                        initiateCall();

                                    }
                                })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Noop.
                                    }
                                })
                        .create();

            case R.id.SET_SIP_OPTIONS:
                return new android.app.AlertDialog.Builder(this)
                        .setMessage("Please update your SIP Account Settings.")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                updatePreferences();
                            }
                        })
                        .setNegativeButton(
                                android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        // Noop.
                                    }
                                })
                        .create();
        }
        return null;
    }

    public void updatePreferences() {
        Intent settingsActivity = new Intent(getBaseContext(),
                SipSettings.class);
        startActivity(settingsActivity);
    }

}