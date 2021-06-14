package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class RestoreDownloadsActivity extends AppCompatActivity {

    private boolean download=false;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_downloads);
        textView = findViewById(R.id.main_text);
        if(new File(getApplicationContext().getFilesDir(),"DownloadedVideos.txt").exists()) finish();
        textView.setText(textView.getText().toString()+getIntent().getStringExtra("restore"));
        if(getIntent().hasExtra("dark"))
        {
            textView.setTextColor(Color.parseColor("#CCCCCC"));
            findViewById(R.id.restore_layout).setBackgroundColor(Color.parseColor("#000000"));
        }
        findViewById(R.id.restore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download=true;
                FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!download) return;
                        String temp = getIntent().getStringExtra("restore");
                        int endCount=-1;
                        while (endCount<temp.length()-1)
                        {
                            int i;
                            for(i=endCount+1;temp.charAt(i)!='\n';i++);
                            String name = temp.substring(endCount+1,i);
                            endCount=i;
                            for(i=1;i<=Integer.parseInt(snapshot.child("movie").getValue().toString());i++)
                            {
                                if(name.equals(snapshot.child("movie"+i).getValue().toString()))
                                {
                                    new DownloadNotification(false).enqueueDownload(RestoreDownloadsActivity.this,name,
                                            snapshot.child("movielink"+i).getValue().toString().substring("(video)".length()));
                                }
                            }
                            for(i=1;i<=Integer.parseInt(snapshot.child("classic").getValue().toString());i++)
                            {
                                if(name.equals(snapshot.child("classic"+i).getValue().toString())) {
                                    new DownloadNotification(false).enqueueDownload(RestoreDownloadsActivity.this,name,
                                            snapshot.child("classiclink"+i).getValue().toString().substring("(video)".length()));
                                }
                            }
                        }
                        Toast.makeText(getApplicationContext(),"Starting Downloads",Toast.LENGTH_SHORT).show();
                        DownloadFileData.setPercent(0);
                        DownloadFileData.setText("Starting Download");
                        new DownloadNotification().goToNextTask(RestoreDownloadsActivity.this,getIntent().hasExtra("dark"));
                        startActivity(new Intent(getApplicationContext(),QueuedDownloadsActivity.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        findViewById(R.id.skip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
}