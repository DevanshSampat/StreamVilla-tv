package com.example.news;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        if(getIntent().hasExtra("dark")) setDarkTheme();
        getHistory();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }, 1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView textView = (TextView) findViewById(R.id.description);
                if(getIntent().hasExtra("signed_in")) textView.setText("Synced with your Google account");
                else textView.setText("Loaded from local storage");
            }
        },2000);
    }
    private void setDarkTheme()
    {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
        scrollView.setBackgroundColor(Color.parseColor("#000000"));
        TextView textView = (TextView) findViewById(R.id.history);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
    }
    private void getHistory()
    {
        String historyData = null;
        try {
            FileInputStream fis = null;
            fis = this.openFileInput("History.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str=br.readLine())!=null)
            {
                sb.append(str).append("\n");
            }
            historyData = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView textView = (TextView) findViewById(R.id.history);
        textView.setText(historyData);
    }
}