package com.peppermint.peppermint.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.peppermint.peppermint.model.User;
import com.peppermint.peppermint.ui.UserListActivity;

import org.json.JSONObject;

/**
 * Created by mark on 7/28/15.
 */
public class AccountUtils {
public static void registerAccount(User user, Context context){

    AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
    final Account account = new Account(user.getEmail(), "com.peppermint.peppermint");

    final Bundle extraData = new Bundle();

    extraData.putInt("id", user.getId());

    am.addAccountExplicitly(account,  "UYHJOIUHYKJ$%^&*76543", extraData);
    am.getUserData(account, "id");
    Intent openStartingPoint;
    openStartingPoint = new Intent(context, UserListActivity.class);
    context.startActivity(openStartingPoint);

}

}
