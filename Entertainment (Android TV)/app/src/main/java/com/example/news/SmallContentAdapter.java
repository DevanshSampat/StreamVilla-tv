package com.example.news;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import static android.content.Context.MODE_APPEND;
import static android.content.Context.MODE_PRIVATE;

public class SmallContentAdapter extends RecyclerView.Adapter<SmallContentAdapter.ViewHolder>{
    ContentData contentData[];
    Context context;
    private ViewHolder holder;
    private int position;
    private boolean dark;
    private boolean shimmer = false;

    private void printDate()
    {
        File temp_history = new File(context.getFilesDir(),"TempHistory.txt");
        try {
            File file = new File(context.getFilesDir(),"Date.txt");
            if(!file.exists())
            {
                file.createNewFile();
                try {
                    FileOutputStream fileOutputStream = context.openFileOutput("Date.txt",MODE_PRIVATE);
                    fileOutputStream.write("00000000".getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileInputStream fis = null;
            fis = context.openFileInput("Date.txt");
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
                    FileOutputStream fileOutputStream = context.openFileOutput("History.txt",MODE_PRIVATE);
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
                    FileOutputStream fos = context.openFileOutput("Date.txt", MODE_PRIVATE);
                    fos.write((calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.DAY_OF_MONTH)).getBytes());
                    fos.close();
                    File file_history = new File(context.getFilesDir(),"History.txt");
                    fos = context.openFileOutput("History.txt",MODE_APPEND);
                    fos.write(("\n"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+(1+calendar.get(Calendar.MONTH))+"\n"+"\n").getBytes());
                    fos = context.openFileOutput("TempHistory.txt",MODE_APPEND);
                    fos.write(("\n"+calendar.get(Calendar.DAY_OF_MONTH)+"/"+(1+calendar.get(Calendar.MONTH))+"\n"+"\n").getBytes());
                    try {
                        FileOutputStream fileOutputStream = context.openFileOutput("Backup.txt",MODE_PRIVATE);
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
            FileOutputStream fos = context.openFileOutput("History.txt", MODE_APPEND);
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

    public SmallContentAdapter(ContentData contentData[], Context context, boolean dark)
    {
        this.contentData=contentData;
        this.context = context;
        this.dark=dark;
    }
    public SmallContentAdapter(boolean shimmer, Context context, boolean dark)
    {
        this.shimmer=shimmer;
        this.context = context;
        this.dark=dark;
    }
    public boolean isShimmer(){return shimmer;}
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.small_recycle_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        this.holder = holder;
        this.position = position;
        if(dark)
        {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#363636"));
            holder.textView.setTextColor(Color.WHITE);
        }
        if(shimmer||contentData==null)
        {
            holder.shimmerFrameLayout.startShimmer();
            Shimmer.ColorHighlightBuilder builder = new Shimmer.ColorHighlightBuilder()
                    .setBaseAlpha(1)
                    .setHighlightColor(Color.parseColor("#E7E7E7"))
                    .setHighlightAlpha(1)
                    .setDropoff(50);
            if(dark)
            {
                builder.setBaseColor(Color.DKGRAY);
            }
            else builder.setBaseColor(Color.GRAY);
            holder.shimmerFrameLayout.setShimmer(builder.build());
            holder.textView.setText("");
            if(dark) {
                holder.imageView.setBackgroundColor(Color.DKGRAY);
                holder.textView.setBackgroundColor(Color.DKGRAY);
            }
            else
            {
                holder.imageView.setBackgroundColor(Color.LTGRAY);
                holder.textView.setBackgroundColor(Color.LTGRAY);
            }
            return;
        }
        holder.shimmerFrameLayout.stopShimmer();
        holder.shimmerFrameLayout.setShimmer(null);
        if(!contentData[position].getLink().equals("webseries")) holder.progressBar.setProgress(contentData[position].getProgressPercent());
        else holder.progressBar.setVisibility(View.INVISIBLE);
        holder.textView.setText(contentData[position].getName());
        Picasso.with(holder.imageView.getContext()).load(contentData[position].getImage()).into(holder.imageView);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if(contentData[position].getLink().contains("(video)"))
                {
                    intent = new Intent(context,DescriptionActivity.class);
                    intent.putExtra("link",contentData[position].getLink().substring("(video)".length()));
                    intent.putExtra("name",contentData[position].getName());
                    intent.putExtra("image",contentData[position].getImage());
                    intent.putExtra("description",contentData[position].getDate());
                    intent.putExtra("movie_db",contentData[position].getDataBaseName());
                    if(contentData[position].getDataBaseName()!=null) intent.putExtra("add_to_quick_picks",contentData[position].getDataBaseName());
                    if(dark) intent.putExtra("dark",true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else if(contentData[position].getLink().equals("webseries")){
                    /*intent = new Intent(context,SeasonPickerActivity.class);
                    intent.putExtra("name",contentData[position].getName());
                    intent.putExtra("dbName",contentData[position].getDataBaseName());
                    intent.putExtra("image",contentData[position].getImage());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    printHistory(contentData[position].getName());
                    new Sync().uploadHistory(context);
                    new Sync().addToQuickPicks(context,contentData[position].getDataBaseName());
                    context.startActivity(intent);
                */
                    showWatchPopUpForWebSeries(position);
                }
                else
                {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(contentData[position].getLink()));
                    printHistory(contentData[position].getName());
                    new Sync().uploadHistory(context);
                    new Sync().addToQuickPicks(context,contentData[position].getDataBaseName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(contentData[position].getLink().equals("webseries")) {
                    showWatchPopUpForWebSeries(position);
                    return false;
                }
                final Dialog dialog = new Dialog(context);
                dialog.setCancelable(true);
                dialog.setContentView(R.layout.watch_pop_up);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
               ((TextView)dialog.findViewById(R.id.title)).setText(contentData[position].getName());
                ((TextView)dialog.findViewById(R.id.description)).setText(contentData[position].getDate());
                Picasso.with(context).load(contentData[position].getImage()).into((ImageView)dialog.findViewById(R.id.image));
                if(!contentData[position].getLink().contains("(video)")
                    || new File(context.getExternalFilesDir(null),
                        contentData[position].getName().replace(' ','_')
                                .replace(':','_')+".mp4").exists()) dialog.findViewById(R.id.more_options).setVisibility(View.GONE);
                else {
                    dialog.findViewById(R.id.more_options).startAnimation(AnimationUtils.loadAnimation(context,R.anim.appear_up));
                }
                dialog.findViewById(R.id.more_options).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new DownloadNotification().enqueueDownload(context, contentData[position].getName(),
                                contentData[position].getLink().substring("(video)".length()));
                        if (!new File(context.getFilesDir(), "isDownloading.txt").exists())
                            new DownloadNotification().goToNextTask(context, dark);
                        dialog.dismiss();
                    }
                });
                if(contentData[position].getProgressPercent()>0)
                {
                    ObjectAnimator progressAnimator = ObjectAnimator.ofInt((ProgressBar)dialog.findViewById(R.id.progress),
                            "progress",
                            0,contentData[position].getProgressPercent());
                    progressAnimator.setDuration(500);
                    progressAnimator.start();
                    ((ProgressBar)dialog.findViewById(R.id.progress)).setProgress(contentData[position].getProgressPercent());
                    String string = toStandardTimeForm(contentData[position].getProgress()/1000)+
                            " / "+toStandardTimeForm(contentData[position].getLength()/1000);
                    ((TextView) dialog.findViewById(R.id.watch_text)).setText(string);
                }
                if(new File(context.getExternalFilesDir(null),
                        contentData[position].getName().replace(' ','_').replace(':','_')+".mp4").exists()
                        && !isFileDownloading(contentData[position].getName()))
                {
                    String string = ((TextView)dialog.findViewById(R.id.watch_text)).getText().toString()+" (Offline)";
                    ((TextView)dialog.findViewById(R.id.watch_text)).setText(string);
                }
                if(dark)
                {
                    dialog.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#303030"));
                    ((TextView)dialog.findViewById(R.id.title)).setTextColor(Color.parseColor("#CCCCCC"));
                    ((TextView)dialog.findViewById(R.id.description)).setTextColor(Color.parseColor("#CCCCCC"));
                }
                else ((CardView)dialog.findViewById(R.id.watch_card)).setCardBackgroundColor(Color.parseColor("#CCCCCC"));
                try {
                    String dbName = contentData[position].getDataBaseName();
                    if (!dbName.startsWith("comedy")&&!dbName.startsWith("movie")&&!dbName.startsWith("classic")) {
                        dialog.findViewById(R.id.watchlist).setVisibility(View.GONE);
                    } else
                        dialog.findViewById(R.id.watchlist).startAnimation(AnimationUtils.loadAnimation(context, R.anim.appear_up));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try{
                    ArrayList<String> watchList = UserName.getWatchList();
                    if(watchList.contains(contentData[position].getDataBaseName()))
                        ((TextView)dialog.findViewById(R.id.text_watchlist)).setText("Remove from Watchlist");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.findViewById(R.id.watchlist).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(((TextView)dialog.findViewById(R.id.text_watchlist)).getText().equals("Add to Watchlist")){
                            new Sync().addToWatchList(context,contentData[position].getDataBaseName());
                            ((TextView)dialog.findViewById(R.id.text_watchlist)).setText("Remove from Watchlist");
                        }
                        else{
                            new Sync().removeFromWatchList(context,contentData[position].getDataBaseName());
                            ((TextView)dialog.findViewById(R.id.text_watchlist)).setText("Add to Watchlist");
                        }
                    }
                });
                dialog.findViewById(R.id.watch_card).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        if(!contentData[position].getLink().contains("(video)")) holder.cardView.callOnClick();
                        else
                        {
                            Intent intent = new Intent(context, VideoPlayerActivity.class);
                            intent.putExtra("name", contentData[position].getName());
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("link", contentData[position].getLink().substring("(video)".length()));
                            intent.putExtra("online",true);
                            intent.putExtra("movie_db",contentData[position].getDataBaseName());
                            printHistory(contentData[position].getName());
                            new Sync().addToQuickPicks(context, contentData[position].getDataBaseName());
                            if(!new File(context.getFilesDir(),"Tutorial.txt").exists())
                            {
                                Intent tutorialIntent = new Intent(context,TutorialActivity.class);
                                tutorialIntent.putExtras(intent.getExtras());
                                tutorialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if(dark) tutorialIntent.putExtra("dark",true);
                                context.startActivity(tutorialIntent);
                                try {
                                    new File(context.getFilesDir(),"Tutorial.txt").createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return;
                            }
                            context.startActivity(intent);
                        }
                    }
                });
                WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
                lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                lp.y = 100;
                lp.verticalMargin = 5;
                lp.horizontalMargin = 5;
                dialog.getWindow().setAttributes(lp);
                dialog.show();
                dialog.findViewById(R.id.title).startAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_in));
                dialog.findViewById(R.id.description).startAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_in));
                dialog.findViewById(R.id.image).startAnimation(AnimationUtils.loadAnimation(context,R.anim.fade_in));
                dialog.findViewById(R.id.watch_card).startAnimation(AnimationUtils.loadAnimation(context,R.anim.appear_up));
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialog.getWindow().setDimAmount(0.90f);
                return false;
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void showWatchPopUpForWebSeries(final int position) {
        final ContentData data = contentData[position];
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.watch_pop_up);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.getWindow().setDimAmount(0.90f);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        lp.y = 100;
        lp.verticalMargin = 5;
        lp.horizontalMargin = 5;
        dialog.getWindow().setAttributes(lp);
        String season = "1";
        String episode = "1";
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        String combo = "s1e1";
        String videoPosition = "0";
        ((TextView)dialog.findViewById(R.id.text_download)).setText("See all Seasons & Episodes");
        ((TextView)dialog.findViewById(R.id.title)).setText(data.getName());
        ((TextView)dialog.findViewById(R.id.description)).setText(data.getDate());
        dialog.findViewById(R.id.watch_card).startAnimation(AnimationUtils.loadAnimation(context,R.anim.appear_up));
        dialog.findViewById(R.id.more_options).startAnimation(AnimationUtils.loadAnimation(context,R.anim.appear_up));
        dialog.findViewById(R.id.watchlist).startAnimation(AnimationUtils.loadAnimation(context,R.anim.appear_up));
        try{
            ArrayList<String> watchList = UserName.getWatchList();
            if(watchList.contains(data.getDataBaseName()))
                ((TextView)dialog.findViewById(R.id.text_watchlist)).setText("Remove from Watchlist");
        } catch (Exception e) {
            e.printStackTrace();
        }
        dialog.findViewById(R.id.watchlist).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(((TextView)dialog.findViewById(R.id.text_watchlist)).getText().equals("Add to Watchlist")){
                    new Sync().addToWatchList(context,data.getDataBaseName());
                    ((TextView)dialog.findViewById(R.id.text_watchlist)).setText("Remove from Watchlist");
                }
                else{
                    new Sync().removeFromWatchList(context,data.getDataBaseName());
                    ((TextView)dialog.findViewById(R.id.text_watchlist)).setText("Add to Watchlist");
                }
            }
        });
        dialog.findViewById(R.id.more_options).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent intent = new Intent(context,SeasonPickerActivity.class);
                intent.putExtra("name",data.getName());
                intent.putExtra("dbName",data.getDataBaseName());
                intent.putExtra("image",data.getImage());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                printHistory(data.getName());
                new Sync().uploadHistory(context);
                new Sync().addToQuickPicks(context,data.getDataBaseName());
                context.startActivity(intent);
            }
        });
        Picasso.with(context).load(data.getImage()).into((ImageView)dialog.findViewById(R.id.image));
        if(dark)
        {
            dialog.findViewById(R.id.layout).setBackgroundColor(Color.parseColor("#303030"));
            ((TextView)dialog.findViewById(R.id.title)).setTextColor(Color.parseColor("#CCCCCC"));
            ((TextView)dialog.findViewById(R.id.description)).setTextColor(Color.parseColor("#CCCCCC"));
        }
        else ((CardView)dialog.findViewById(R.id.watch_card)).setCardBackgroundColor(Color.parseColor("#CCCCCC"));
        if(new File(context.getFilesDir(),data.getName().replace(' ','+')+".txt").exists()){
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(data.getName().replace(' ','+')+".txt")));
                String str = br.readLine();
                season = str.substring(0,str.indexOf('\t'));
                episode = str.substring(str.indexOf('\t')+1);
                if(episode.contains("\t")){
                    videoPosition = episode.substring(episode.indexOf('\t')+1);
                    episode = episode.substring(0,episode.indexOf("\t"));
                }
                combo = "s"+season+"e"+episode;
                ((TextView)dialog.findViewById(R.id.watch_text)).setText("Continue : Season "+season+" - Episode "+episode);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        final String finalEpisode = episode;
        final String finalSeason = season;
        final String finalCombo = combo;
        final String finalVideoPosition = videoPosition;
        dialog.findViewById(R.id.watch_card).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                String name = data.getName();
                final String dbName = data.getDataBaseName();
                final Intent intent = new Intent(context,VideoPlayerActivity.class);
                intent.putExtra("name",name+" : S"+finalSeason+"E"+finalEpisode);
                intent.putExtra("raw_name",name+" : S"+finalSeason+"E");
                intent.putExtra("online",true);
                intent.putExtra("dbName",dbName+"s"+ finalSeason);
                intent.putExtra("episode_number", finalEpisode);
                intent.putExtra("position", finalVideoPosition);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                printHistory(data.getName());
                new Sync().uploadHistory(context);
                new Sync().addToQuickPicks(context,data.getDataBaseName());
                FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            intent.putExtra("link",snapshot.child(dbName+ finalCombo).getValue().toString());
                            context.startActivity(intent);
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        dialog.show();
    }

    private String toStandardTimeForm(int time)
    {
        String str = "";
        if(time/3600>0) str = str + time/3600 +":";
        if((time%3600)/60<10) str = str + "0";
        str = str + (time%3600)/60 + ":";
        if(time%60<10) str = str + "0";
        str = str + time%60;
        return str;
    }
    private boolean isFileDownloading(String name)
    {
        try {
            FileInputStream fis = context.openFileInput("isDownloading.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            if(br.readLine().equals(name)) return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    @Override
    public int getItemCount() {
        if(contentData==null) return 5;
        return contentData.length;
    }
    public void setContentData(ContentData[] contentData)
    {
        this.contentData = contentData;
        shimmer = false;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView textView;
        CardView cardView;
        TextView subtitle;
        ShimmerFrameLayout shimmerFrameLayout;
        ProgressBar progressBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.title);
            cardView = itemView.findViewById(R.id.content_details);
            progressBar = itemView.findViewById(R.id.movie_progress);
            shimmerFrameLayout = itemView.findViewById(R.id.shimmer_frame);
        }
    }
}