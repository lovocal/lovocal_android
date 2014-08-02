

package com.lovocal.http;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.lovocal.LavocalApplication;
import com.lovocal.utils.Utils;

import com.lovocal.utils.AppConstants.DeviceInfo;

/**
 * Receiver for monitoring network changes
 * 
 * @author Vinay S Shenoy
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if ((intent.getAction() != null)
                        && intent.getAction()
                                        .equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            Utils.setupNetworkInfo(context);
            if (DeviceInfo.INSTANCE.isNetworkConnected()) {
                LavocalApplication.startChatService();
            }
        }
    }

}
