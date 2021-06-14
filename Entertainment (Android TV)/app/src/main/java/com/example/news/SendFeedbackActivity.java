package com.example.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SendFeedbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_feedback);
        if(Build.VERSION.SDK_INT<30&&!checkIfInstalled("com.google.android.gm"))
        {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gm&hl=en_US&gl=US"));
            startActivity(i);
            finish();
        }
        if(getIntent().hasExtra("dark")) setDarkTheme();
    }
    private void setDarkTheme()
    {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.feedback_layout);
        relativeLayout.setBackgroundColor(Color.parseColor("#000000"));
        CardView cardView = (CardView) findViewById(R.id.feed_card);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        EditText editText = (EditText) findViewById(R.id.feed_text);
        editText.setTextColor(Color.WHITE);
    }
    public boolean checkIfInstalled(String str)
    {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(str,PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch(Exception e){
            return false;
        }
    }
    private void sendMail()
    {
        Intent i;
        EditText editText = (EditText) findViewById(R.id.feed_text);
        if(editText.getText().toString().trim().length()>0)
        {
            i = new Intent(Intent.ACTION_SENDTO);
            i.setData(Uri.parse("mailto:team.entertainment108@gmail.com?subject="+Uri.encode("Entertainment - v"+BuildConfig.VERSION_NAME)+"&body="+Uri.encode("Device : "+Build.BRAND+" "+Build.MODEL+"("+Build.DEVICE+")"+"\nAndroid version : "+Build.VERSION.RELEASE+"\nAPI level : "+Build.VERSION.SDK_INT+"\n\nFeedback : "+editText.getText().toString())));
            try {
                startActivity(i);
                finish();
            }
            catch (Exception e)
            {
                Toast.makeText(this,"Install mail app first",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.gm"));
                startActivity(intent);
            }
        }
        else Toast.makeText(this,"Feedback cannot be empty",Toast.LENGTH_SHORT).show();
        overridePendingTransition(R.anim.up_start,R.anim.up_end);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in,R.anim.zoom_out_bottom);
    }

    public void submitFeedback(View v)
    {
        sendMail();
    }
}