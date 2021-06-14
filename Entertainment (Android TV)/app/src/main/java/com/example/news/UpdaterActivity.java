package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;

public class UpdaterActivity extends AppCompatActivity {

    Bundle bundle;
    String app_update;
    String changelog;
    private String update_link;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_updater);
        bundle = getIntent().getExtras();
        app_update = bundle.getString("version");
        changelog = bundle.getString("changelog");
        update_link = bundle.getString("update_link");
        if (getIntent().hasExtra("dark")) setDarkTheme();
        getDetails();
        findViewById(R.id.now).requestFocus();
    }
    private void setDarkTheme()
    {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.update_activity);
        layout.setBackgroundColor(Color.parseColor("#000000"));
        TextView textView = (TextView) findViewById(R.id.changelog_text);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.text_progress);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        CardView cardView = (CardView) findViewById(R.id.changelog_card);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
    }
    public void getDetails()
    {
        TextView tv;
        tv = (TextView) findViewById(R.id.version);
        tv.setText("Version "+app_update);
        tv = (TextView) findViewById(R.id.changelog_text);
        tv.setText(changelog);
        findViewById(R.id.now).requestFocus();
    }
    public void downloadNow(View v)
    {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                Toast.makeText(getApplicationContext(), "Allow Entertainment to install apk", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:com.example.news")));
                    }
                }, 1000);
                return;
            }
        }
        if (new File(getApplicationContext().getExternalFilesDir(null), "Entertainment_" +
                app_update.replace(' ', '_') + ".apk").exists())
        {
            installUpdate();
            return;
        }
        findViewById(R.id.load_download).setVisibility(View.VISIBLE);
        findViewById(R.id.progress_card).setVisibility(View.VISIBLE);
        findViewById(R.id.now).setVisibility(View.INVISIBLE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(update_link));
        request.setTitle("Entertainment");
        request.setDescription("Version " + app_update);
        request.allowScanningByMediaScanner();
        request.setDestinationInExternalFilesDir(getApplicationContext(), "",
                "Entertainment_" + app_update.replace(' ', '_') +
                        ".apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
        request.setVisibleInDownloadsUi(false);
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = Objects.requireNonNull(manager).enqueue(request);
        findViewById(R.id.text_progress).setVisibility(View.VISIBLE);
        TextView textView = findViewById(R.id.text_progress);
        textView.setText("Connecting...");
        textView.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;
                while (downloading) {
                    final ProgressBar progressBar = findViewById(R.id.load_download);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    try {
                        Cursor cursor = manager.query(query);
                        cursor.moveToFirst();
                        final int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        final int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        final int progress = (100 * bytes_downloaded) / bytes_total;
                        final TextView textView = findViewById(R.id.text_progress);
                        final CardView cardView = findViewById(R.id.now);
                        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                            downloading = false;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progress);
                                if (bytes_total >= bytes_downloaded) {
                                    String current = "", total = "";
                                    current = String.valueOf((double) bytes_downloaded / (1024 * 1024));
                                    total = String.valueOf((double) bytes_total / (1024 * 1024));
                                    try {
                                        current = current.substring(0, current.indexOf('.') + 3);
                                        total = total.substring(0, total.indexOf('.') + 3);
                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                        textView.setText("Getting update info...");
                                        return;
                                    }
                                    textView.setText(current + " MB / " + total + " MB");
                                    if (current.equals(total)) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                installUpdate();
                                            }
                                        }, 540);
                                    }
                                }
                            }
                        });
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private void installUpdate()
    {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri updateFileUri = FileProvider.getUriForFile(getApplicationContext(),BuildConfig.APPLICATION_ID+".provider",
                new File(getApplicationContext().getExternalFilesDir(null),"Entertainment_"+
                        app_update.replace(' ','_')+".apk"));
        install.setDataAndType(updateFileUri,"application/vnd.android.package-archive");
        startActivity(install);
        findViewById(R.id.load_download).setVisibility(View.GONE);
        findViewById(R.id.text_progress).setVisibility(View.GONE);
        findViewById(R.id.progress_card).setVisibility(View.GONE);
        findViewById(R.id.now).setVisibility(View.VISIBLE);
    }
    public void openUpdateLink(View v)
    {
        Intent i = new Intent(this,WebActivity.class);
        if(getIntent().hasExtra("beta")) i.putExtra("link","https://drive.google.com/drive/folders/18fIJ7LPVJR5ql9cm32VA0AQvQp6elUop");
        else i.putExtra("link","https://drive.google.com/drive/folders/105N8dwqkgU5k7c-5Zot9wPn6peLICURV");
        startActivity(i);
        overridePendingTransition(R.anim.zoom_in_bottom,R.anim.fade_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        File file = new File(getApplicationContext().getExternalFilesDir(null),"Entertainment_"+app_update.replace(' ','_')+".apk");
        Uri uri = FileProvider.getUriForFile(getApplicationContext(),BuildConfig.APPLICATION_ID+".provider",file);
        getContentResolver().delete(uri,null,null);
        overridePendingTransition(R.anim.right_start,R.anim.right_end);
    }

}