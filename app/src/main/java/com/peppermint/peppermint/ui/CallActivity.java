package com.peppermint.peppermint.ui;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.peppermint.peppermint.R;

import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_role_e;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;

public class CallActivity extends AppCompatActivity implements Handler.Callback {
    private static final String TAG = makeLogTag(CallActivity.class);
    public static Handler handler_;
    private AnswerFragment mAnswerFragment;
    private final Handler handler = new Handler(this);
    private static CallInfo lastCallInfo;

    private static FloatingActionButton buttonHangup;
    private static TextView timerTextView;
    private static TextView actionTextView;
    private static TextView nameTextView;

    long startTime = 0;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            timerTextView.setText(String.format("%d:%02d", minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        timerTextView = (TextView)findViewById(R.id.time);
        actionTextView = (TextView)findViewById(R.id.action);
        nameTextView = (TextView)findViewById(R.id.name);
        buttonHangup = (FloatingActionButton) findViewById(R.id.in_call_hang_up);
        buttonHangup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.hangupCall();
                timerHandler.removeCallbacks(timerRunnable);
                actionTextView.setText("Call ended...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });
//        nameTextView.setText();
        if (MainActivity.currentCall != null && !MainActivity.currentCall.OUTGOING) {

            mAnswerFragment = new AnswerFragment();
            hideEndCallButton();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.answer_call, mAnswerFragment);
            transaction.commit();
        }


        handler_ = handler;
        if (MainActivity.currentCall != null) {
            try {
                lastCallInfo = MainActivity.currentCall.getInfo();
                updateCallState(lastCallInfo);
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
            updateCallState(lastCallInfo);
            LOGD(TAG, "No current call");
        }

    }

    public void hideEndCallButton() {
        buttonHangup.setVisibility(View.GONE);
    }

    public void showEndCallButton() {
        buttonHangup.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler_ = null;
    }

    @Override
    public boolean handleMessage(Message m) {
        if (m.what == MainActivity.MSG_TYPE.CALL_STATE) {

            lastCallInfo = (CallInfo) m.obj;
            updateCallState(lastCallInfo);
            LOGD("Call state", lastCallInfo.getStateText());

        } else if (m.what == MainActivity.MSG_TYPE.CALL_MEDIA_STATE) {

            if (MainActivity.currentCall.vidWin != null) {
        /* If there's incoming video, display it. */
                //setupVideoSurface();
            }

        } else {

	    /* Message not handled */
            return false;

        }

        return true;
    }

    private void updateCallState(CallInfo ci) {
        String call_state = "";


        if (ci.getRole() == pjsip_role_e.PJSIP_ROLE_UAC) {

        }

        if (ci.getState().swigValue() <
                pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {
            if (ci.getRole() == pjsip_role_e.PJSIP_ROLE_UAS) {
                call_state = "Incoming call...";
                actionTextView.setText("Incoming Call...");
                nameTextView.setText(
                        ci.getRemoteUri()
                        .substring(ci.getRemoteUri().indexOf(":")+1, ci.getRemoteUri().indexOf("@")));

		/* Default button texts are already 'Accept' & 'Reject' */
            } else {
                call_state = ci.getStateText();
                actionTextView.setText("Dialing...");
                nameTextView.setText(
                        ci.getRemoteUri()
                                .substring(ci.getRemoteUri().indexOf(":")+1, ci.getRemoteUri().indexOf("@")));
            }
        } else if (ci.getState().swigValue() >=
                pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {


            call_state = ci.getStateText();
            actionTextView.setText("In call...");
            if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                startTime = System.currentTimeMillis();
                timerHandler.postDelayed(timerRunnable, 0);
            } else if (ci.getState() ==
                    pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                timerHandler.removeCallbacks(timerRunnable);
                actionTextView.setText("Call ended...");
                call_state = "Call disconnected: " + ci.getLastReason();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
            }
        }
        if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONNECTING) {
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
        }
//        LOGE(TAG, "CallId String " + ci.getCallIdString());
//        LOGE(TAG, "Last Reason " + ci.getLastReason());
//        LOGE(TAG, "LocalContact " + ci.getLocalContact());
//        LOGE(TAG, "Local Uri- " + ci.getLocalUri());
//        LOGE(TAG, "State " + ci.getState());
//        LOGE(TAG, "State Text" + ci.getStateText());
//        LOGE(TAG, "Connection Duration- " + ci.getConnectDuration().getSec());
//        LOGE(TAG, "Remote URI " + ci.getRemoteUri());
//        LOGE(TAG, "Total duration " + ci.getTotalDuration().getSec());
//        LOGE(TAG, "Remote Contact " + ci.getRemoteContact().toString());
//        LOGE(TAG, "Role" + ci.getRole());

    }


}
