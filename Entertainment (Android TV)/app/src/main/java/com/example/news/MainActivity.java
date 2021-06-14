package com.example.news;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.IconCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.textclassifier.ConversationActions;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingRegistrar;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessageCreator;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int PICKFILE_RESULT_CODE = 999;
    private int trend_hour;
    private ContentData[] trendData,recentlyAddedContent;
    private boolean app_opened=true;
    private boolean first=true;
    private RecyclerView recyclerView;
    private RecyclerView trendView;
    private RecyclerView recentlyAdded;
    private boolean show = false;
    private boolean clear = false;
    private String online_history;
    private String local_history;
    private String beta_name;
    private String beta_log;
    private String beta_link;
    private String userType;
    private String quick_picks;
    private FirebaseFirestore firestore;
    private boolean dark = false;
    private FirebaseUser user;
    private String name="";
    private DatabaseReference reference;
    private int version = 0;
    private String version_name;
    private String changelog;
    private String update_link;
    private Button btn;
    private ImageView imageView;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private TextView tv;
    private int RC_SIGN_IN =0;
    private FirebaseAuth auth;
    private ContentData recentData[];
    private GoogleSignInClient mGoogleSignInClient;
    private String[] trend_name,latestContent;
    private String notification_token;
    private StringBuilder watch_time;
    private String downloadedMovies;
    private String restoreMovies;
    private String movie_watch_session_times="";
    private Notification notification;
    private boolean personal = true;
    private boolean restarted=false;
    private boolean clicked = false;
    private String last_sign_in="0000/00/00 at 00:00";
    private String next_notification;
    private Object watchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        printDate();
        Switch toggle = (Switch) findViewById(R.id.dark_toggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if(checked) setDarkTheme();
                else setLightTheme();
            }
        });
        downloadedMovies=getDownloadedMovies();
        findViewById(R.id.search).setVisibility(View.INVISIBLE);
        watch_time=new StringBuilder();
        deleteExistingApk();
        setAppropriateTheme();
        findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setVisibility(View.GONE);
        findViewById(R.id.quick).setVisibility(View.GONE);
        trendView = (RecyclerView) findViewById(R.id.recycler_trend);
        trendView.setHasFixedSize(true);
        trendView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        trendView.setVisibility(View.GONE);
        findViewById(R.id.trend).setVisibility(View.GONE);
        recentlyAdded = (RecyclerView) findViewById(R.id.recycler_recently_added);
        recentlyAdded.setHasFixedSize(true);
        recentlyAdded.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recentlyAdded.setVisibility(View.GONE);
        findViewById(R.id.recently_added).setVisibility(View.GONE);
        userType = new String();
        userType = "";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null)
        {
            Button btn = (Button) findViewById(R.id.sign_out_button);
            signIn();
            btn.setVisibility(View.VISIBLE);
            getInformation();
        }
        else
        {
            app_opened=false;
            if(getIntent().hasExtra("search")) signIn();
            else checkForUpdates();
        }
        FirebaseAnalytics.getInstance(this);
        findViewById(R.id.show_quick_and_trending).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
                clicked = true;
                checkUserType();
            }
        });
        findViewById(R.id.profile_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(user==null) signIn();
                 else if(findViewById(R.id.settings).getVisibility()==View.VISIBLE)
                 {
                     showSignOutDialog();
                 }
            }
        });
        if(!checkInternetConnection())
        {
            Intent i = new Intent(this,WebActivity.class);
            i.putExtra("link","null");
            startActivity(i);
        }
        loadImages();
        findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ShareActivity.class);
                if(dark) intent.putExtra("dark",true);
                startActivity(intent);
                overridePendingTransition(R.anim.zoom_in,R.anim.fade_out);
            }
        });
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel("general",
                    "General", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Latest content and news");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            channel = new NotificationChannel("app_update",
                    "App Update", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notify about app update, whenever available");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            channel = new NotificationChannel("personal",
                    "Personalized Notifications", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Based on your activity");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        FirebaseMessaging.getInstance().subscribeToTopic("general");
        FirebaseMessaging.getInstance().subscribeToTopic("personal");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new FCM registration token
                        notification_token=task.getResult();
                    }
                });
    }

    private void showSignOutDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.sign_out_dialog);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams layoutParams = dialog.getWindow().getAttributes();
        layoutParams.y = 100;
        layoutParams.verticalMargin=10;
        layoutParams.horizontalMargin=10;
        dialog.getWindow().setAttributes(layoutParams);
        dialog.setCancelable(true);
        if(dark)
        {
            ((CardView)dialog.findViewById(R.id.card)).setCardBackgroundColor(Color.parseColor("#363636"));
            ((TextView)dialog.findViewById(R.id.name)).setTextColor(Color.WHITE);
            ((TextView)dialog.findViewById(R.id.email)).setTextColor(Color.WHITE);
            ((TextView)dialog.findViewById(R.id.info)).setTextColor(Color.WHITE);
        }
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        ((TextView)dialog.findViewById(R.id.name)).setText(account.getDisplayName());
        ((TextView)dialog.findViewById(R.id.email)).setText(account.getEmail());
        FirebaseFirestore.getInstance().collection("Users").document(account.getEmail()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String temp = documentSnapshot.getString("Last Sign in");
                String dateAndTime;
                String[] month = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
                dateAndTime = temp.substring(8,10);
                temp = temp.substring(temp.indexOf('/')+1);
                dateAndTime = dateAndTime +" "+month[Integer.parseInt(temp.substring(0,temp.indexOf('/')))-1]+" "+
                        documentSnapshot.getString("Last Sign in").substring(0,4);
                String str = "Signed in on " + dateAndTime + documentSnapshot.getString("Last Sign in").substring(10)+
                        " on "+documentSnapshot.getString("Last device used");
                ((TextView)dialog.findViewById(R.id.info)).setText(str);
            }
        });
        dialog.findViewById(R.id.sign_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut(findViewById(R.id.sign_out_button));
            }
        });
        dialog.findViewById(R.id.all_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if(!show) showSettings(new View(getApplicationContext()));
            }
        });
        if(account.getPhotoUrl()!=null)
            Picasso.with(this).load(account.getPhotoUrl()).into((ImageView)dialog.findViewById(R.id.profile_image));
        dialog.show();
        dialog.getWindow().setDimAmount(0.90f);
        dialog.findViewById(R.id.profile_image).startAnimation(AnimationUtils.loadAnimation(this,R.anim.rotate_come));
        dialog.findViewById(R.id.layout).startAnimation(AnimationUtils.loadAnimation(this,R.anim.appear_up));

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
            Switch toggle = (Switch) findViewById(R.id.dark_toggle);
            toggle.setChecked(sb.toString().equals("true"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setDarkTheme()
    {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
        scrollView.setBackgroundColor(Color.parseColor("#000000"));
        CardView cardView = (CardView) findViewById(R.id.profile_info);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        TextView textView = (TextView) findViewById(R.id.profile_name);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        textView = (TextView) findViewById(R.id.profile_email);
        textView.setTextColor(Color.parseColor("#CCCCCC"));
        cardView = (CardView) findViewById(R.id.news);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.comedy);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.sports);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.movies);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.classics);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.kids);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.search);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.share);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        cardView = (CardView) findViewById(R.id.settings);
        cardView.setCardBackgroundColor(Color.parseColor("#363636"));
        textView = (TextView) findViewById(R.id.news_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.comedy_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.sports_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.movies_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.classics_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.kids_name);
        textView.setTextColor(Color.WHITE);
        textView = (TextView) findViewById(R.id.contact);
        textView.setTextColor(Color.WHITE);
        Button button = (Button) findViewById(R.id.update);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.add_content);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.history);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.beta);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.my_downloads);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.watchlist);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.play_local_video);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.app_info);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        button = (Button) findViewById(R.id.broadcast);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#363636")));
        button.setTextColor(Color.WHITE);
        dark = true;
        if(user!=null&&!app_opened)
        {
            trendView.setAdapter(new SmallContentAdapter(trendData,this,dark));
            recyclerView.setAdapter(new SmallContentAdapter(recentData,this,dark));
            recentlyAdded.setAdapter(new SmallContentAdapter(recentlyAddedContent,this,dark));
        }
        try {

            File file = new File(this.getFilesDir(), "Theme.txt");
            FileOutputStream fos = openFileOutput("Theme.txt",MODE_PRIVATE);
            fos.write(("true").getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setLightTheme()
    {
        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll);
        scrollView.setBackgroundColor(Color.WHITE);
        CardView cardView = (CardView) findViewById(R.id.profile_info);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        TextView textView = (TextView) findViewById(R.id.profile_name);
        textView.setTextColor(Color.parseColor("#333333"));
        textView = (TextView) findViewById(R.id.profile_email);
        textView.setTextColor(Color.parseColor("#333333"));
        cardView = (CardView) findViewById(R.id.news);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.comedy);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.sports);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.movies);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.classics);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.kids);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.search);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.share);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        cardView = (CardView) findViewById(R.id.settings);
        cardView.setCardBackgroundColor(Color.parseColor("#DADADA"));
        textView = (TextView) findViewById(R.id.news_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.comedy_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.sports_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.movies_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.classics_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.kids_name);
        textView.setTextColor(Color.BLACK);
        textView = (TextView) findViewById(R.id.contact);
        textView.setTextColor(Color.BLACK);
        Button button = (Button) findViewById(R.id.update);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.add_content);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.history);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.beta);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.my_downloads);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.watchlist);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.play_local_video);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.app_info);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        button = (Button) findViewById(R.id.broadcast);
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DADADA")));
        button.setTextColor(Color.BLACK);
        dark = false;
        if(user!=null&&!app_opened)
        {
            trendView.setAdapter(new SmallContentAdapter(trendData,this,dark));
            recyclerView.setAdapter(new SmallContentAdapter(recentData,this,dark));
            recentlyAdded.setAdapter(new SmallContentAdapter(recentlyAddedContent,this,dark));
        }
        try {
            FileOutputStream fos = openFileOutput("Theme.txt",MODE_PRIVATE);
            fos.write("false".getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showHistory(View v)
    {
        Intent intent = new Intent(this,HistoryActivity.class);
        if(dark) intent.putExtra("dark",true);
        if(user!=null) intent.putExtra("signed_in",true);
        startActivity(intent);
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void printDate()
    {
        File temp_history = new File(this.getFilesDir(),"TempHistory.txt");
        try {
            File file = new File(this.getFilesDir(),"Date.txt");
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
            fis = this.openFileInput("Date.txt");
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
                    if(!sb.toString().substring(0,4).equals("0000")) clear=true;
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
                    File file_history = new File(this.getFilesDir(),"History.txt");
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
            TextView textView = (TextView) findViewById(R.id.app_info);
            textView.setText("Not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void signOut(final View v)
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                TextView tv;
                tv = (TextView) findViewById(R.id.profile_name);
                tv.setText("Tap to sign-in");
                tv = (TextView) findViewById(R.id.profile_email);
                tv.setText("to enjoy latest content");
                Button btn = (Button) findViewById(R.id.sign_out_button);
                btn.setVisibility(View.GONE);
                btn = (Button) findViewById(R.id.sign_out);
                btn.setVisibility(View.GONE);
                btn = (Button) findViewById(R.id.add_content);
                btn.setVisibility(View.GONE);
                findViewById(R.id.broadcast).setVisibility(View.GONE);
                btn = (Button) findViewById(R.id.beta);
                btn.setVisibility(View.GONE);
                imageView = (ImageView) findViewById(R.id.profile_photo);
                storage = FirebaseStorage.getInstance();
                imageView.setVisibility(View.GONE);
                imageView = (ImageView) findViewById(R.id.blank_profile);
                imageView.setVisibility(View.VISIBLE);
                revokeAccess();
                user=null;
                tv = (TextView) findViewById(R.id.verify);
                tv.setVisibility(View.GONE);
                userType="";
                if(v == (Button) findViewById(R.id.sign_out))show=false;
                checkForUpdates();
                recyclerView.setVisibility(View.GONE);
                findViewById(R.id.quick).setVisibility(View.GONE);
                trendView.setVisibility(View.GONE);
                findViewById(R.id.trend).setVisibility(View.GONE);
                findViewById(R.id.recently_added).setVisibility(View.GONE);
                recentlyAdded.setVisibility(View.GONE);
                findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
                findViewById(R.id.search).setVisibility(View.INVISIBLE);
                popup("Signed out successfully");
                try {
                    FileOutputStream fos = openFileOutput("Date.txt",MODE_PRIVATE);
                    fos.write("00000000".getBytes());
                    fos.close();
                    FileInputStream fis = openFileInput("DownloadedVideos.txt");
                    BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                    String string;
                    while((string=br.readLine())!=null)
                    {
                        File file = new File(getApplicationContext().getFilesDir(),string.replace(' ','+')+".txt");
                        if(file.exists()) file.delete();
                    }
                    Intent intent = new Intent(getApplicationContext(),SplashActivity.class);
                    finish();
                    startActivity(intent);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void popup(String str)
    {
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
    }
    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }
    private void signIn()
    {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        first=true;
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else if(requestCode==PICKFILE_RESULT_CODE)
        {
            if(resultCode==RESULT_OK)
            {
                Uri uri = data.getData();
                String path=uri.getPath();
                int i=path.length()-1;
                while(path.charAt(i)!='/') i--;
                path=path.substring(i+1);
                Intent intent = new Intent(getApplicationContext(),VideoPlayerActivity.class);
                intent.putExtra("uri",String.valueOf(uri));
                intent.putExtra("name",path);
                startActivity(intent);
            }
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
            validate();
            getInformation();
            if (getIntent().hasExtra("search"))
            {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                intent.putExtra("search", getIntent().getStringExtra("search"));
                if (getIntent().hasExtra("open")) intent.putExtra("open", true);
                intent.putExtra("signed_in", true);
                if (dark) intent.putExtra("dark", true);
                startActivity(intent);
            }
            auth = FirebaseAuth.getInstance();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google", "signInResult:failed code=" + e.getStatusCode());
        }
    }
    private void updateUserDetails()
    {
        auth = FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        try {

            while (user == null) {
                user = auth.getCurrentUser();
            }
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
            user.updateProfile(profile);
        }
        catch (Exception e)
        {
            updateUserDetails();
        }
    }
    public void verify(View v)
    {
        verifyEmailAddress();
    }
    private void verifyEmailAddress()
    {
        auth = FirebaseAuth.getInstance();
        user= auth.getCurrentUser();
        try {
            while (user == null) {
                user = auth.getCurrentUser();
            }
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
            user.updateProfile(profile);
            if(user.getDisplayName()==null)
            {
                user.updateProfile(profile);
            };
        }
        catch (Exception e)
        {
            verifyEmailAddress();
        }
        if(user.getDisplayName()==null)
        {
            try {
                Thread.sleep(300);
            }
            catch (Exception e){}
            user.sendEmailVerification();
        }
        else user.sendEmailVerification();
        Toast.makeText(this, "Verification email sent to " + user.getEmail()+"\nRe-Launch the app after verification", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.gm");
                if(intent!=null)
                {
                    startActivity(intent);
                    finish();
                }
                else
                {
                    Intent web = new Intent(MainActivity.this,WebActivity.class);
                    web.putExtra("link","https://mail.google.com");
                    startActivity(web);
                    finish();
                }
            }
        }, 1000);
    }

    private void getInformation()
    {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        TextView name = (TextView) findViewById(R.id.profile_name);
        TextView email = (TextView) findViewById(R.id.profile_email);
        Uri url = account.getPhotoUrl();
        name.setText("Google Account");
        email.setText("getting information");
        Animation animation = AnimationUtils.loadAnimation(this,R.anim.fade_in);
        if(url!=null) {
            ImageView imageView = (ImageView) findViewById(R.id.blank_profile);
            imageView.setVisibility(View.GONE);
            imageView = (ImageView) findViewById(R.id.profile_photo);
            imageView.setVisibility(View.VISIBLE);
            imageView.startAnimation(animation);
            Picasso.with(this).load(String.valueOf(url)).into(imageView);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void validate()
    {
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct!=null) {
            findViewById(R.id.search).setVisibility(View.VISIBLE);
            findViewById(R.id.settings).setVisibility(View.INVISIBLE);
            UserName.setUsername(acct.getEmail());
            UserName.setUsername(getApplicationContext(),acct.getEmail());
            auth = FirebaseAuth.getInstance();
            name = acct.getDisplayName();
            Toast.makeText(this, "Signed in as " + acct.getEmail(), Toast.LENGTH_SHORT).show();
            auth.createUserWithEmailAndPassword((acct.getEmail()), "12345678").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull final Task<AuthResult> task) {
                    while(user==null)
                    {
                        user=auth.getCurrentUser();
                    }
                    GoogleSignInAccount acc2 = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(acc2.getDisplayName()).build();
                    user.updateProfile(profile);
                    if(user.getDisplayName()==null)
                    {
                        user.updateProfile(profile);
                        Toast.makeText(MainActivity.this,user.getDisplayName(),Toast.LENGTH_SHORT);
                        popup("Setting up your credentials");
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (task.isSuccessful()) {
                                verifyEmailAddress();
                            } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                updateUserDetails();
                            }
                        }
                    },1500);


                }
            });
            auth.signInWithEmailAndPassword(acct.getEmail(), "12345678");
            user=auth.getCurrentUser();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(user!=null) checkUserType();
                }
            },1000);
            if (acct.getEmail().equals("devansh.sampat@gmail.com")) {
                Button btn = (Button) findViewById(R.id.add_content);
                btn.setVisibility(View.VISIBLE);
                findViewById(R.id.broadcast).setVisibility(View.VISIBLE);
            }
            Button btn = (Button) findViewById(R.id.sign_out_button);
            btn.setVisibility(View.VISIBLE);
            btn = (Button) findViewById(R.id.beta);
            btn.setVisibility(View.VISIBLE);
            RecyclerView demoView;
            findViewById(R.id.recently_added).setVisibility(View.VISIBLE);
            findViewById(R.id.trend).setVisibility(View.VISIBLE);
            findViewById(R.id.quick).setVisibility(View.VISIBLE);
            demoView = findViewById(R.id.recycler);
            demoView.setVisibility(View.VISIBLE);
            demoView.setAdapter(new SmallContentAdapter(true,getApplicationContext(),dark));
            demoView = findViewById(R.id.recycler_trend);
            demoView.setVisibility(View.VISIBLE);
            demoView.setAdapter(new SmallContentAdapter(true,getApplicationContext(),dark));
            demoView = findViewById(R.id.recycler_recently_added);
            demoView.setVisibility(View.VISIBLE);
            demoView.setAdapter(new SmallContentAdapter(true,getApplicationContext(),dark));
        }
    }
    private void checkUserType()
    {
        firestore = FirebaseFirestore.getInstance();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        DocumentReference documentReference = firestore.collection("Users").document(account.getEmail());
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try {
                    if(last_sign_in.equals("0000/00/00 at 00:00")) last_sign_in=documentSnapshot.getString("Last Sign in");
                    Toast.makeText(MainActivity.this, documentSnapshot.getString("Type"), Toast.LENGTH_SHORT);
                    restoreMovies="";
                    restoreMovies=documentSnapshot.getString("Downloads");
                    quick_picks=documentSnapshot.getString("Quick Picks");
                    Toast.makeText(MainActivity.this,documentSnapshot.getString("Quick Picks"),Toast.LENGTH_SHORT);
                    Toast.makeText(getApplicationContext(),documentSnapshot.getString("Downloads"),Toast.LENGTH_SHORT);
                    try {
                        Toast.makeText(getApplicationContext(),documentSnapshot.getString("Watch Files"),Toast.LENGTH_SHORT);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                    try{
                        watchList = documentSnapshot.get("Watchlist");
                    } catch (Exception e) {
                        watchList = null;
                    }
                    userType = documentSnapshot.getString("Type");
                    if(movie_watch_session_times.equals(""))
                    {
                        movie_watch_session_times=documentSnapshot.get("Watch Files").toString();
                        Intent intent = new Intent(getApplicationContext(), RestoreDownloadsActivity.class);
                        intent.putExtra("restore", restoreMovies);
                        if (dark) intent.putExtra("dark", true);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                makeTimeFiles();
                            }
                        },1000);
                    }
                    else makeWatchTimingsFile();
                    Button btn = (Button) findViewById(R.id.beta);
                    if(userType.equals("beta"))
                    {
                        btn.setText("Revert to stable");
                    }
                    else btn.setText("Become a tester");
                    online_history=documentSnapshot.getString("Watch History");
                    Toast.makeText(MainActivity.this,online_history.trim(),Toast.LENGTH_SHORT);
                    try{
                        FileInputStream fis = null;
                        fis = getApplicationContext().openFileInput("History.txt");
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                        StringBuilder sb = new StringBuilder();
                        String str;
                        while ((str=br.readLine())!=null)
                        {
                            sb.append(str).append("\n");
                        }
                        local_history = sb.toString();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(!local_history.substring(0,7).equals("History"))
                                {
                                    Calendar calendar = Calendar.getInstance();
                                    local_history= "History of "+String.valueOf(calendar.get(Calendar.YEAR)) +"\n"+local_history;
                                }
                            }
                        }, 500);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                uploadUserDetails();
                            }
                        }, 1000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    if(userType==null||!userType.equals("beta"))userType = "stable";
                     online_history= new String();
                    try{
                        FileInputStream fis = null;
                        fis = getApplicationContext().openFileInput("History.txt");
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                        StringBuilder sb = new StringBuilder();
                        String str;
                        while ((str=br.readLine())!=null)
                        {
                            sb.append(str).append("\n");
                        }
                        local_history = sb.toString();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                uploadUserDetails();
                                if(restoreMovies==null) makeTimeFiles();
                            }
                        }, 1000);
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        });
        documentReference.get().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.println(Log.ASSERT,"doc","failed");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        uploadUserDetails();
                    }
                }, 1000);
             }
        });
        documentReference = firestore.collection("Data").document("Trending");
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                trend_name = new String[10];
                if(documentSnapshot.get("User").toString().equals(user.getEmail()))
                {
                    for(int i=0;i<=9;i++)
                    {
                        trend_name[i]=documentSnapshot.get(String.valueOf(i+6)).toString();
                    }
                }
                else
                {
                    for(int i=0;i<=9;i++)
                    {
                        trend_name[i]=documentSnapshot.get(String.valueOf(i+1)).toString();
                    }
                }
                trend_hour=Integer.parseInt(documentSnapshot.get("Hour").toString());
            }
        });
    }
    private void uploadUserDetails()
    {
        if(user==null) return;
        String history="";
        try {
            if(online_history.trim().length()==0) history = local_history;
            else if(online_history.trim().length()<=local_history.trim().length()) history=local_history;
            else
            {
                if(clear&&!online_history.substring(0,"History of 0000".length()).equals("History of "+Calendar.getInstance().get(Calendar.YEAR)))
                {
                    history="";
                    popup("Watch history reset");
                    clear=false;
                }
                else
                {
                    try {
                        FileOutputStream fileOutputStream = openFileOutput("History.txt", MODE_PRIVATE);
                        fileOutputStream.write((online_history).getBytes());
                        fileOutputStream.close();
                        history = online_history;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.println(Log.ASSERT,"error",e.toString());
            checkUserType();
            return;
        }
        firestore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = firestore.collection("Users").document(GoogleSignIn.getLastSignedInAccount(getApplicationContext()).getEmail());
        final Map<String,Object> userInfo = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        userInfo.put("Name",user.getDisplayName());
        userInfo.put("Type",userType);
        StringBuilder sb = new StringBuilder();
        sb.append(calendar.get(Calendar.YEAR)+"/");
        if(calendar.get(Calendar.MONTH)+1<10) sb.append("0");
        sb.append((calendar.get(Calendar.MONTH)+1)+"/");
        if(calendar.get(Calendar.DAY_OF_MONTH)<10) sb.append("0");
        sb.append(calendar.get(Calendar.DAY_OF_MONTH)+" at ");
        if(calendar.get(Calendar.HOUR_OF_DAY)<10) sb.append("0");
        sb.append(calendar.get(Calendar.HOUR_OF_DAY)+":");
        if(calendar.get(Calendar.MINUTE)<10) sb.append("0");
        sb.append(calendar.get(Calendar.MINUTE));
        if(quick_picks==null) quick_picks="no data";
        //Log.println(Log.ASSERT,"Data","Uploading");
        userInfo.put("Last Sign in",sb.toString());
        userInfo.put("Last device used",Build.BRAND+" "+Build.MODEL+"("+Build.DEVICE+")");
        userInfo.put("Android version",Build.VERSION.RELEASE);
        userInfo.put("App version",BuildConfig.VERSION_NAME);
        userInfo.put("Watch History",history);
        userInfo.put("Notification Token",notification_token);
        userInfo.put("Watch Files",movie_watch_session_times);
        userInfo.put("Downloads",downloadedMovies);
        userInfo.put("Quick Picks",quick_picks);
        userInfo.put("Watchlist",watchList);
        UserName.setQuickPicks(quick_picks);
        documentReference.set(userInfo);
        //findViewById(R.id.show_quick_and_trending).setVisibility(View.VISIBLE);
        if(first)
        {
            if(clicked)
            {
                getRecents();
                clicked=false;
            }
            if(app_opened)
            {
                checkForUpdates();
                //findViewById(R.id.show_quick_and_trending).callOnClick();
            }
            else if(findViewById(R.id.options).getVisibility()!=View.VISIBLE) findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
            app_opened=false;
        }
        else
        {
            findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
            findViewById(R.id.load_options).setVisibility(View.GONE);
        }
        if(userType.equals("beta"))
        {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("stable_update");
            FirebaseMessaging.getInstance().subscribeToTopic("beta_update");
        }
        else
        {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("beta_update");
            FirebaseMessaging.getInstance().subscribeToTopic("stable_update");
        }
    }
    public void addContent(View v)
    {
        Intent intent = new Intent(this,AddContentActivity.class);
        if(dark) intent.putExtra("dark",true);
        startActivity(intent);
    }
    public void downloadUpdates(final View v)
    {
        reference = FirebaseDatabase.getInstance().getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(userType.equals("beta")) version=Integer.parseInt(snapshot.child("tv_beta_code").getValue().toString());
                else version=Integer.parseInt(snapshot.child("tv_version_code").getValue().toString());
                version_name=snapshot.child("tv_version_name").getValue().toString();
                changelog=snapshot.child("tv_changelog").getValue().toString();
                update_link=snapshot.child("tv_update_link").getValue().toString();
                beta_log=snapshot.child("tv_beta_log").getValue().toString();
                beta_name=snapshot.child("tv_beta_name").getValue().toString();
                beta_link=snapshot.child("tv_beta_link").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(BuildConfig.VERSION_CODE<version) {
            findViewById(R.id.update_notification).setVisibility(View.VISIBLE);
            Intent i = new Intent(this, UpdaterActivity.class);
            if(dark) i.putExtra("dark",true);
            if(userType.equals("beta"))
            {
                i.putExtra("beta",true);
                i.putExtra("version",beta_name);
                i.putExtra("changelog",beta_log);
                i.putExtra("update_link",beta_link);
            }
            else
            {
                i.putExtra("version", version_name);
                i.putExtra("changelog", changelog);
                i.putExtra("update_link",update_link);
            }
            startActivity(i);
            overridePendingTransition(R.anim.right_start,R.anim.right_end);
            finish();
        }
        else
        {
            Toast.makeText(this,"Entertainment is up-to-date",Toast.LENGTH_SHORT).show();
            findViewById(R.id.update_notification).setVisibility(View.GONE);
        }
    }
    public void checkForUpdates()
    {
        reference = FirebaseDatabase.getInstance().getReference();
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(account!=null&&userType=="") checkForUpdates();
                version=Integer.parseInt(snapshot.child("tv_version_code").getValue().toString());
                if(userType.equals("beta")) version=Integer.parseInt(snapshot.child("tv_beta_code").getValue().toString());
                version_name=snapshot.child("tv_version_name").getValue().toString();
                changelog=snapshot.child("tv_changelog").getValue().toString();
                update_link=snapshot.child("tv_update_link").getValue().toString();
                beta_log=snapshot.child("tv_beta_log").getValue().toString();
                beta_name=snapshot.child("tv_beta_name").getValue().toString();
                beta_link=snapshot.child("tv_beta_link").getValue().toString();
                btn = (Button) findViewById(R.id.update);
                btn.setVisibility(View.VISIBLE);
                tv = (TextView) findViewById(R.id.sports_name);
                tv.setText(snapshot.child("sports_name").getValue().toString());
                if(BuildConfig.VERSION_CODE<version)
                {
                    btn.setTextColor(Color.parseColor("#FF8313"));
                    btn.setBackgroundColor(Color.parseColor("#0C61C8"));
                    if(userType.equals("beta")) btn.setText("Update available\n("+beta_name+")");
                    else btn.setText("Update available\n("+version_name+")");
                    downloadUpdates(btn);
                }
                else
                {
                    findViewById(R.id.update_notification).setVisibility(View.GONE);
                    btn.setText("Check For Updates");
                    setAppropriateTheme();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void loadImages()
    {
        reference = FirebaseDatabase.getInstance().getReference("images");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ImageView imageView = (ImageView) findViewById(R.id.sports_image);
                Picasso.with(MainActivity.this).load(snapshot.child("sports").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.news_image);
                Picasso.with(MainActivity.this).load(snapshot.child("news").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.comedy_image);
                Picasso.with(MainActivity.this).load(snapshot.child("comedy").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.movies_image);
                Picasso.with(MainActivity.this).load(snapshot.child("movies").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.classics_image);
                Picasso.with(MainActivity.this).load(snapshot.child("classics").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.kids_image);
                Picasso.with(MainActivity.this).load(snapshot.child("kids").getValue().toString()).into(imageView);
                imageView = (ImageView) findViewById(R.id.settings_image);
                Picasso.with(MainActivity.this).load(snapshot.child("settings").getValue().toString()).into(imageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void openActivity(View v)
    {
        if(user!=null&&!user.isEmailVerified())
        {
            Toast.makeText(this,"Verify your email first",Toast.LENGTH_SHORT).show();
            TextView textView = (TextView) findViewById(R.id.verify);
            textView.setVisibility(View.VISIBLE);
            return;
        }
        else if(user==null)
        {
            signIn();
            return;
        }
        TextView textView = (TextView) findViewById(R.id.verify);
        textView.setVisibility(View.GONE);
        Intent i = new Intent(getApplicationContext(),ContentDisplayActivity.class);
        switch (v.getId())
        {
            case R.id.news : i.putExtra("content_type","news");
                break;
            case R.id.comedy : i.putExtra("content_type","comedy");
                break;
            case R.id.sports : i.putExtra("content_type","sport");
                break;
            case R.id.movies : i.putExtra("content_type","movie");
                break;
            case R.id.classics : i.putExtra("content_type","classic");
                break;
            case R.id.kids : i.putExtra("content_type","kids");
                break;
        }
        if(dark) i.putExtra("dark",true);
        startActivity(i);
        overridePendingTransition(R.anim.up_start,R.anim.up_end);
    }
    public void showAppInfo(View v)
    {
        Intent i = new Intent(this,AppInfoActivity.class);
        if(dark) i.putExtra("dark",true);
        startActivity(i);
        overridePendingTransition(R.anim.zoom_in_bottom,R.anim.fade_out);
    }
    public boolean checkInternetConnection()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetwork()==null)
        {
            return false;
        }
        return true;
    }
    public void toggleBeta(View v)
    {
        Button btn = (Button) findViewById(R.id.beta);
        if(userType.equals("beta"))
        {
            userType = "stable";
            btn.setText("Become a tester");
            popup("Reverted to stable user");
            checkForUpdates();
        }
        else
        {
            userType = "beta";
            btn.setText("Revert to stable");
            popup("You are a beta user now\nStay signed in for beta updates");
            checkForUpdates();
        }
        FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername()).update("Type",userType);
    }

    public void searchContent(View view)
    {
        Intent intent = new Intent(this,SearchActivity.class);
        if(dark) intent.putExtra("dark",true);
        if(user!=null) intent.putExtra("signed_in",true);
        startActivity(intent,
                ActivityOptionsCompat.makeSceneTransitionAnimation(this,view,view.getTransitionName()).toBundle());
        overridePendingTransition(R.anim.up_start,R.anim.up_end);
    }

    public void showSettings(View view)
    {
        Animation go = AnimationUtils.loadAnimation(this,R.anim.right_end);
        Animation come = AnimationUtils.loadAnimation(this,R.anim.right_start);
        if(findViewById(R.id.sign_out).getVisibility()==View.VISIBLE)
        {
            findViewById(R.id.sign_out).startAnimation(go);
            findViewById(R.id.sign_out).setVisibility(View.GONE);
            show=false;
        }
        if(!show)
        {
            GridLayout gridLayout = (GridLayout) findViewById(R.id.options);
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.toggles);
            gridLayout.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);
            relativeLayout.startAnimation(come);
            findViewById(R.id.quick).setVisibility(View.GONE);
            findViewById(R.id.recycler).setVisibility(View.GONE);
            findViewById(R.id.trend).setVisibility(View.GONE);
            findViewById(R.id.popular_categories).setVisibility(View.GONE);
            findViewById(R.id.recycler_trend).setVisibility(View.GONE);
            findViewById(R.id.recently_added).setVisibility(View.GONE);
            findViewById(R.id.recycler_recently_added).setVisibility(View.GONE);
            findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
            findViewById(R.id.contact).setVisibility(View.GONE);
            show=true;
        }
        else
        {
            GridLayout gridLayout = (GridLayout) findViewById(R.id.options);
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.toggles);
            findViewById(R.id.popular_categories).setVisibility(View.VISIBLE);
            gridLayout.setVisibility(View.VISIBLE);
            gridLayout.startAnimation(come);
            relativeLayout.setVisibility(View.GONE);
            if(user!=null&&recentData!=null) {
                findViewById(R.id.quick).setVisibility(View.VISIBLE);
                findViewById(R.id.recycler).setVisibility(View.VISIBLE);
                findViewById(R.id.recycler).startAnimation(come);
                findViewById(R.id.quick).startAnimation(come);
                findViewById(R.id.trend).setVisibility(View.VISIBLE);
                findViewById(R.id.recycler_trend).setVisibility(View.VISIBLE);
                findViewById(R.id.recycler_trend).startAnimation(come);
                findViewById(R.id.trend).startAnimation(come);
                findViewById(R.id.recently_added).setVisibility(View.VISIBLE);
                findViewById(R.id.recycler_recently_added).setVisibility(View.VISIBLE);
                findViewById(R.id.recycler_recently_added).startAnimation(come);
                findViewById(R.id.recently_added).startAnimation(come);
                findViewById(R.id.contact).setVisibility(View.VISIBLE);
            }
            else if(user!=null)
            {
                findViewById(R.id.show_quick_and_trending).setVisibility(View.VISIBLE);
                findViewById(R.id.show_quick_and_trending).startAnimation(come);
            }
            show=false;
        }
    }
    private void getRecents()
    {
        findViewById(R.id.show_quick_and_trending).setVisibility(View.GONE);
        reference = FirebaseDatabase.getInstance().getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i;
                int count=0;
                String[] name,link,tag,date,image,database_name;
                latestContent = new String[5];
                String downloadedTempData = "";
                FileOutputStream fileOutputStream = null;
                File file = new File(getApplicationContext().getFilesDir(),"DownloadedVideos.txt");
                if(!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    fileOutputStream = openFileOutput("DownloadedVideos.txt",MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                for(i=0;i<=4;i++)
                {
                    latestContent[i]=snapshot.child("latest"+(i+1)).getValue().toString();
                }
                if (!new File(getApplicationContext().getExternalFilesDir(null), ".icon.jpg").exists()) {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(snapshot.child("app_icon").getValue().toString()));
                    request.setTitle("Refreshing Thumbnail");
                    request.setDescription("Entertainment");
                    request.allowScanningByMediaScanner();
                    request.setDestinationInExternalFilesDir(getApplicationContext(), "",
                            ".icon.jpg");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                    request.setVisibleInDownloadsUi(false);
                    DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    Objects.requireNonNull(manager).enqueue(request);

                }
                if(user!=null) {
                    link = new String[Integer.parseInt(snapshot.child("comedy").getValue().toString())
                            + Integer.parseInt(snapshot.child("sport").getValue().toString())
                            + Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())
                            + Integer.parseInt(snapshot.child("kids").getValue().toString())
                            + Integer.parseInt(snapshot.child("news").getValue().toString())];
                    name = new String[Integer.parseInt(snapshot.child("comedy").getValue().toString())
                            + Integer.parseInt(snapshot.child("sport").getValue().toString())
                            + Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())
                            + Integer.parseInt(snapshot.child("kids").getValue().toString())
                            + Integer.parseInt(snapshot.child("news").getValue().toString())];
                    image = new String[Integer.parseInt(snapshot.child("comedy").getValue().toString())
                            + Integer.parseInt(snapshot.child("sport").getValue().toString())
                            + Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())
                            + Integer.parseInt(snapshot.child("kids").getValue().toString())
                            + Integer.parseInt(snapshot.child("news").getValue().toString())];
                    date = new String[Integer.parseInt(snapshot.child("comedy").getValue().toString())
                            + Integer.parseInt(snapshot.child("sport").getValue().toString())
                            + Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())
                            + Integer.parseInt(snapshot.child("kids").getValue().toString())
                            + Integer.parseInt(snapshot.child("news").getValue().toString())];
                    tag = new String[Integer.parseInt(snapshot.child("comedy").getValue().toString())
                            + Integer.parseInt(snapshot.child("sport").getValue().toString())
                            + Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())
                            + Integer.parseInt(snapshot.child("kids").getValue().toString())
                            + Integer.parseInt(snapshot.child("news").getValue().toString())];
                    database_name = new String[Integer.parseInt(snapshot.child("comedy").getValue().toString())
                            + Integer.parseInt(snapshot.child("sport").getValue().toString())
                            + Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())
                            + Integer.parseInt(snapshot.child("kids").getValue().toString())
                            + Integer.parseInt(snapshot.child("news").getValue().toString())];
                    ContentData movieData[] = new ContentData[Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())];
                    int track = 0;
                    for (i = 1; i <= Integer.parseInt(snapshot.child("movie").getValue().toString()); i++) {
                        name[count] = snapshot.child("movie" + i).getValue().toString();
                        link[count] = snapshot.child("movielink" + i).getValue().toString();
                        date[count] = snapshot.child("moviedate" + i).getValue().toString();
                        image[count] = snapshot.child("movieimage" + i).getValue().toString();
                        database_name[count] = "movie" + i;
                        movieData[track] = new ContentData(name[count], image[count], link[count], date[count]);
                        track++;
                        try {
                            tag[count] = snapshot.child("movietag" + i).getValue().toString();
                        } catch (Exception e) {
                            tag[count] = snapshot.child("movie" + i).getValue().toString();
                        }
                        downloadedTempData = downloadedTempData+name[count]+"\n";
                        if (new File(getApplicationContext().getExternalFilesDir(null),
                                name[count].replace(' ', '_').replace(':', '_') + ".mp4").exists()) {
                            if (!new File(getApplicationContext().getExternalFilesDir(null),
                                    name[count].replace(' ', '_').replace(':', '_') + ".jpg").exists()) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(image[count]));
                                request.setTitle("Thumbnail for Videos");
                                request.setDescription("Entertainment");
                                request.allowScanningByMediaScanner();
                                request.setDestinationInExternalFilesDir(getApplicationContext(), "",
                                        name[count].replace(' ', '_').replace(':', '_') + ".jpg");
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                                request.setVisibleInDownloadsUi(false);
                                final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                Objects.requireNonNull(manager).enqueue(request);
                            }
                        } else if (new File(getApplicationContext().getExternalFilesDir(null),
                                name[count].replace(' ', '_').replace(':', '_') + ".jpg").exists()) {
                            Uri updateFileUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider",
                                    new File(getApplicationContext().getExternalFilesDir(null),
                                            name[count].replace(' ', '_').replace(':', '_') + ".jpg"));
                            getContentResolver().delete(updateFileUri, null, null);
                        }
                        count++;
                    }
                    for (i = 1; i <= Integer.parseInt(snapshot.child("comedy").getValue().toString()); i++) {
                        name[count] = snapshot.child("comedy" + i).getValue().toString();
                        link[count] = snapshot.child("comedylink" + i).getValue().toString();
                        date[count] = snapshot.child("comedydate" + i).getValue().toString();
                        image[count] = snapshot.child("comedyimage" + i).getValue().toString();
                        database_name[count] = "comedy" + i;
                        try {
                            tag[count] = snapshot.child("comedytag" + i).getValue().toString();
                        } catch (Exception e) {
                            tag[count] = snapshot.child("comedy" + i).getValue().toString();
                        }
                        downloadedTempData = downloadedTempData+name[count]+"\n";
                        count++;
                    }
                    for (i = 1; i <= Integer.parseInt(snapshot.child("sport").getValue().toString()); i++) {
                        name[count] = snapshot.child("sport" + i).getValue().toString();
                        link[count] = snapshot.child("sportlink" + i).getValue().toString();
                        database_name[count] = "sport" + i;
                        try {
                            tag[count] = snapshot.child("sporttag" + i).getValue().toString();
                        } catch (Exception e) {
                            tag[count] = snapshot.child("sport" + i).getValue().toString();
                        }
                        image[count] = snapshot.child("sports_image_link").getValue().toString();
                        count++;
                    }
                    for (i = 1; i <= Integer.parseInt(snapshot.child("kids").getValue().toString()); i++) {
                        name[count] = snapshot.child("kids" + i).getValue().toString();
                        link[count] = snapshot.child("kidslink" + i).getValue().toString();
                        image[count] = snapshot.child("kidsimage" + i).getValue().toString();
                        database_name[count] = "kids" + i;
                        try {
                            tag[count] = snapshot.child("kidstag" + i).getValue().toString();
                        } catch (Exception e) {
                            tag[count] = snapshot.child("kids" + i).getValue().toString();
                        }
                        count++;
                    }
                    for (i = 1; i <= Integer.parseInt(snapshot.child("classic").getValue().toString()); i++) {
                        name[count] = snapshot.child("classic" + i).getValue().toString();
                        link[count] = snapshot.child("classiclink" + i).getValue().toString();
                        date[count] = snapshot.child("classicdate" + i).getValue().toString();
                        image[count] = snapshot.child("classicimage" + i).getValue().toString();
                        database_name[count] = "classic" + i;
                        movieData[track] = new ContentData(name[count], image[count], link[count], date[count]);
                        track++;
                        try {
                            tag[count] = snapshot.child("classictag" + i).getValue().toString();
                        } catch (Exception e) {
                            tag[count] = snapshot.child("classic" + i).getValue().toString();
                        }
                        downloadedTempData = downloadedTempData + name[count] + "\n";
                        if (new File(getApplicationContext().getExternalFilesDir(null),
                                name[count].replace(' ', '_').replace(':', '_') + ".mp4").exists()) {
                            if (!new File(getApplicationContext().getExternalFilesDir(null),
                                    name[count].replace(' ', '_').replace(':', '_') + ".jpg").exists()) {
                                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(image[count]));
                                request.setTitle("Thumbnail for Videos");
                                request.setDescription("Entertainment");
                                request.allowScanningByMediaScanner();
                                request.setDestinationInExternalFilesDir(getApplicationContext(), "",
                                        name[count].replace(' ', '_').replace(':', '_') + ".jpg");
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                                request.setVisibleInDownloadsUi(false);
                                final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                Objects.requireNonNull(manager).enqueue(request);
                            }
                        } else if (new File(getApplicationContext().getExternalFilesDir(null),
                                name[count].replace(' ', '_').replace(':', '_') + ".jpg").exists()) {
                            Uri updateFileUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider",
                                    new File(getApplicationContext().getExternalFilesDir(null),
                                            name[count].replace(' ', '_').replace(':', '_') + ".jpg"));
                            getContentResolver().delete(updateFileUri, null, null);
                        }
                        count++;
                    }
                    for (i = 1; i <= Integer.parseInt(snapshot.child("news").getValue().toString()); i++) {
                        name[count] = snapshot.child("news" + i).getValue().toString();
                        link[count] = snapshot.child("newslink" + i).getValue().toString();
                        date[count] = snapshot.child("newsdate" + i).getValue().toString();
                        image[count] = snapshot.child("newsimage" + i).getValue().toString();
                        database_name[count] = "news" + i;
                        try {
                            tag[count] = snapshot.child("newstag" + i).getValue().toString();
                        } catch (Exception e) {
                            tag[count] = snapshot.child("news" + i).getValue().toString();
                        }
                        count++;
                    }
                    try {
                        fileOutputStream.write(downloadedTempData.getBytes());
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ContentData contentData[] = new ContentData[name.length];
                    for (i = 0; i < name.length; i++) {
                        contentData[i] = new ContentData(getApplicationContext(), name[i], image[i], link[i], date[i], "", database_name[i]);
                    }
                    recentData = new ContentData[5];
                    String tempString = quick_picks;
                    try
                    {
                        quick_picks.length();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        quick_picks="no data";
                    }
                    if (!quick_picks.equals("no data")) {
                        for (i = 0; i < 5; i++) {
                            String str = tempString.substring(0, tempString.indexOf('\n'));
                            if (i < 4)
                                tempString = tempString.substring(tempString.indexOf('\n') + 1);
                            String category = null, number = null;
                            int j;
                            for (j = 0; j < str.length(); j++) {
                                if (str.charAt(j) > '0' && str.charAt(j) <= '9') {
                                    category = str.substring(0, j);
                                    number = str.substring(j);
                                    break;
                                }
                            }
                            String imageTemp = "", dateTemp = "";
                            if (category.equals("sport"))
                                imageTemp = snapshot.child("sports_image_link").getValue().toString();
                            else
                                imageTemp = snapshot.child(category + "image" + number).getValue().toString();
                            if (!category.equals("kids") && !category.equals("sport"))
                                dateTemp = snapshot.child(category + "date" + number).getValue().toString();
                            recentData[i] = new ContentData(getApplicationContext(), snapshot.child(category + number).getValue().toString(),
                                    imageTemp, snapshot.child(category + "link" + number).getValue().toString(), dateTemp, "", str);
                        }
                    } else {
                        int latestData[] = new int[contentData.length];
                        for (i = 0; i < contentData.length; i++) {
                            latestData[i] = 0;
                            for (count = local_history.length() - contentData[i].getName().length() - 1; count >= 0 && count >= local_history.length() - 500 && latestData[i] == 0; count--) {
                                if (local_history.substring(count).contains("\t" + contentData[i].getName() + "\n"))
                                    latestData[i] = count;
                            }
                        }
                        int temp;
                        ContentData tempData;
                        ContentData dummyData[] = contentData;
                        for (i = 0; i <= contentData.length - 2; i++) {
                            for (int j = 0; j <= dummyData.length - 2 - i; j++) {
                                if (latestData[j] < latestData[j + 1]) {
                                    temp = latestData[j];
                                    latestData[j] = latestData[j + 1];
                                    latestData[j + 1] = temp;
                                    tempData = dummyData[j];
                                    dummyData[j] = dummyData[j + 1];
                                    dummyData[j + 1] = tempData;
                                }
                            }
                        }
                        recentData = new ContentData[5];
                        quick_picks = "";
                        for (i = 0; i <= recentData.length - 1; i++) {
                            recentData[i] = dummyData[i];
                            quick_picks = quick_picks + recentData[i].getDataBaseName() + "\n";
                        }
                        UserName.setQuickPicks(quick_picks);
                        new Sync().addToQuickPicks(getApplicationContext(), recentData[0].getDataBaseName());
                    }
                    SmallContentAdapter smallContentAdapter = new SmallContentAdapter(recentData,MainActivity.this, dark);
                    if (smallContentAdapter.getItemCount() == 0) {
                        getRecents();
                        return;
                    }
                    firestore = FirebaseFirestore.getInstance();
                    DocumentReference documentReference = firestore.collection("Data").document("Trending");
                    Map<String, Object> trend = new HashMap<>();
                    for (i = 0; i < contentData.length; i++) {
                        if (i < 5) trend.put(String.valueOf(i + 1), recentData[i].getName());
                        else trend.put(String.valueOf(i + 1), contentData[i].getName());
                    }
                    trend.put("Hour", Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                    trend.put("User", user.getEmail());
                    if (trend_hour != Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                        documentReference.set(trend);
                    trendData = new ContentData[5];
                    count = 0;
                    if(contentData==null||trend_name==null||trendData==null)
                    {
                        Toast.makeText(getApplicationContext(),"Resetting up the app...",Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(new Intent(getApplicationContext(),SplashActivity.class));
                        return;
                    }
                    for (i = 0; i <= 9 && count < 5; i++) {
                        for (int j = 0; j < contentData.length; j++) {
                            if (trend_name[i].equals(contentData[j].getName())) {
                                trendData[count] = contentData[j];
                                count++;
                                break;
                            }
                        }
                    }
                    if(show) return;
                    SmallContentAdapter trendAdapter = new SmallContentAdapter(trendData,MainActivity.this, dark);
                    if(((SmallContentAdapter)trendView.getAdapter()).isShimmer()) trendView.setAdapter(trendAdapter);
                    ((SmallContentAdapter)trendView.getAdapter()).setContentData(trendData);
                    trendView.setVisibility(View.VISIBLE);
                    findViewById(R.id.trend).setVisibility(View.VISIBLE);
                    if(((SmallContentAdapter)recyclerView.getAdapter()).isShimmer()) recyclerView.setAdapter(smallContentAdapter);
                    ((SmallContentAdapter)recyclerView.getAdapter()).setContentData(recentData);
                    recyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.quick).setVisibility(View.VISIBLE);
                    recentlyAddedContent = new ContentData[5];
                    String temporary_name, contentName, contentImage, contentLink, contentDescription;
                    for (i = 0; i <= 4; i++) {
                        for (int j = 0; j < latestContent[i].length(); j++) {
                            if (latestContent[i].charAt(j) >= '1' && latestContent[i].charAt(j) <= '9') {
                                temporary_name = latestContent[i].substring(0, j);
                                contentName = snapshot.child(latestContent[i]).getValue().toString();
                                contentLink = snapshot.child(temporary_name + "link" + latestContent[i].substring(j)).getValue().toString();
                                if (temporary_name.equals("sport"))
                                    contentImage = snapshot.child("sports_image_link").getValue().toString();
                                else
                                    contentImage = snapshot.child(temporary_name + "image" + latestContent[i].substring(j)).getValue().toString();
                                contentDescription = "";
                                try {
                                    contentDescription = snapshot.child(temporary_name + "date" + latestContent[i].substring(j)).getValue().toString();
                                } catch (Exception exception) {
                                    exception.printStackTrace();
                                }
                                recentlyAddedContent[i] = new ContentData(getApplicationContext(), contentName, contentImage, contentLink, contentDescription,
                                        "", latestContent[i]);
                                break;
                            }
                        }
                    }
                    if(((SmallContentAdapter)recentlyAdded.getAdapter()).isShimmer()) recentlyAdded.setAdapter(new SmallContentAdapter(recentlyAddedContent, MainActivity.this, dark));
                    ((SmallContentAdapter)recentlyAdded.getAdapter()).setContentData(recentlyAddedContent);
                    recentlyAdded.setVisibility(View.VISIBLE);
                    findViewById(R.id.recently_added).setVisibility(View.VISIBLE);
                    findViewById(R.id.settings).setVisibility(View.VISIBLE);
                    findViewById(R.id.load_options).setVisibility(View.GONE);
                    ((TextView)findViewById(R.id.profile_email)).setText("Tap for more information");
                    first = false;
                    int select = (int) (System.currentTimeMillis() % movieData.length);
                    if (getIntent().hasExtra("personal") && personal) {
                        if (movieData[select].getLink().contains("(video)")) {
                            Intent intent = new Intent(getApplicationContext(), DescriptionActivity.class);
                            if (dark) intent.putExtra("dark", true);
                            intent.putExtra("name", movieData[select].getName());
                            intent.putExtra("description", movieData[select].getDate());
                            intent.putExtra("image", movieData[select].getImage());
                            intent.putExtra("link", movieData[select].getLink().substring("(video)".length()));
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                            if (dark) intent.putExtra("dark", true);
                            intent.putExtra("search", movieData[select].getName());
                            intent.putExtra("signed_in", true);
                            intent.putExtra("personal", true);
                            startActivity(intent);
                        }
                        finish();
                    }
                    personal = false;
                    if (restarted || new File(getApplicationContext().getFilesDir(), "Alarm.txt").exists()) {
                        restarted = false;
                        return;
                    }
                    try {
                        new File(getApplicationContext().getFilesDir(), "Alarm.txt").createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    select = (int) ((System.currentTimeMillis() + 5) % movieData.length);
                    AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    Calendar calendar = Calendar.getInstance();
                    Calendar actualCalendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    int hour = actualCalendar.get(Calendar.HOUR_OF_DAY) + 3;
                    int minute = 0;
                    if(last_sign_in==null) last_sign_in="0000/00/00 at 00:00";
                    if (Integer.parseInt(last_sign_in.substring(0, 4)) == actualCalendar.get(Calendar.YEAR)
                            && Integer.parseInt(last_sign_in.substring(5, 7)) == actualCalendar.get(Calendar.MONTH) + 1
                            && Integer.parseInt(last_sign_in.substring(8, 10)) == actualCalendar.get(Calendar.DAY_OF_MONTH)
                            && Integer.parseInt(last_sign_in.substring(14, 16)) > actualCalendar.get(Calendar.HOUR_OF_DAY) - 3) {
                        restarted = false;
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(calendar.get(Calendar.YEAR) + "/");
                    if (calendar.get(Calendar.MONTH) + 1 < 10) sb.append("0");
                    sb.append((calendar.get(Calendar.MONTH) + 1) + "/");
                    if (calendar.get(Calendar.DAY_OF_MONTH) < 10) sb.append("0");
                    sb.append(calendar.get(Calendar.DAY_OF_MONTH) + " at ");
                    if (calendar.get(Calendar.HOUR_OF_DAY) < 10) sb.append("0");
                    sb.append(calendar.get(Calendar.HOUR_OF_DAY) + ":");
                    if (calendar.get(Calendar.MINUTE) < 10) sb.append("0");
                    sb.append(calendar.get(Calendar.MINUTE));
                    last_sign_in = sb.toString();
                    if (hour >= 24) {
                        calendar.set(Calendar.DAY_OF_YEAR, 1 + actualCalendar.get(Calendar.DAY_OF_YEAR));
                        hour = hour % 24;
                    }
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);
                    Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
                    intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 6 * AlarmManager.INTERVAL_HOUR, pendingIntent);
                    String str = "";
                    if (hour < 10) str = str + "0";
                    str = str + hour + ":";
                    if (minute < 10) str = str + "0";
                    str = str + minute;
                    next_notification = str;
                    Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider",
                            new File(getApplicationContext().getExternalFilesDir(null), ".notification.jpg"));
                    getContentResolver().delete(uri, null, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void makeWatchTimingsFile()
    {
        File file = new File(this.getFilesDir(),"UserWatchTimings.txt");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                FileOutputStream fos;
                movie_watch_session_times="";
                try {
                    fos = openFileOutput("UserWatchTimings.txt",MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                }
                int i;
                FileInputStream fileInputStream;
                for(i=1;i<=Integer.parseInt(snapshot.child("movie").getValue().toString());i++)
                {
                    if(new File(getApplicationContext().getFilesDir(),snapshot.child("movie"+i).getValue().toString().replace(' ','+')+".txt").exists())
                    {
                        try {
                            fileInputStream=openFileInput(snapshot.child("movie"+i).getValue().toString().replace(' ','+')+".txt");
                            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream,StandardCharsets.UTF_8));
                            String str=br.readLine();
                            fos.write((snapshot.child("movie"+i).getValue().toString().replace(' ','+')+"\n" +str).getBytes());
                            watch_time.append(snapshot.child("movie"+i).getValue().toString().replace(' ','+')+".txt\n"+str).append('\n');
                            movie_watch_session_times=movie_watch_session_times +
                                    snapshot.child("movie"+i).getValue().toString().replace(' ','+')+".txt\n"+str+"\n";
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                for(i=1;i<=Integer.parseInt(snapshot.child("comedy").getValue().toString());i++)
                {
                    if(new File(getApplicationContext().getFilesDir(),snapshot.child("comedy"+i).getValue().toString().replace(' ','+')+".txt").exists())
                    {
                        try {
                            fileInputStream=openFileInput(snapshot.child("comedy"+i).getValue().toString().replace(' ','+')+".txt");
                            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream,StandardCharsets.UTF_8));
                            String str=br.readLine();
                            fos.write((snapshot.child("comedy"+i).getValue().toString().replace(' ','+')+"\n" +str).getBytes());
                            watch_time.append(snapshot.child("comedy"+i).getValue().toString().replace(' ','+')+".txt\n"+str).append('\n');
                            movie_watch_session_times=movie_watch_session_times +
                                    snapshot.child("comedy"+i).getValue().toString().replace(' ','+')+".txt\n"+str+"\n";
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                for(i=1;i<=Integer.parseInt(snapshot.child("classic").getValue().toString());i++)
                {
                    if(new File(getApplicationContext().getFilesDir(),snapshot.child("classic"+i).getValue().toString().replace(' ','+')+".txt").exists())
                    {
                        try {
                            fileInputStream=openFileInput(snapshot.child("classic"+i).getValue().toString().replace(' ','+')+".txt");
                            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream,StandardCharsets.UTF_8));
                            String str = br.readLine();
                            fos.write((snapshot.child("classic"+i).getValue().toString().replace(' ','+')+"\n" +str).getBytes());
                            watch_time.append(snapshot.child("classic"+i).getValue().toString().replace(' ','+')+".txt\n"+str).append('\n');
                            movie_watch_session_times=movie_watch_session_times +
                                    snapshot.child("classic"+i).getValue().toString().replace(' ','+')+".txt\n"+str+"\n";
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(movie_watch_session_times.equals("")) movie_watch_session_times="0";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private String getDownloadedMovies()
    {
        String name, downloaded;
        downloaded="";
        FileInputStream fileInputStream;
        try {
            fileInputStream = openFileInput("DownloadedVideos.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            while ((name= bufferedReader.readLine())!=null)
            {
                if(new File(getApplicationContext().getExternalFilesDir(null),name.replace(' ','_').replace(':','_')+".mp4").exists())
                {
                    downloaded = downloaded + name + "\n";
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return downloaded;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return downloaded;
    }
    private void makeTimeFiles()
    {
        try {
            if(!new File(this.getFilesDir(),"UserWatchTimings.txt").exists()) new File(this.getFilesDir(),"UserWatchTimings.txt").createNewFile();
            FileOutputStream fos = openFileOutput("UserWatchTimings.txt",MODE_PRIVATE);
            fos.write(movie_watch_session_times.getBytes());
            fos.close();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        FileInputStream fis = openFileInput("UserWatchTimings.txt");
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                        FileOutputStream fileOutputStream;
                        String str;
                        while ((str=br.readLine())!=null)
                        {
                            File file = new File(getApplicationContext().getFilesDir(),str);
                            if(!file.exists()) file.createNewFile();
                            fileOutputStream=openFileOutput(str,MODE_PRIVATE);
                            str=br.readLine();
                            if(str==null)
                            {
                                findViewById(R.id.show_quick_and_trending).callOnClick();
                                return;
                            }
                            fileOutputStream.write(str.getBytes());
                            fileOutputStream.close();
                        }
                        if(movie_watch_session_times.equals("")) movie_watch_session_times="0";
                        if(user.isEmailVerified()) findViewById(R.id.show_quick_and_trending).callOnClick();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            },2000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if(show) showSettings(findViewById(R.id.settings));
        else super.onBackPressed();
    }

    private void deleteExistingApk()
    {
        if(new File(getApplicationContext().getExternalFilesDir(null),"Entertainment_"+
                BuildConfig.VERSION_NAME.replace(' ','_')+".apk").exists())
        {
            Uri updateFileUri = FileProvider.getUriForFile(getApplicationContext(),BuildConfig.APPLICATION_ID+".provider",
                    new File(getApplicationContext().getExternalFilesDir(null),"Entertainment_"+
                            BuildConfig.VERSION_NAME.replace(' ','_')+".apk"));
            getContentResolver().delete(updateFileUri,null,null);
        }
    }

    public void showDownloads(View view) {
        Intent intent = new Intent(this,QueuedDownloadsActivity.class);
        intent.putExtra("download",true);
        intent.putExtra("content_type","download");
        if(dark) intent.putExtra("dark",true);
        startActivity(intent);
    }

    public void playLocalVideo(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(Intent.createChooser(intent,"Complete action using..."),PICKFILE_RESULT_CODE);
    }
    private void sendIntroductoryNotification(int delay)
    {
        Intent intent = new Intent(getApplicationContext(),SplashActivity.class);
        intent.putExtra("search","movie");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),100,intent,PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"personal")
                .setContentTitle("Welcome "+user.getDisplayName())
                .setContentText("Let's explore some movies")
                .setSmallIcon(R.drawable.notification)
                .setColor(Color.RED)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        notification = builder.build();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationManagerCompat.from(getApplicationContext()).notify(999,notification);
            }
        },delay);
    }

    public void sendFeedback(View view) {
        Intent intent = new Intent(getApplicationContext(),SendFeedbackActivity.class);
        if(dark) intent.putExtra("dark",true);
        startActivity(intent);
        overridePendingTransition(R.anim.zoom_in_bottom,R.anim.fade_out);
    }
    public void broadcastMessage(View view) {
        FirebaseFirestore.getInstance().collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String[] recipients;
                int i = 0;
                for(QueryDocumentSnapshot testSnapshot : queryDocumentSnapshots)
                {
                    i++;
                }
                recipients = new String[i];
                i=0;
                for(QueryDocumentSnapshot testSnapshot : queryDocumentSnapshots)
                {
                    recipients[i] = testSnapshot.getId().toString();
                    i++;
                }
                Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
                mailIntent.setData(Uri.parse("mailto:"));
                mailIntent.putExtra(Intent.EXTRA_EMAIL,"team.entertainment108@gmail.com");
                mailIntent.putExtra(Intent.EXTRA_BCC,recipients);
                mailIntent.putExtra(Intent.EXTRA_SUBJECT,"");
                mailIntent.putExtra(Intent.EXTRA_TEXT,"\n\n\nGet the app here at https://drive.google.com/drive/folders/105N8dwqkgU5k7c-5Zot9wPn6peLICURV");
                startActivity(mailIntent);
            }
        });
    }

    public void showWatchlist(View view) {
        startActivity(new Intent(getApplicationContext(),WatchlistActivity.class));
    }
}