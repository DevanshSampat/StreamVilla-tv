package com.example.news;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.Objects;

public class NotificationReceiver extends BroadcastReceiver {
    private Notification notification;
    private String name;
    private String image;
    private String date;
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.println(Log.ASSERT,"Pending Intent","Delivered");
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.println(Log.ASSERT,"DB","connected");
                int i,count=0;
                ContentData contentData[] = new ContentData[Integer.parseInt(snapshot.child("movie").getValue().toString())
                        + Integer.parseInt(snapshot.child("classic").getValue().toString())];
                for(i=1;i<=Integer.parseInt(snapshot.child("movie").getValue().toString());i++)
                {
                    contentData[count] = new ContentData(snapshot.child("movie"+i).getValue().toString(),
                            snapshot.child("movieimage"+i).getValue().toString(),
                            snapshot.child("movielink"+i).getValue().toString(),
                            snapshot.child("moviedate"+i).getValue().toString());
                    count++;
                }
                for(i=1;i<=Integer.parseInt(snapshot.child("classic").getValue().toString());i++)
                {
                    contentData[count] = new ContentData(snapshot.child("classic"+i).getValue().toString(),
                            snapshot.child("classicimage"+i).getValue().toString(),
                            snapshot.child("classiclink"+i).getValue().toString(),
                            snapshot.child("classicdate"+i).getValue().toString());
                    count++;
                }
                int select = (int) (System.currentTimeMillis()%contentData.length);
                Log.println(Log.ASSERT,"Random","picked");
                name = contentData[select].getName();
                image=contentData[select].getImage();
                date=contentData[select].getDate().substring(0,4);
                String message = "Want to try something new? Let us help";
                if (new File(context.getFilesDir(), name.replace(' ', '+') + ".txt").exists())
                    message = "Pick-up from where you left off";
                Intent startIntent = new Intent(context, SplashActivity.class);
                startIntent.putExtra("search", name);
                startIntent.putExtra("open", true);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "personal")
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.notification)
                        .setColor(Color.RED)
                        .setSound(null)
                        .setContentTitle(name+" ("+date+")")
                        .setContentText(message)
                        .setContentIntent(pendingIntent);
                Bitmap bitmap;
                bitmap = BitmapFactory.decodeFile("/storage/emulated/0/android/data/com.example.news/files/.icon.jpg");
                builder.setLargeIcon(bitmap);
                Log.println(Log.ASSERT,"Storage","Accessed");
                notification = builder.build();
                NotificationManagerCompat.from(context).notify(999, notification);
                Log.println(Log.ASSERT,"Notification","Sent");
                Picasso.with(context).load(image).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bit, Picasso.LoadedFrom from) {
                        builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bit));
                        notification = builder.build();
                        NotificationManagerCompat.from(context).cancel(999);
                        NotificationManagerCompat.from(context).notify(999, notification);
                        Log.println(Log.ASSERT,"Content",name);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.println(Log.ASSERT,"Status","Cancelled");
            }
        });

    }
}
