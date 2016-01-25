package com.peppermint.peppermint.ui;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import com.peppermint.peppermint.R;

import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.CallInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.VideoWindowHandle;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_role_e;
import org.pjsip.pjsua2.pjsip_status_code;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;

    public class CallActivity extends AppCompatActivity implements Handler.Callback, SurfaceHolder.Callback{
    private static final String TAG = makeLogTag(CallActivity.class);
    public static AccountConfig accCfg = null;
    public static Handler handler_;
        private AnswerFragment mAnswerFragment;
    private final Handler handler = new Handler(this);
    private static CallInfo lastCallInfo;

        private FloatingActionButton buttonHangup;
        private DragEventListener mDragListen;
        private float mPrevX;
        private float mPrevY;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        FrameLayout lower = (FrameLayout)findViewById(R.id.answer_call);
        buttonHangup = (FloatingActionButton) findViewById(R.id.in_call_hang_up);
        buttonHangup.setVisibility(View.GONE);
        buttonHangup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hangupCall(v);
            }
        });
//        setContentView(R.layout.answer_fragment);
        AnswerFragment answerFragment = new AnswerFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.answer_call, answerFragment);
        transaction.commit();

//        SurfaceView surfaceView = (SurfaceView)
//                findViewById(R.id.surfaceIncomingVideo);
//        if (MainActivity.currentCall == null ||
//                MainActivity.currentCall.vidWin == null)
//        {
//            surfaceView.setVisibility(View.GONE);
//        }
//        surfaceView.getHolder().addCallback(this);
//        mDragListen = new DragEventListener();
//        final FloatingActionButton call_respond = (FloatingActionButton)findViewById(R.id.in_call_pick_up);
//        //call_respond.setOnDragListener(mDragListen);
//
//        call_respond.setOnTouchListener(new View.OnTouchListener() {
////            public void onSwipeTop(float translate) {
////                call_respond.setTranslationY(translate);
////                Toast.makeText(CallActivity.this, "top", Toast.LENGTH_SHORT).show();
////            }
////            public void onSwipeRight(float translate) {
////                call_respond.setTranslationX(translate);
////
////                Toast.makeText(CallActivity.this, "right", Toast.LENGTH_SHORT).show();
////                //LayoutParams params = new LayoutParams(call_respond.getWidth(), call_respond.getHeight(),(int)(me.getRawX() - (call_respond.getWidth() / 2)), (int)(me.getRawY() - (call_respond.getHeight())));
////                //call_respond.setLayoutParams(params);
////            }
////            public void onSwipeLeft(float translate) {
////                call_respond.setTranslationX(translate);
////                Toast.makeText(CallActivity.this, "left", Toast.LENGTH_SHORT).show();
////            }
////            public void onSwipeBottom(float translate) {
////                call_respond.setTranslationY(translate);
////                Toast.makeText(CallActivity.this, "bottom", Toast.LENGTH_SHORT).show();
////            }
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                float currX,currY;
//                int action = event.getAction();
//                switch (action ) {
//                    case MotionEvent.ACTION_DOWN: {
//
//                        mPrevX = event.getX();
//                        mPrevY = event.getY();
//                        break;
//                    }
//
//                    case MotionEvent.ACTION_MOVE:
//                    {
//
//                        currX = event.getRawX();
//                        currY = event.getRawY();
//
//
//                        MarginLayoutParams marginParams = new MarginLayoutParams(view.getLayoutParams());
//                        marginParams.setMargins((int)(currX - mPrevX), (int)(currY - mPrevY),0, 0);
//                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(marginParams);
//                        view.setLayoutParams(layoutParams);
//
//
//                        break;
//                    }
//
//
//
////                    case MotionEvent.ACTION_CANCEL:
////                        MarginLayoutParams marginParams = new MarginLayoutParams(view.getLayoutParams());
////                        marginParams.setMargins(0,0,0, 0);
////                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(marginParams);
////                        view.setLayoutParams(layoutParams);
////                        break;
//
////                    case MotionEvent.ACTION_UP:
////
////                        blreak;
//                }
//
//                return true;
//            }
//
//
//
//        });

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
            updateCallState(lastCallInfo);
        }

