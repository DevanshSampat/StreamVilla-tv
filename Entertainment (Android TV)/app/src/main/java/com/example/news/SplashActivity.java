package com.example.news;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SplashActivity extends AppCompatActivity {

    private boolean dark=false;
    private long time;
    private long downloadId=0;
    private boolean downloading=false;
    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        time=System.currentTimeMillis();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setAppropriateTheme();
        if (!checkInternetConnection()) {
            Intent intent = new Intent(this, WebActivity.class);
            intent.putExtra("link", "null");
            if(dark) intent.putExtra("dark",true);
            startActivity(intent);
            finish();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                getMessage();
            }
        }).start();
        TextView textView = (TextView) findViewById(R.id.name);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.up_start);
        ImageView imageView = (ImageView) findViewById(R.id.icon);
        imageView.startAnimation(animation);
        textView.startAnimation(animation);
    }
    private void getMessage()
    {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("messages");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(System.currentTimeMillis()>time+500) resumeDownloads();
                TextView textView = (TextView) findViewById(R.id.message);
                textView.setText(snapshot.child("start_message").getValue().toString());
                final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
                ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar,"progress",0,100);
                progressAnimator.setDuration(1000);
                progressAnimator.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this,MainActivity.class);
                        if(getIntent().hasExtra("search")) intent.putExtra("search",getIntent().getStringExtra("search"));
                        if(getIntent().hasExtra("open")) intent.putExtra("open",true);
                        if(getIntent().hasExtra("personal")) intent.putExtra("personal",true);
                        startActivity(intent);
                        getIntent().removeExtra("search");
                        getIntent().removeExtra("personal");
                        getIntent().removeExtra("open");
                        overridePendingTransition(R.anim.zoom_in,R.anim.zoom_out);
                        SplashActivity.this.finish();
                    }
                },1008);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkInternetConnection()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetwork()==null)
        {
            return false;
        }
        return true;
    }
    private void setDarkTheme()
    {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.splash);
        relativeLayout.setBackgroundColor(Color.parseColor("#000000"));
        TextView textView = (TextView) findViewById(R.id.name);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.message);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        dark=true;
    }
    private void setAppropriateTheme()
    {
        try {
            FileInputStream fis = null;
            fis = this.openFileInput("Theme.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String str;
            sb.append(br.readLine());
            while ((str=br.readLine())!=null)
            {
                sb.append(str);
            }
            TextView textView = (TextView) findViewById(R.id.app_info);
            if(sb.toString().equals("true"))setDarkTheme();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void resumeDownloads() {
        if(new File(getFilesDir(),"isDownloading.txt").exists()&&DownloadFileData.getText()==null)
            new DownloadNotification().generateDownloadNotification(getApplicationContext(),this,dark);
        if(!new File(getFilesDir(),"PendingDownloads.txt").exists()){
            try {
                new File(getFilesDir(),"PendingDownloads.txt").createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}