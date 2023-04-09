package com.example.seniordesign2;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.SmsManager;

import androidx.room.Insert;

/**
 * Handler for SMS messaging
 */
public class SMSHandler {

    //
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 7);
//        }
//        SMSHandler smsHandler = new SMSHandler(this);
//        smsHandler.sendSMS("test message", "");
    Context context;
    SmsManager smsManager;

    public SMSHandler(Context context) {
        this.context = context;
        smsManager = context.getSystemService(SmsManager.class);
    }

    public void sendSMS(String message, String phone) {
        smsManager.sendTextMessage(phone, null, message, null, null);
    }

}