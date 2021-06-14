package com.example.news;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.File;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.println(Log.ASSERT,"Boot Status","Completed");
        if(new File(context.getFilesDir(),"isDownloading.txt").exists()&&DownloadFileData.getText()==null){
            Intent resumeIntent = new Intent(context,QueuedDownloadsActivity.class);
            resumeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"general")
                    .setSmallIcon(R.drawable.notification)
                    .setContentIntent(PendingIntent.getActivity(context,0,resumeIntent,PendingIntent.FLAG_ONE_SHOT))
                    .setContentTitle("Downloading Movie")
                    .setContentText("Tap for more info")
                    .setAutoCancel(true)
                    .setColor(Color.RED);
            NotificationManagerCompat.from(context).notify(18,builder.build());
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.MINUTE,0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmingIntent = new Intent(context,NotificationReceiver.class);
        alarmingIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,alarmingIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),6*AlarmManager.INTERVAL_HOUR,pendingIntent);
        Log.println(Log.ASSERT,"Alarm",calendar.get(Calendar.DAY_OF_MONTH)+"/"+(1+calendar.get(Calendar.MONTH))+" at "+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE));
    }
}
