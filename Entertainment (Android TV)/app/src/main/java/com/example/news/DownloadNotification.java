package com.example.news;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.news.DeleteMovieReceiver;
import com.example.news.DownloadFileData;
import com.example.news.R;
import com.example.news.SearchActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.content.Context.DOWNLOAD_SERVICE;

public class DownloadNotification {
    private long downloadId;
    private boolean downloading=false;
    private String name;
    private Context context;
    private Activity activity;
    private boolean dark;
    private boolean isRegistered = false;
    private boolean toastmsg = true;
    private boolean isPaused = false;
    private long bytesStored;
    private long time;
    private long count;
    public DownloadNotification(){}
    public DownloadNotification(boolean toastmsg){this.toastmsg = toastmsg;}
    public void generateDownloadNotification(final Context context,final Activity activity, final boolean dark)
    {
        count = 0;
        time=System.currentTimeMillis();
        this.context = context;
        this.activity = activity;
        this.dark = dark;
        isPaused = new File(context.getFilesDir(),"isPaused.txt").exists();
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isPaused = intent.getStringExtra("status").equals("pause");
                if(!new File(context.getFilesDir(),"isPaused.txt").exists()&&isPaused){
                    try {
                        new File(context.getFilesDir(),"isPaused.txt").createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else if(!isPaused) new File(context.getFilesDir(),"isPaused.txt").delete();
            }
        };
        context.registerReceiver(broadcastReceiver,new IntentFilter("PAUSE_OR_RESUME_DOWNLOAD"));
        isRegistered = true;
        try {
            FileInputStream fis = context.openFileInput("isDownloading.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            name = br.readLine();
            fis = context.openFileInput("download_id_for_"+name.replace(' ','_').replace(':','_')+".txt");
            br = new BufferedReader(new InputStreamReader(fis));
            try{
                downloadId = Long.parseLong(br.readLine());
            } catch (NumberFormatException e) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        final DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloading = true;
                while (downloading) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    try {
                        Cursor cursor = manager.query(query);
                        cursor.moveToFirst();
                        try {
                            final long bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            final long bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                            final int progress = (int) ((100 * bytes_downloaded) / bytes_total);
                            if(!new File(context.getFilesDir(),"isDownloading.txt").exists()){
                                goToNextTask();
                                return;
                            }
                            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                                NotificationManagerCompat.from(context).cancel(108);
                                downloading = false;
                                new File(context.getFilesDir(), "download_id_for_" +
                                        name.replace(' ', '_').replace(':', '_') + ".txt").delete();
                                Intent intent = new Intent(context, SearchActivity.class);
                                intent.putExtra("search", name);
                                intent.putExtra("open", true);
                                intent.putExtra("signed_in", true);
                                if (dark) intent.putExtra("dark", true);
                                DownloadFileData.setText("Starting Download");
                                DownloadFileData.setPercent(0);
                                DownloadFileData.setTimeLeft("");
                                DownloadFileData.setSpeed("");
                                Notification notification = new NotificationCompat.Builder(context, "general")
                                        .setAutoCancel(true)
                                        .setContentTitle(name)
                                        .setContentText("Download Complete")
                                        .setColor(Color.RED)
                                        .setSmallIcon(R.drawable.notification)
                                        .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                                        .build();
                                NotificationManagerCompat.from(context).cancel(18);
                                NotificationManagerCompat.from(context).notify(108, notification);
                                new File(context.getFilesDir(), "isDownloading.txt").delete();
                                goToNextTask();
                                return;
                            } else if (!checkInternetConnection()&&!isPaused) {
                                Intent intent = new Intent(context, QueuedDownloadsActivity.class);
                                intent.putExtra("search", name);
                                intent.putExtra("open", true);
                                intent.putExtra("signed_in", true);
                                DownloadFileData.setText("Waiting for Network");
                                DownloadFileData.setTimeLeft("");
                                DownloadFileData.setSpeed("");
                                if (dark) intent.putExtra("dark", true);
                                NotificationManagerCompat.from(context).notify(18,
                                        new NotificationCompat.Builder(context, "personal")
                                                .setOngoing(true)
                                                .setSmallIcon(R.drawable.notification)
                                                .setContentTitle(name)
                                                .setColor(Color.RED)
                                                .setContentText("Waiting for network")
                                                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                                                .build());
                            } else if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_FAILED) {
                                NotificationManagerCompat.from(context).cancel(18);
                                Intent intent = new Intent(context, SearchActivity.class);
                                intent.putExtra("search", name);
                                intent.putExtra("open", true);
                                intent.putExtra("signed_in", true);
                                DownloadFileData.setText("Starting Download");
                                DownloadFileData.setTimeLeft("");
                                DownloadFileData.setSpeed("");
                                if (dark) intent.putExtra("dark", true);
                                NotificationManagerCompat.from(context).notify(18,
                                        new NotificationCompat.Builder(context, "general")
                                                .setSmallIcon(R.drawable.notification)
                                                .setContentTitle(name)
                                                .setColor(Color.RED)
                                                .setContentText("Download Failed")
                                                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                                                .setAutoCancel(true)
                                                .build());
                                downloading = false;
                                new File(context.getFilesDir(), "isDownloading.txt").delete();
                                new File(context.getFilesDir(), "download_id_for_" +
                                        name.replace(' ', '_').replace(':', '_') + ".txt").delete();
                                manager.remove(downloadId);
                                goToNextTask();
                                return;
                            }
                            else if (isPaused) {
                                Intent intent = new Intent(context, QueuedDownloadsActivity.class);
                                intent.putExtra("search", name);
                                intent.putExtra("open", true);
                                intent.putExtra("signed_in", true);
                                Intent cancelIntent = new Intent(context, DeleteMovieReceiver.class);
                                cancelIntent.putExtra("name", name);
                                cancelIntent.putExtra("download_id", String.valueOf(downloadId));
                                PendingIntent pendingIntentForCancellation = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                DownloadFileData.setText("Long Press to Resume");
                                DownloadFileData.setTimeLeft("");
                                DownloadFileData.setSpeed("");
                                if (dark) intent.putExtra("dark", true);
                                Intent resumeIntent = new Intent(context,PauseOrResumeMovieDownloadReceiver.class);
                                resumeIntent.putExtra("name",name);
                                resumeIntent.putExtra("status","resume");
                                String str;
                                if(DownloadFileData.getPercent()!=0) str = " ("+DownloadFileData.getPercent()+"%)";
                                else str = "";
                                PendingIntent pendingResumeIntent = PendingIntent.getBroadcast(context,0,resumeIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                                NotificationManagerCompat.from(context).notify(18,
                                        new NotificationCompat.Builder(context, "personal")
                                                .setOngoing(true)
                                                .setSmallIcon(R.drawable.notification)
                                                .setContentTitle(name)
                                                .setColor(Color.RED)
                                                .addAction(R.drawable.notification,"Resume",pendingResumeIntent)
                                                .addAction(R.drawable.notification,"Cancel",pendingIntentForCancellation)
                                                .setContentText("Download Paused"+ str)
                                                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                                                .build());
                            }
                            else {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (bytes_total >= bytes_downloaded) {
                                            time = System.currentTimeMillis();
                                            if(time-DownloadFileData.getTime()>1000) {
                                                DownloadFileData.setTime(time);
                                                DownloadFileData.setSize(bytes_total);
                                                DownloadFileData.setDownloadedBytes(bytes_downloaded);
                                            }
                                            String[] unit = new String[]{"B","KB","MB","GB"};
                                            String current = "", total = "";
                                            double down = bytes_downloaded;
                                            double total_bytes = bytes_total;
                                            int i=0;
                                            current="";
                                            total="";
                                            while(down>1024&&i<3)
                                            {
                                                current = String.valueOf(down/1024.0);
                                                try {
                                                    current = current.substring(0, current.indexOf('.') + 3);
                                                } catch (Exception exception) {
                                                    exception.printStackTrace();
                                                }
                                                down = down/1024;
                                                i++;
                                            }
                                            if(current.length()==0) current="0";
                                            current = current+" "+unit[i];
                                            i=0;
                                            while(total_bytes>1024&&i<3)
                                            {
                                                total = String.valueOf(total_bytes/1024.0);
                                                try {
                                                    total = total.substring(0, total.indexOf('.') + 3);
                                                } catch (Exception exception) {
                                                    exception.printStackTrace();
                                                }
                                                total_bytes = total_bytes/1024;
                                                i++;
                                            }
                                            total = total+" "+unit[i];
                                            if (new File(context.getFilesDir(), "download_id_for_" + name
                                                    .replace(':', '_').replace(' ', '_') + ".txt").exists()) {
                                                Intent cancelIntent = new Intent(context, DeleteMovieReceiver.class);
                                                cancelIntent.putExtra("name", name);
                                                cancelIntent.putExtra("download_id", String.valueOf(downloadId));
                                                PendingIntent pendingIntentForCancellation = PendingIntent.getBroadcast(context, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                Intent pauseIntent = new Intent(context,PauseOrResumeMovieDownloadReceiver.class);
                                                pauseIntent.putExtra("status","pause");
                                                pauseIntent.putExtra("name",name);
                                                PendingIntent pendingPauseIntent = PendingIntent.getBroadcast(context,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                                                if(dark) cancelIntent.putExtra("dark",true);
                                                Intent intent = new Intent(context, QueuedDownloadsActivity.class);
                                                intent.putExtra("name", name);
                                                intent.putExtra("download",true);
                                                intent.putExtra("open", true);
                                                intent.putExtra("signed_in", true);
                                                if (dark) intent.putExtra("dark", true);
                                                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                                String title = name;
                                                DownloadFileData.setText(current + " / " + total);
                                                DownloadFileData.setPercent(progress);
                                                Notification notification = new NotificationCompat.Builder(context, "personal")
                                                        .setOngoing(true)
                                                        .setContentTitle(title)
                                                        .setContentText(progress+"%")
                                                        .setSmallIcon(R.drawable.notification)
                                                        .setColor(Color.RED)
                                                        .setContentIntent(pendingIntent)
                                                        .addAction(R.drawable.notification,"Pause",pendingPauseIntent)
                                                        .addAction(R.drawable.notification,"Cancel",pendingIntentForCancellation)
                                                        .setProgress(100, progress, false)
                                                        .build();
                                                NotificationManagerCompat.from(context).notify(18, notification);
                                            } else {
                                                NotificationManagerCompat.from(context).cancel(18);
                                            }
                                            if (current.equals(total)) {
                                                new File(context.getFilesDir(), "download_id_for_" +
                                                        name.replace(' ', '_').replace(':', '_') + ".txt").delete();
                                            }
                                        }
                                    }
                                });
                            }
                        } catch (Exception exception) {
                            downloading = false;
                            downloadId = 0L;
                            NotificationManagerCompat.from(context).cancel(18);
                            return;
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }).start();
    }
    public void goToNextTask()
    {
        try{
            FileInputStream fis = context.openFileInput("PendingDownloads.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String str = br.readLine();
            String other = "";
            String content;
            while((content=br.readLine())!=null) {
                other = other + content + "\n";
            }
            if(str==null) return;
            String name = str;
            name = name.substring(0,name.indexOf('\t'));
            String link = str;
            link = link.substring(link.indexOf('\t')+1);
            startDownloading(name,link);
            FileOutputStream fileOutputStream = context.openFileOutput("PendingDownloads.txt",Context.MODE_PRIVATE);
            fileOutputStream.write(other.getBytes());
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void startDownloading(String name,String link)
    {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
        request.setTitle(name);
        request.setDescription("Entertainment");
        request.allowScanningByMediaScanner();
        request.setDestinationInExternalFilesDir(context, "",
                name.replace(' ', '_').replace(':', '_') + ".mp4");
        request.setVisibleInDownloadsUi(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        long id = manager.enqueue(request);
        if(!new File(context.getFilesDir(),"download_id_for_"+name.replace(' ', '_').replace(':', '_')+".txt").exists())
        {
            try {
                new File(context.getFilesDir(),"download_id_for_"+name.replace(' ', '_').replace(':', '_')+".txt").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = context.openFileOutput("download_id_for_"+name.replace(' ', '_').replace(':', '_')+".txt",Context.MODE_PRIVATE);
            fos.write((id+"\n").getBytes());
            fos.close();
            if(!new File(context.getFilesDir(), "isDownloading.txt").exists()) new File(context.getFilesDir(), "isDownloading.txt").createNewFile();
            fos = context.openFileOutput("isDownloading.txt",Context.MODE_PRIVATE);
            fos.write(name.getBytes());
            fos.close();
            generateDownloadNotification(context,activity,dark);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void goToNextTask(Context context, boolean dark)
    {
        this.context = context;
        this.activity = (Activity)context;
        this.dark = dark;
        goToNextTask();
    }
    public void enqueueDownload(Context context,String name, String link){
        if(!new File(context.getFilesDir(),"PendingDownloads.txt").exists()) {
            try {
                new File(context.getFilesDir(),"PendingDownloads.txt").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!isAlreadyEnqueued(context, name)){
            try {
                FileOutputStream fos = context.openFileOutput("PendingDownloads.txt",Context.MODE_APPEND);
                fos.write((name+"\t"+link+"\n").getBytes());
                fos.close();
                if(toastmsg) Toast.makeText(context,name+" is added to the queue",Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(toastmsg) Toast.makeText(context,name+" exists in queue",Toast.LENGTH_SHORT).show();
    }
    public void dequeue(Context context, String name){
        try {
            FileInputStream fis = context.openFileInput("PendingDownloads.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String str;
            String dataToPut = "";
            while ((str=br.readLine())!=null){
                if(!str.substring(0,str.indexOf('\t')).equals(name)) dataToPut = dataToPut+str+"\n";
            }
            FileOutputStream fos = context.openFileOutput("PendingDownloads.txt",Context.MODE_PRIVATE);
            fos.write(dataToPut.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public boolean isAlreadyEnqueued(Context context, String name) {
        try {
            FileInputStream fis = context.openFileInput("PendingDownloads.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String str;
            while((str=br.readLine())!=null){
                if(str.substring(0,str.indexOf('\t')).equals(name)) return true;
            }
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean checkInternetConnection()
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetwork()==null)
        {
            return false;
        }
        return true;
    }
}
