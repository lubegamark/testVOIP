/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * limitations under the License
 */

package com.peppermint.peppermint.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.peppermint.peppermint.R;
import com.peppermint.peppermint.util.Log;
import com.peppermint.peppermint.util.VideoProfile;

import java.util.ArrayList;
import java.util.List;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;

/**
 *
 */
public class AnswerFragment extends Fragment implements GlowPadWrapper.AnswerListener{//, AnswerPresenter.AnswerUi {
//public class AnswerFragment extends Fragment implements GlowPadWrapper.AnswerListener {
    private static final String TAG = makeLogTag(AnswerFragment.class);
    public static final int TARGET_SET_FOR_AUDIO_WITHOUT_SMS = 0;
    public static final int TARGET_SET_FOR_AUDIO_WITH_SMS = 1;
    public static final int TARGET_SET_FOR_VIDEO_WITHOUT_SMS = 2;
    public static final int TARGET_SET_FOR_VIDEO_WITH_SMS = 3;
    public static final int TARGET_SET_FOR_VIDEO_ACCEPT_REJECT_REQUEST = 4;

    /**
     * The popup showing the list of canned responses.
     *
     * This is an AlertDialog containing a ListView showing the possible choices.  This may be null
     * if the InCallScreen hasn't ever called showRespondViaSmsPopup() yet, or if the popup was
     * visible once but then got dismissed.
     */
    private Dialog mCannedResponsePopup = null;

    /**
     * The popup showing a text field for users to type in their custom message.
     */
    private AlertDialog mCustomMessagePopup = null;

    private ArrayAdapter<String> mSmsResponsesAdapter;

    private final List<String> mSmsResponses = new ArrayList<>();

    private GlowPadWrapper mGlowpad;
    CallActivity callActivity;

    public Vibrator vibrator;
    private AudioManager myAudioManager;
    Ringtone r;
    public AnswerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mGlowpad = (GlowPadWrapper) inflater.inflate(R.layout.answer_fragment,
                container, false);
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        myAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        Uri ring = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        r = RingtoneManager.getRingtone(getContext(), ring);
        r.play();
        long[] pattern = {100, 1000, 2000, 1000, 2000, 1000, 2000};
        vibrator.vibrate(pattern, 2 );

        LOGD(TAG, " Creating view for answer fragment");
        LOGD(TAG, "Created from activity" + getActivity());
        mGlowpad.setAnswerListener(this);
        mGlowpad.startPing();
        callActivity = (CallActivity)getContext();
        return mGlowpad;
    }

    @Override
    public void onDestroyView() {
        LOGD(TAG, "onDestroyView");
        if (mGlowpad != null) {
            mGlowpad.stopPing();
            mGlowpad = null;
        }
        r.stop();
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        r.stop();
        vibrator.cancel();
        super.onPause();
    }

    /**
     * Sets targets on the glowpad according to target set identified by the parameter.
     * @param targetSet Integer identifying the set of targets to use.
     */
    public void showTargets(int targetSet) {
        showTargets(targetSet, VideoProfile.STATE_BIDIRECTIONAL);
    }

    /**
     * Sets targets on the glowpad according to target set identified by the parameter.
     * @param targetSet Integer identifying the set of targets to use.
     */
