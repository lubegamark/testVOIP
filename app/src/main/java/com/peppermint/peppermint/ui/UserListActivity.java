package com.peppermint.peppermint.ui;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.peppermint.peppermint.R;
import com.peppermint.peppermint.service.CallService;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;


public class UserListActivity extends BaseActivity {
    private static final String TAG = makeLogTag(UserListActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccountManager am = (AccountManager) UserListActivity.this.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = am.getAccountsByType("com.peppermint.peppermint");
        Account account =null;
        if (accounts.length > 0) {
            account = accounts[0];
            Log.i("Account Name", account.name);
        }

        Intent openStartingPoint;
        if (account == null) {
            openStartingPoint = new Intent(UserListActivity.this, LoginActivity.class);
            startActivity(openStartingPoint);
        }else{

            setContentView(R.layout.activity_main);

            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            Intent serviceIntent = new Intent(this, CallService.class);
            startService(serviceIntent);

        }



    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            LOGD(TAG, "settings clicked");
        }

        return super.onOptionsItemSelected(item);
    }
}
