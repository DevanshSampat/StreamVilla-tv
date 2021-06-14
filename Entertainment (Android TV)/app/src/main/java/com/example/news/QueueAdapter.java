package com.example.news;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {
    private List<String> names;
    private boolean dark;
    private Context context;
    public QueueAdapter(boolean dark, Context context){
        this.dark = dark;
        this.context = context;
    }
    public void setNames(List<String> names){
        this.names = names;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.queue_download_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.name.setText(names.get(position));
        if(dark){
            holder.card.setCardBackgroundColor(Color.parseColor("#363636"));
            holder.name.setTextColor(Color.parseColor("#CCCCCC"));
        }
        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,SearchActivity.class);
                intent.putExtra("search",holder.name.getText());
                intent.putExtra("open",true);
                intent.putExtra("signed_in",true);
                if(dark) intent.putExtra("dark",true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DownloadNotification().dequeue(context,names.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CardView cancel;
        CardView card;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            cancel = itemView.findViewById(R.id.cancel);
            card = itemView.findViewById(R.id.card);
        }
    }
}
