package com.peppermint.peppermint.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle Splash) {
		super.onCreate(Splash);
		//setContentView(R.layout.splash);
		AccountManager am = (AccountManager) MainActivity.this.getSystemService(Context.ACCOUNT_SERVICE);
        Account [] accounts = am.getAccountsByType("com.peppermint.peppermint");
        Account account =null;
        if (accounts.length > 0) {
        account = accounts[0];
        Log.i("Account Name", account.name);
        }
               
        Intent openStartingPoint;   
        if (account == null) {
           	openStartingPoint = new Intent(MainActivity.this, LoginActivity.class);
           }else{
           	openStartingPoint = new Intent(MainActivity.this, UserListActivity.class);
           }
		startActivity(openStartingPoint);

	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.mevibe, menu);
		return true;
	}

}