//    @Override
    public void showTargets(int targetSet, int videoState) {
        final int targetResourceId;
        final int targetDescriptionsResourceId;
        final int directionDescriptionsResourceId;
        final int handleDrawableResourceId;
        mGlowpad.setVideoState(videoState);

        switch (targetSet) {
            case TARGET_SET_FOR_AUDIO_WITH_SMS:
                targetResourceId = R.array.incoming_call_widget_audio_with_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_with_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_with_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_audio_handle;
                break;
            case TARGET_SET_FOR_VIDEO_WITHOUT_SMS:
                targetResourceId = R.array.incoming_call_widget_video_without_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_video_without_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_video_without_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            case TARGET_SET_FOR_VIDEO_WITH_SMS:
                targetResourceId = R.array.incoming_call_widget_video_with_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_video_with_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_video_with_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            case TARGET_SET_FOR_VIDEO_ACCEPT_REJECT_REQUEST:
                targetResourceId =
                    R.array.incoming_call_widget_video_request_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_video_request_target_descriptions;
                directionDescriptionsResourceId = R.array
                        .incoming_call_widget_video_request_target_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_video_handle;
                break;
            case TARGET_SET_FOR_AUDIO_WITHOUT_SMS:
            default:
                targetResourceId = R.array.incoming_call_widget_audio_without_sms_targets;
                targetDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_without_sms_target_descriptions;
                directionDescriptionsResourceId =
                        R.array.incoming_call_widget_audio_without_sms_direction_descriptions;
                handleDrawableResourceId = R.drawable.ic_incall_audio_handle;
                break;
        }

        if (targetResourceId != mGlowpad.getTargetResourceId()) {
            mGlowpad.setTargetResources(targetResourceId);
            mGlowpad.setTargetDescriptionsResourceId(targetDescriptionsResourceId);
            mGlowpad.setDirectionDescriptionsResourceId(directionDescriptionsResourceId);
            mGlowpad.setHandleDrawable(handleDrawableResourceId);
            mGlowpad.reset(false);
        }
    }

    private boolean isCannedResponsePopupShowing() {
        if (mCannedResponsePopup != null) {
            return mCannedResponsePopup.isShowing();
        }
        return false;
    }

    private boolean isCustomMessagePopupShowing() {
        if (mCustomMessagePopup != null) {
            return mCustomMessagePopup.isShowing();
        }
        return false;
    }

    /**
     * Dismiss the canned response list popup.
     *
     * This is safe to call even if the popup is already dismissed, and even if you never called
     * showRespondViaSmsPopup() in the first place.
     */
    private void dismissCannedResponsePopup() {
        if (mCannedResponsePopup != null) {
            mCannedResponsePopup.dismiss();  // safe even if already dismissed
            mCannedResponsePopup = null;
        }
    }

    /**
     * Dismiss the custom compose message popup.
     */
    private void dismissCustomMessagePopup() {
       if (mCustomMessagePopup != null) {
           mCustomMessagePopup.dismiss();
           mCustomMessagePopup = null;
       }
    }

    public void dismissPendingDialogs() {
        if (isCannedResponsePopupShowing()) {
            dismissCannedResponsePopup();
        }

        if (isCustomMessagePopupShowing()) {
            dismissCustomMessagePopup();
        }
    }

    public boolean hasPendingDialogs() {
        return !(mCannedResponsePopup == null && mCustomMessagePopup == null);
    }

    /**
     * Shows the custom message entry dialog.
     */
    public void showCustomMessageDialog() {
        // Create an alert dialog containing an EditText
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final EditText et = new EditText(builder.getContext());
        builder.setCancelable(true).setView(et)
                .setPositiveButton(R.string.custom_message_send,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // The order is arranged in a way that the popup will be destroyed when the
                        // InCallActivity is about to finish.
                        final String textMessage = et.getText().toString().trim();
                        dismissCustomMessagePopup();
//                        getPresenter().rejectCallWithMessage(textMessage);
                        Log.d(this, "Call rejected with message");
                        LOGD(TAG, "Call rejected with mesage" + textMessage);
                        MainActivity.hangupCall();
                        callActivity.finish();

                    }
                })
                .setNegativeButton(R.string.custom_message_cancel,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismissCustomMessagePopup();
                        Log.d(this, "Dismiss with custom message");

//        getPresenter().onDismissDialog();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        dismissCustomMessagePopup();
                        Log.d(this, "Dismiss with custom message");
//                        getPresenter().onDismissDialog();
                    }
                })
                .setTitle(R.string.respond_via_sms_custom_message);
        mCustomMessagePopup = builder.create();

        // Enable/disable the send button based on whether there is a message in the EditText
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final Button sendButton = mCustomMessagePopup.getButton(
                        DialogInterface.BUTTON_POSITIVE);
                sendButton.setEnabled(s != null && s.toString().trim().length() != 0);
            }
        });

        // Keyboard up, show the dialog
        mCustomMessagePopup.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        mCustomMessagePopup.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        mCustomMessagePopup.show();

        // Send button starts out disabled
        final Button sendButton = mCustomMessagePopup.getButton(DialogInterface.BUTTON_POSITIVE);
        sendButton.setEnabled(false);
    }

    public void configureMessageDialog(List<String> textResponses) {
        mSmsResponses.clear();
        mSmsResponses.addAll(textResponses);
        mSmsResponses.add(getResources().getString(
                R.string.respond_via_sms_custom_message));
        if (mSmsResponsesAdapter != null) {
            mSmsResponsesAdapter.notifyDataSetChanged();
        }
    }

    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onAnswer(int videoState, Context context) {
        Log.d(this, "onAnswer videoState=" + videoState + " context=" + context);
        LOGD(TAG, "Answered" + context);

                MainActivity.acceptCall(mGlowpad);
        callActivity.showEndCallButton();
    }

    @Override
    public void onDecline(Context context)
    {
//        Log.d(this, "onDecline  context=" + context);
        LOGD(TAG, "Declined" +context);
        MainActivity.hangupCall();
        callActivity.finish();

    }

    @Override
    public void onDeclineUpgradeRequest(Context context) {
        Log.d(this, "onDeclineUpgrade  context=" + context);
    }

    @Override
    public void onText() {
        showCustomMessageDialog();
        Log.d(this, "onTest ");



    }

    /**
     * OnItemClickListener for the "Respond via SMS" popup.
     */
    public class RespondViaSmsItemClickListener implements AdapterView.OnItemClickListener {

        /**
         * Handles the user selecting an item from the popup.
         */
        @Override
        public void onItemClick(AdapterView<?> parent,  // The ListView
                View view,  // The TextView that was clicked
                int position, long id) {
            Log.d(this, "RespondViaSmsItemClickListener.onItemClick(" + position + ")...");
            final String message = (String) parent.getItemAtPosition(position);
            Log.v(this, "- message: '" + message + "'");
            dismissCannedResponsePopup();

            // The "Custom" choice is a special case.
            // (For now, it's guaranteed to be the last item.)
            if (position == (parent.getCount() - 1)) {
                // Show the custom message dialog
                showCustomMessageDialog();
            } else {
//                getPresenter().rejectCallWithMessage(message);
                Log.d(this, "Message rejected with message=");

            }

        }
    }
}
