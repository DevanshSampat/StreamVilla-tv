package com.example.news;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.ViewHolder> {
    private String[] links;
    private String name;
    private int count;
    private Context context;
    private String imageURL;
    private String dbName;
    private boolean dark;
    public EpisodeAdapter(Context context, String name, String imageURL, String[] links, String dbName, int count){
        this.count = count;
        this.links = links;
        this.name = name;
        this.dbName = dbName;
        this.context = context;
        this.imageURL = imageURL;
        this.dark = new Theme(context).isInDarkMode();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        view = layoutInflater.inflate(R.layout.season_episode_item,parent,false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.textView.setText("Episode "+(position+1));
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(context,R.anim.zoom_in_recycle));
        holder.setIsRecyclable(false);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,VideoPlayerActivity.class);
                intent.putExtra("name",name+"E"+(position+1));
                intent.putExtra("raw_name",name+"E");
                intent.putExtra("online",true);
                intent.putExtra("dbName",dbName);
                intent.putExtra("episode_number",(position+1)+"");
                intent.putExtra("link",links[position]);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layout;
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.layout);
            textView = itemView.findViewById(R.id.text);
        }
    }
}
