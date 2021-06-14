package com.example.news;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneIncomingCallListener extends PhoneStateListener {
    private Context context;
    public PhoneIncomingCallListener(Context context){this.context = context;}
    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        if(state == TelephonyManager.CALL_STATE_RINGING) context.sendBroadcast(new Intent("PAUSE"));
        else context.sendBroadcast(new Intent("PLAY"));
    }
}
