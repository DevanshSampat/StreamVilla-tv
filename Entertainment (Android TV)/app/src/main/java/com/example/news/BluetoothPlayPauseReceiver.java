package com.example.news;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class BluetoothPlayPauseReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent keyEvent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        switch (keyEvent.getKeyCode())
        {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                context.sendBroadcast(new Intent("PLAY"));
                break;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                context.sendBroadcast(new Intent("PAUSE"));
                break;
            case  KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                context.sendBroadcast(new Intent("TOGGLE"));
        }
    }
}
