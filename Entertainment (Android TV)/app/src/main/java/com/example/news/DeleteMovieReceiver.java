package com.example.news;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import java.io.File;

public class DeleteMovieReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name").replace(':','_').replace(' ','_');
        new File(context.getFilesDir(),"download_id_for_"+name+".txt").delete();
        new File(context.getExternalFilesDir(null),name+".mp4").delete();
        new File(context.getFilesDir(),"isDownloading.txt").delete();
        new File(context.getFilesDir(),"isPaused.txt").delete();
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.remove(Long.parseLong(intent.getStringExtra("download_id")));
        DownloadFileData.setPercent(0);
        DownloadFileData.setText("Starting Download");
        NotificationManagerCompat.from(context).cancel(18);
    }
}
