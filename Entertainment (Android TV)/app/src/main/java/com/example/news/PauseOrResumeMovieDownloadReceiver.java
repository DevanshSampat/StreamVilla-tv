package com.example.news;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PauseOrResumeMovieDownloadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ContentValues values = new ContentValues();
        ContentResolver contentResolver = context.getContentResolver();
        if(intent.getStringExtra("status").equals("pause"))
            values.put("control",1);
        else values.put("control",0);
        contentResolver.update(Uri.parse("content://downloads/my_downloads"),values,"title=?",new String[]{intent.getStringExtra("name")});
        Intent sendIntent = new Intent("PAUSE_OR_RESUME_DOWNLOAD");
        sendIntent.putExtra("status",intent.getStringExtra("status"));
        context.sendBroadcast(sendIntent);
    }
}
