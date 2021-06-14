package com.example.news;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class WebActivity extends AppCompatActivity {

    private String link;
    private BroadcastReceiver internetReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        Bundle bundle = getIntent().getExtras();
        link=bundle.getString("link");
        WebView webView = new WebView(this);
        internetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(checkInternetConnection())
                {
                    findViewById(R.id.refresh).callOnClick();
                    unregisterReceiver(this);
                }
            }
        };
        checkAndroidVersion();
        if(getIntent().hasExtra("dark"))
        {
            findViewById(R.id.warn_layout).setBackgroundColor(Color.parseColor("#000000"));
            TextView textView = findViewById(R.id.warning);
            textView.setTextColor(Color.parseColor("#CCCCCC"));
        }
        safeOpen();
    }
    private void checkAndroidVersion()
    {
        Button btn = (Button) findViewById(R.id.wifi);
        if(Build.VERSION.SDK_INT>=29)
        {
            btn.setText("turn on Wifi/Mobile Data");
        }
    }
    private  void safeOpen()
    {
        if(checkInternetConnection()) {
            if(link.equals("null"))
            {
                startActivity(new Intent(getApplicationContext(),SplashActivity.class));
                finish();
                return;
            }
            openLink();
        }
        else
        {
            Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show();
        }
    }
    private void openLink()
    {
        if(getIntent().hasExtra("video")) {
            Toast.makeText(WebActivity.this, "Double tap the recents key to return to Entertainment app", Toast.LENGTH_LONG).show();
        }
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.setData(Uri.parse(link));
        startActivity(i);
        finish();
    }

    public void refresh(View v)
    {
        safeOpen();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean checkInternetConnection()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetwork()==null)
        {
            registerReceiver(internetReceiver,new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            return false;
        }
        return true;
    }
    public void turnOnWifi(View v) {
        if (Build.VERSION.SDK_INT< 29) {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wm.setWifiEnabled(true);
            Toast.makeText(this, "Turned on Wifi", Toast.LENGTH_SHORT).show();
        } else {
            Intent panelIntent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
            startActivity(panelIntent);
        }
    }

    public void showDownloadedVideos(View view) {
        Intent intent = new Intent(getApplicationContext(),ContentDisplayActivity.class);
        intent.putExtra("offline",true);
        intent.putExtra("download",true);
        intent.putExtra("content_type","download");
        if(getIntent().hasExtra("dark")) intent.putExtra("dark",true);
        startActivity(intent);
    }
}