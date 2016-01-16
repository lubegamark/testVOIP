/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.peppermint.peppermint.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.peppermint.peppermint.R;
import com.peppermint.peppermint.call.SipSettings;

import java.text.ParseException;

/**
 * Handles all calling, receiving calls, and UI interaction in the WalkieTalkie app.
 */
public class CallFragment extends Fragment implements View.OnTouchListener {

    public String sipAddress = null;
    public String sipUsername="lubega";
    public String sipDomain="192.168.1.20";
    public String sipPassword="bongo";
    public int    sipPort = 5060;

    public SipManager manager = null;
    public SipProfile profile = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver = null;
    //private static final int CALL_ADDRESS = 1;
    //private static final int SET_AUTH_INFO = 2;
    //private static final int UPDATE_SETTINGS_DIALOG = 3;
    //private static final int HANG_UP = 4;

    Context currentContext ;
    View rootView;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.walkietalkie);

        currentContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.walkietalkie, container, false);
        initializeManager();


        // Set up the intent filter.  This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.peppermint.peppermint.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        currentContext.registerReceiver(callReceiver, filter);



        // "Push to talk" can be a serious pain when the screen keeps turning off.
        // Let's prevent that.
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        rootView.findViewById(R.id.callStateImage).setOnTouchListener( this );




        return rootView;
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
            currentContext.unregisterReceiver(callReceiver);
        }
    }

    public void initializeManager() {
        if(manager == null) {
          manager = SipManager.newInstance(currentContext);
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        sipUsername = prefs.getString("namePref", "");
        sipDomain = prefs.getString("domainPref", "");
        sipPassword = prefs.getString("passPref", "");
        sipPort = Integer.parseInt( prefs.getString( "portPref", "5060" ) );

        if (sipUsername.length() == 0 || sipDomain.length() == 0 || sipPassword.length() == 0) {
            getActivity().showDialog(R.id.SET_SIP_OPTIONS);
            return;
        }

        try {
            SipProfile.Builder builder = new SipProfile.Builder(sipUsername, sipDomain);
            builder.setPassword(sipPassword);
            builder.setPort(sipPort);
            profile = builder.build();

            Intent i = new Intent();
            i.setAction("com.peppermint.peppermint.INCOMING_CALL");
            PendingIntent pi = PendingIntent.getBroadcast(currentContext, 0, i, Intent.FILL_IN_DATA);
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
            Log.d("CallFragment/onDestroy", "Failed to close local profile.", ee);
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
            Log.i("CallFragment/InitiateCall", "Error when trying to close manager.", e);
            if (profile != null) {
                try {
                    manager.close(profile.getUriString());
                } catch (Exception ee) {
                    Log.i("CallFragment/InitiateCall",
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
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                TextView labelView = (TextView) rootView.findViewById(R.id.sipLabel);
                labelView.setText(status);

                //the ImageView for illustrating the call-state

                ImageView callStateImage = (ImageView) rootView.findViewById(R.id.callStateImage);
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
                Log.d("CallFragment/onOptionsItemSelected",
                        "Error ending call.", se);
            }
            incomingCall.close();
            return;
        }

        call = incomingCall;
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
                        Log.d("CallFragment/onOptionsItemSelected",
                                "Error ending call.", se);
                    }
                    call.close();
                    updateStatus("call closed. / ready again.");
                    return true;

                } else if( event.getAction() == MotionEvent.ACTION_DOWN
                        && ( call == null || ( call != null && !call.isInCall() ) ) ) {

                    getActivity().showDialog(R.id.CALL_ADDRESS);
                    return true;

                }
                return true;

        }

        return false;

    }

    //  @Override
    public void onPrepareDialog(int id, Dialog dialog) {

        switch( id )
        {

            case R.id.CALL_ADDRESS:
                //force keyboard
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                break;

        }

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sipphone, menu);
        //menu.add(0, CALL_ADDRESS, 0, "Call someone");
        //menu.add(0, SET_AUTH_INFO, 0, "Edit your SIP Info.");
        //menu.add(0, HANG_UP, 0, "End Current Call.");
        //return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.CALL_ADDRESS:

                getActivity().showDialog(R.id.CALL_ADDRESS);
                break;
            case R.id.SET_SIP_OPTIONS:
                updatePreferences();
                break;
            case R.id.HANG_UP:
                if(call != null) {
                    try {
                      call.endCall();
                    } catch (SipException se) {
                        Log.d("CallFragment/onOptionsItemSelected",
                                "Error ending call.", se);
                    }
                    call.close();
                    updateStatus("call closed. / ready again.");
                }
                break;
        }
        return true;
    }

    //@Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case R.id.CALL_ADDRESS:

                LayoutInflater factory = LayoutInflater.from(currentContext);
                final View textBoxView = factory.inflate(R.layout.call_address_dialog, null);
                return new AlertDialog.Builder(currentContext)
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
                return new AlertDialog.Builder(currentContext)
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
        Intent settingsActivity = new Intent(getActivity().getBaseContext(),
                SipSettings.class);
        startActivity(settingsActivity);

    }


    /**
     * Listens for incoming SIP calls, intercepts and hands them off to CallFragment.
     */
    public class IncomingCallReceiver extends BroadcastReceiver {
        /**
         * Processes the incoming call, answers it, and hands it over to the
         * CallFragment.
         * @param context The context under which the receiver is running.
         * @param intent The intent being received.
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            SipAudioCall incomingCall = null;
            try {

                SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                    @Override
                    public void onRinging(SipAudioCall call, SipProfile caller) {
                        try {
                            call.answerCall(30);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };

                incomingCall = manager.takeAudioCall(intent, listener);
                incomingCall.answerCall(30);
                incomingCall.startAudio();
                incomingCall.setSpeakerMode(true);
                //MUG::  if(incomingCall.isMuted()) {
                //MUG::     incomingCall.toggleMute();
                //MUG::}


                //MUG:: wtActivity.call = incomingCall;
                updateCallStatus(incomingCall);

            } catch (Exception e) {
                if (incomingCall != null) {
                    incomingCall.close();
                }
            }
        }

    }

}
