package com.example.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QueuedDownloadsActivity extends AppCompatActivity {

    private List<String> names;
    private RecyclerView recyclerView;
    private boolean dark;
    private boolean longClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        dark = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queued_downloads);
        names = new ArrayList<>();
        recyclerView = findViewById(R.id.recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new QueueAdapter(getIntent().hasExtra("dark")||isInDarkMode(),this));
        if(isInDarkMode()) setDarkMode();
        findViewById(R.id.all_downloads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ContentDisplayActivity.class);
                intent.putExtra("download",true);
                intent.putExtra("content_type","download");
                if(dark) intent.putExtra("dark",true);
                startActivity(intent);
                if(!new File(getApplicationContext().getFilesDir(),"isDownloading.txt").exists()) finish();
            }
        });
        if(!new File(getApplicationContext().getFilesDir(),"isDownloading.txt").exists()){
            findViewById(R.id.all_downloads).callOnClick();
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(DownloadFileData.getText()==null) new DownloadNotification()
                        .generateDownloadNotification(getApplicationContext(),QueuedDownloadsActivity.this,getIntent().hasExtra("dark"));
            }
        },2000);
        findViewById(R.id.current_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(longClick){
                    longClick = false;
                    return;
                }
                Intent intent = new Intent(QueuedDownloadsActivity.this,SearchActivity.class);
                if(getIntent().hasExtra("dark")) intent.putExtra("dark",true);
                intent.putExtra("search",((TextView)findViewById(R.id.recent_name)).getText());
                intent.putExtra("signed_in",true);
                intent.putExtra("open",true);
                startActivity(intent);
            }
        });
        findViewById(R.id.current_card).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                longClick = true;
                Intent intent = new Intent(getApplicationContext(),PauseOrResumeMovieDownloadReceiver.class);
                intent.putExtra("name",((TextView)findViewById(R.id.recent_name)).getText());
                if(((TextView)findViewById(R.id.time_left)).getText().toString().equals("Long Press to Resume"))
                    intent.putExtra("status","resume");
                else intent.putExtra("status","pause");
                sendBroadcast(intent);
                return false;
            }
        });
        getDownloadStatus();
    }

    private boolean isInDarkMode() {
        try{
            FileInputStream fis = openFileInput("Theme.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            return br.readLine().equals("true");
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setDarkMode(){
        ((CardView)findViewById(R.id.current_card)).setCardBackgroundColor(Color.parseColor("#363636"));
        ((TextView)findViewById(R.id.recent_name)).setTextColor(Color.WHITE);
        ((TextView)findViewById(R.id.recent_text)).setTextColor(Color.parseColor("#CCCCCC"));
        ((TextView)findViewById(R.id.recent_percent)).setTextColor(Color.parseColor("#CCCCCC"));
        ((TextView)findViewById(R.id.time_left)).setTextColor(Color.parseColor("#CCCCCC"));
        ((TextView)findViewById(R.id.speed)).setTextColor(Color.parseColor("#CCCCCC"));
        findViewById(R.id.layout).setBackgroundColor(Color.BLACK);
        dark = true;
    }
    @SuppressLint("SetTextI18n")
    private void getDownloadStatus() {
        try {
            FileInputStream fis = openFileInput("isDownloading.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            ((TextView) findViewById(R.id.recent_name)).setText(br.readLine());
            ((ProgressBar) findViewById(R.id.progress)).setProgress(DownloadFileData.getPercent());
            ((TextView) findViewById(R.id.recent_text)).setText(DownloadFileData.getText());
            ((TextView) findViewById(R.id.recent_percent)).setText(DownloadFileData.getPercent() + "%");
            ((TextView) findViewById(R.id.time_left)).setText(DownloadFileData.getTimeLeft());
            ((TextView) findViewById(R.id.speed)).setText(DownloadFileData.getSpeed());
            try {
                if (DownloadFileData.getText().equals("Long Press to Resume")) {
                    ((TextView) findViewById(R.id.recent_text)).setText(DownloadFileData.getDownloadBackup());
                    ((TextView) findViewById(R.id.recent_percent)).setText(DownloadFileData.getPercent() + "%");
                    ((TextView) findViewById(R.id.time_left)).setText(DownloadFileData.getText());
                    ((TextView) findViewById(R.id.speed)).setText("0 BPS");
                }
            } catch (Exception exception) {}
            fis = openFileInput("PendingDownloads.txt");
            br = new BufferedReader(new InputStreamReader(fis));
            String str;
            names.clear();
            while ((str=br.readLine())!=null) names.add(str.substring(0,str.indexOf('\t')));
            ((QueueAdapter) Objects.requireNonNull(recyclerView.getAdapter())).setNames(names);
        } catch (FileNotFoundException e) {
            finish();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getDownloadStatus();
            }
        },1000);
    }

    public void cancelCurrentDownload(View view) {
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = 0;
        try {
            FileInputStream fis = openFileInput("download_id_for_"
                    + ((TextView)findViewById(R.id.recent_name)).getText().toString().replace(' ', '_').replace(':', '_') + ".txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            downloadId = Long.parseLong(br.readLine());
            Intent intent = new Intent(getApplicationContext(),DeleteMovieReceiver.class);
            intent.putExtra("name",((TextView)findViewById(R.id.recent_name)).getText());
            intent.putExtra("download_id",downloadId+"");
            sendBroadcast(intent);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}