package com.peppermint.peppermint.ui;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.peppermint.peppermint.R;

import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_role_e;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;

    public class CallActivity extends AppCompatActivity implements Handler.Callback{
    private static final String TAG = makeLogTag(CallActivity.class);
    public static Handler handler_;
    private AnswerFragment mAnswerFragment;
    private final Handler handler = new Handler(this);
    private static CallInfo lastCallInfo;

        private static FloatingActionButton buttonHangup;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        buttonHangup = (FloatingActionButton) findViewById(R.id.in_call_hang_up);
        buttonHangup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity.hangupCall();
                finish();
            }
        });
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
                LOGD(TAG, "last callinfo-"+lastCallInfo);
                updateCallState(lastCallInfo);
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {
//            updateCallState(lastCallInfo);
        LOGD(TAG, "No current call");
        }

    }

        public void hideEndCallButton(){
            buttonHangup.setVisibility(View.GONE);
        }

        public void showEndCallButton(){
            buttonHangup.setVisibility(View.VISIBLE);
        }

@Override
protected void onDestroy()
        {
        super.onDestroy();
        handler_ = null;
        }

@Override
public boolean handleMessage(Message m)
        {
        if (m.what == MainActivity.MSG_TYPE.CALL_STATE) {

        lastCallInfo = (CallInfo) m.obj;
        updateCallState(lastCallInfo);

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
        pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue())
        {
        if (ci.getRole() == pjsip_role_e.PJSIP_ROLE_UAS) {
        call_state = "Incoming call..";
		/* Default button texts are already 'Accept' & 'Reject' */
        } else {
        call_state = ci.getStateText();
        }
        }
        else if (ci.getState().swigValue() >=
        pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue())
        {
        call_state = ci.getStateText();
        if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
        //buttonHangup.setText("Hangup");
        } else if (ci.getState() ==
        pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED)
        {
        call_state = "Call disconnected: " + ci.getLastReason();
        }
        }

        }




}
