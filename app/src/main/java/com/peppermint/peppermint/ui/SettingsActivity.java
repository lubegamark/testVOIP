package com.peppermint.peppermint.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;
import android.view.View;

import com.peppermint.peppermint.R;

/**
 * Created by mark on 7/12/15.
 */
public class SettingsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toolbar.setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
