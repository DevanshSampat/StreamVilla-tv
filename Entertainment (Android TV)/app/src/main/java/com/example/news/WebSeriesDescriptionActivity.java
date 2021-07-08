package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class WebSeriesDescriptionActivity extends AppCompatActivity {
    private String link;
    private String season;
    private String episode;
    private String combo;
    private String videoPosition;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_series_description);
        FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername(getApplicationContext())).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try{
                    ArrayList<String> watchList = (ArrayList<String>) documentSnapshot.get("Watchlist");
                    if(watchList.contains(getIntent().getStringExtra("movie_db")))
                        ((TextView)findViewById(R.id.text_watchlist)).setText("Remove from Watchlist");
                } catch (Exception e) {

                }

            }
        });
        findViewById(R.id.watchlist).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                if(((TextView)findViewById(R.id.text_watchlist)).getText().equals("Add to Watchlist")) {
                    new Sync().addToWatchList(getApplicationContext(), getIntent().getStringExtra("dbName"));
                    ((TextView)findViewById(R.id.text_watchlist)).setText("Remove from Watchlist");
                }
                else{
                    new Sync().removeFromWatchList(getApplicationContext(), getIntent().getStringExtra("dbName"));
                    ((TextView)findViewById(R.id.text_watchlist)).setText("Add to Watchlist");
                }
            }
        });
        findViewById(R.id.seasons_and_episodes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WebSeriesDescriptionActivity.this,SeasonPickerActivity.class);
                intent.putExtra("name",getIntent().getStringExtra("name"));
                intent.putExtra("dbName",getIntent().getStringExtra("dbName"));
                intent.putExtra("image",getIntent().getStringExtra("image"));
                printHistory(getIntent().getStringExtra("name"));
                startActivity(intent);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new Sync().uploadHistory(WebSeriesDescriptionActivity.this);
                        new Sync().addToQuickPicks(WebSeriesDescriptionActivity.this,getIntent().getStringExtra("dbName"));
                    }
                }).start();
            }
        });
        findViewById(R.id.watch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = getIntent().getStringExtra("name");
                final String dbName = getIntent().getStringExtra("dbName");
                final Intent intent = new Intent(WebSeriesDescriptionActivity.this,VideoPlayerActivity.class);
                intent.putExtra("name",name+" : S"+season+"E"+episode);
                intent.putExtra("raw_name",name+" : S"+season+"E");
                intent.putExtra("online",true);
                intent.putExtra("dbName",dbName+"s"+ season);
                intent.putExtra("episode_number",episode);
                intent.putExtra("position", videoPosition);
                intent.putExtra("link",link);
                printHistory(getIntent().getStringExtra("name"));
                startActivity(intent);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new Sync().uploadHistory(WebSeriesDescriptionActivity.this);
                        new Sync().addToQuickPicks(WebSeriesDescriptionActivity.this,getIntent().getStringExtra("dbName"));
                    }
                }).start();
             }
        });
        loadData();
    }

    private void loadData() {
        findViewById(R.id.buttons).setVisibility(View.GONE);
        Picasso.with(this).load(getIntent().getStringExtra("image")).into((ImageView)findViewById(R.id.image));
        ((TextView)findViewById(R.id.title)).setText(getIntent().getStringExtra("name"));
        ((TextView)findViewById(R.id.description)).setText(getIntent().getStringExtra("description"));
        season = "1";
        episode = "1";
        combo = "s1e1";
        videoPosition = "0";
        if(new File(getFilesDir(),getIntent().getStringExtra("name").replace(' ','+')+".txt").exists()){
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(openFileInput(getIntent().getStringExtra("name").replace(' ','+')+".txt")));
                String str = br.readLine();
                season = str.substring(0,str.indexOf('\t'));
                episode = str.substring(str.indexOf('\t')+1);
                if(episode.contains("\t")){
                    videoPosition = episode.substring(episode.indexOf('\t')+1);
                    episode = episode.substring(0,episode.indexOf("\t"));
                }
                combo = "s"+season+"e"+episode;
                ((TextView)findViewById(R.id.play_text)).setText("Continue : Season "+season+" - Episode "+episode);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FirebaseDatabase.getInstance().getReference(getIntent().getStringExtra("dbName")+combo).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                link = snapshot.getValue().toString();
                findViewById(R.id.buttons).setVisibility(View.VISIBLE);
                findViewById(R.id.watch).requestFocus();
                findViewById(R.id.buttons).startAnimation(AnimationUtils.loadAnimation(WebSeriesDescriptionActivity.this,R.anim.fade_in));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void printDate()
    {
        File temp_history = new File(getFilesDir(),"TempHistory.txt");
        try {
            File file = new File(getFilesDir(),"Date.txt");
            if(!file.exists())
            {
                file.createNewFile();
                try {
                    FileOutputStream fileOutputStream = openFileOutput("Date.txt",MODE_PRIVATE);
                    fileOutputStream.write("00000000".getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileInputStream fis = null;
            fis = openFileInput("Date.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str=br.readLine())!=null)
            {
                sb.append(str);
            }
            Calendar calendar = Calendar.getInstance();
            if(!sb.toString().substring(0,4).equals(String.valueOf(calendar.get(Calendar.YEAR))))
            {
                try {
                    FileOutputStream fileOutputStream = openFileOutput("History.txt",MODE_PRIVATE);
                    fileOutputStream.write(("History of "+String.valueOf(calendar.get(Calendar.YEAR))).getBytes());
                    fileOutputStream.close();
                    if(!sb.toString().substring(0,4).equals("0000"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(!sb.toString().equals(calendar.get(Calendar.YEAR)+""+(calendar.get(Calendar.MONTH)+1)+""+calendar.get(Calendar.DAY_OF_MONTH)))
            {
                try {
                    FileOutputStream fos = openFileOutput("Date.txt", MODE_PRIVATE);
                    fos.write((calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.DAY_OF_MONTH)).getBytes());
                    fos.close();
                    File file_history = new File(getFilesDir(),"History.txt");
                    fos = openFileOutput("History.txt",MODE_APPEND);
                    fos.write(("\n"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+(1+calendar.get(Calendar.MONTH))+"\n"+"\n").getBytes());
                    fos = openFileOutput("TempHistory.txt",MODE_APPEND);
                    fos.write(("\n"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+(1+calendar.get(Calendar.MONTH))+"\n"+"\n").getBytes());
                    try {
                        FileOutputStream fileOutputStream = openFileOutput("Backup.txt",MODE_PRIVATE);
                        fileOutputStream.write("true".getBytes());
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void printHistory(String str)
    {
        try {
            printDate();
            FileOutputStream fos = openFileOutput("History.txt", MODE_APPEND);
            Calendar calendar = Calendar.getInstance();
            if(calendar.get(Calendar.HOUR_OF_DAY)<10) fos.write("0".getBytes());
            fos.write((calendar.get(Calendar.HOUR_OF_DAY)+":").getBytes());
            if(calendar.get(Calendar.MINUTE)<10) fos.write("0".getBytes());
            fos.write((calendar.get(Calendar.MINUTE)+"\t\t"+str+"\n").getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        },2000);
    }
}