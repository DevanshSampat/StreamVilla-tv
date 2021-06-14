package com.example.news;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        if(getIntent().hasExtra("dark")) setDarkTheme();
        findViewById(R.id.share_apk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareApk();
            }
        });
        findViewById(R.id.share_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareLink();
            }
        });
        getInfo();
    }

    private void setDarkTheme()
    {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.share);
        relativeLayout.setBackgroundColor(Color.parseColor("#000000"));
        TextView textView = (TextView) findViewById(R.id.name);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.version_info);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.share_via);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
    }

    public void getInfo()
    {
        TextView version = (TextView) findViewById(R.id.version_info);
        version.setText("Version "+BuildConfig.VERSION_NAME);
    }

    private void shareLink()
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,"Hey, I just used Entertainment app for Android. It provides amazing movies and comedy shows, sports, latest news content to read as well as watch online. Moreover, its useful for old people who watch classical movies/drama as well as for kids who watch cartoons.\nGet it now on https://drive.google.com/drive/folders/105N8dwqkgU5k7c-5Zot9wPn6peLICURV");
        startActivity(Intent.createChooser(intent,"Share Link Using"));
    }

    private void shareApk()
    {
        ApplicationInfo app = getApplicationContext().getApplicationInfo();
        String filePath = app.sourceDir;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.android.package-archive");
        File originalAPK = new File(filePath);
        File tempFile = new File(getApplicationContext().getExternalFilesDir(null),
                "Entertainment_" + BuildConfig.VERSION_NAME.replace(' ', '_') + ".apk");
        if (!tempFile.exists()) {
            try {
                tempFile.createNewFile();
                InputStream in = new FileInputStream(originalAPK);
                OutputStream out = new FileOutputStream(tempFile);
                byte buf[] = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Uri uriOfAPK = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", tempFile);
        intent.putExtra(Intent.EXTRA_STREAM, uriOfAPK);
        startActivity(Intent.createChooser(intent, "Share APK using"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in,R.anim.zoom_out);
    }
}