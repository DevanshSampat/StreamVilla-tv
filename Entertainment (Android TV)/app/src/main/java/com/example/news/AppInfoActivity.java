package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AppInfoActivity extends AppCompatActivity {

    private boolean dark = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        if(getIntent().hasExtra("dark")) setDarkTheme();
        showVersion();
    }
    private void setDarkTheme()
    {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.info_activity);
        relativeLayout.setBackgroundColor(Color.parseColor("#000000"));
        TextView textView = (TextView) findViewById(R.id.header);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.name);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.share_button);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.app_version);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.developer_details);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.contact);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.changelog);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        CardView cardView = (CardView) findViewById(R.id.log_card);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.app_details);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        dark=true;
    }
    public void showVersion()
    {
        final TextView tv = (TextView) findViewById(R.id.app_version);
        tv.setText("Version "+BuildConfig.VERSION_NAME);
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TextView textView = (TextView) findViewById(R.id.changelog);
                textView.setText("About this app:-\n\n"+snapshot.child("about").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void sendMail(View v)
    {
        Intent i = new Intent(this,SendFeedbackActivity.class);
        if(dark) i.putExtra("dark",true);
        startActivity(i);
        overridePendingTransition(R.anim.zoom_in_bottom,R.anim.fade_out);
    }

    public void shareApp(View v)
    {
        Intent i = new Intent(this,ShareActivity.class);
        if(dark) i.putExtra("dark",true);
        startActivity(i);
        overridePendingTransition(R.anim.zoom_in,R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in,R.anim.zoom_out_bottom);
    }
}