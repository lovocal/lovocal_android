package com.lovocal.smsreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.lovocal.LavocalApplication;
import com.lovocal.bus.SmsVerification;
import com.lovocal.utils.Logger;

/**
 * Created by anshul1235 on 18/07/14.
 */
public class IncomingSms extends BroadcastReceiver {

    /**
     * SmsManager to get the sms in the receiver
     */
    private final SmsManager mSms = SmsManager.getDefault();
    private String mMessage;


    private static final String TAG = "IncomingSms";

    @Override
    public void onReceive(Context context, Intent intent) {

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    String senderNum = phoneNumber;
                    mMessage = currentMessage.getDisplayMessageBody();

                    ((LavocalApplication) context.getApplicationContext()).getBus().post(new SmsVerification(mMessage));
                    Logger.d(TAG, "SmsReceiver : senderNum: " + senderNum + "; message: " + mMessage);


                    // Show Alert
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context,
                            "senderNum: " + senderNum + ", message: " + mMessage, duration);
                    toast.show();

                } // end for loop
            } // bundle is null

        } catch (Exception e) {
            Logger.e(TAG, "Exception smsReceiver" + e);

        }
    }
}
