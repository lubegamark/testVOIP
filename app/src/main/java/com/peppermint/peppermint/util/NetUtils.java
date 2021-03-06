/*
 * Copyright 2014 Google Inc. All rights reserved.
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

package com.peppermint.peppermint.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;

import com.peppermint.peppermint.Config;

import static com.peppermint.peppermint.util.LogUtils.LOGD;
import static com.peppermint.peppermint.util.LogUtils.LOGE;
import static com.peppermint.peppermint.util.LogUtils.makeLogTag;


public class NetUtils {
    private static final String TAG = makeLogTag(NetUtils.class);
    private static String mUserAgent = null;

    public static String getUserAgent(Context mContext) {
        if (mUserAgent == null) {
            mUserAgent = Config.APP_NAME;
            try {
                String packageName = mContext.getPackageName();
                String version = mContext.getPackageManager().getPackageInfo(packageName, 0).versionName;
                mUserAgent = mUserAgent + " (" + packageName + "/" + version + ")";
                LOGD(TAG, "User agent set to: " + mUserAgent);
            } catch (PackageManager.NameNotFoundException e) {
                LOGE(TAG, "Unable to find self by package name", e);
            }
        }
        return mUserAgent;
    }

    public static boolean isDataConnected(Context c) {
        try {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();

        } catch (Exception e) {

            return false;
        }
    }
}