//        Bundle extras= getIntent().getExtras();
//        String username = extras.getString("sipUsername").toString();
//        String domain = extras.get("sipDomain").toString();
//        String password = extras.get("sipPassword").toString();
//        accCfg = new AccountConfig();
//        accCfg.setIdUri("sip:" + username + "@" + domain);
//        accCfg.getNatConfig().setIceEnabled(true);
//        accCfg.getRegConfig().setRegistrarUri("sip:" + domain);
//        AuthCredInfo creds= new AuthCredInfo("Digest", "*", username, 0, password);
//        accCfg.getSipConfig().getAuthCreds().add(creds);
//        MyAccount acc = new MyAccount(accCfg);
//        LOGD(TAG, String.valueOf(acc.isValid()));
//        try {
//            acc.setRegistration(true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

@Override
protected void onDestroy()
        {
        super.onDestroy();
        handler_ = null;
        }

private void updateVideoWindow(SurfaceHolder holder)
        {
        if (MainActivity.currentCall != null &&
        MainActivity.currentCall.vidWin != null)
        {
        VideoWindowHandle vidWH = new VideoWindowHandle();
        if (holder == null)
        vidWH.getHandle().setWindow(null);
        else
        vidWH.getHandle().setWindow(holder.getSurface());
        try {
        MainActivity.currentCall.vidWin.setWindow(vidWH);
        } catch (Exception e) {}
        }
        }

public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
        {
        updateVideoWindow(holder);
        }

public void surfaceCreated(SurfaceHolder holder)
        {
        }

public void surfaceDestroyed(SurfaceHolder holder)
        {
        updateVideoWindow(null);
        }

public void acceptCall(View view)
        {
        CallOpParam prm = new CallOpParam();
        prm.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
        try {
        MainActivity.currentCall.answer(prm);
        } catch (Exception e) {
        System.out.println(e);
        }

        view.setVisibility(View.GONE);
        buttonHangup.setVisibility(View.VISIBLE);
        }

public void hangupCall(View view)
        {
        handler_ = null;
        finish();

        if (MainActivity.currentCall != null) {
        CallOpParam prm = new CallOpParam();
        prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
        try {
        MainActivity.currentCall.hangup(prm);
        } catch (Exception e) {
        System.out.println(e);
        }
        }
        }

//private void setupVideoSurface()
//        {
//        SurfaceView surfaceView = (SurfaceView)
//        findViewById(R.id.surfaceIncomingVideo);
//        surfaceView.setVisibility(View.VISIBLE);
//        updateVideoWindow(surfaceView.getHolder());
//        }

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
        //TextView tvPeer  = (TextView) findViewById(R.id.textViewPeer);
        //TextView tvState = (TextView) findViewById(R.id.textViewCallState);

        //Button buttonAccept = (Button) findViewById(R.id.buttonAccept);
        String call_state = "";

        if (ci.getRole() == pjsip_role_e.PJSIP_ROLE_UAC) {
        //buttonAccept.setVisibility(View.GONE);
        }

        if (ci.getState().swigValue() <
        pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue())
        {
        if (ci.getRole() == pjsip_role_e.PJSIP_ROLE_UAS) {
        call_state = "Incoming call..";
		/* Default button texts are already 'Accept' & 'Reject' */
        } else {
        //buttonHangup.setText("Cancel");
        call_state = ci.getStateText();
        }
        }
        else if (ci.getState().swigValue() >=
        pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue())
        {
        //buttonAccept.setVisibility(View.GONE);
        call_state = ci.getStateText();
        if (ci.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
        //buttonHangup.setText("Hangup");
        } else if (ci.getState() ==
        pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED)
        {
        //buttonHangup.setText("OK");
        call_state = "Call disconnected: " + ci.getLastReason();
        }
        }

        //tvPeer.setText(ci.getRemoteUri());
        //tvState.setText(call_state);
        }




}
