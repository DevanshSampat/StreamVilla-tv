package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class EpisodePickerActivity extends AppCompatActivity {
    private int count;
    private String[] links;
    private String dbName;
    private String name;
    private String imageURL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_picker);
        if(new Theme(this).isInDarkMode()) findViewById(R.id.layout).setBackgroundColor(Color.BLACK);
        name = getIntent().getStringExtra("name");
        dbName = getIntent().getStringExtra("dbName");
        imageURL = getIntent().getStringExtra("image");
        Picasso.with(this).load(imageURL).into((ImageView)findViewById(R.id.image));
        String header = name;
        header = header.substring(0,header.lastIndexOf('S'));
        header = header + "Season "+name.substring(name.lastIndexOf('S')+1);
        ((TextView)findViewById(R.id.header)).setText(header);
        final RecyclerView recyclerView = findViewById(R.id.recycle);
        recyclerView.requestFocus();
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setHasFixedSize(true);
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    count = Integer.parseInt(snapshot.child(dbName).getValue().toString());
                }
                catch (Exception e){
                    count = 0;
                }
                links = new String[count];
                for(int i=0;i<count;i++){
                    try{
                        links[i] = snapshot.child(dbName+"e"+(i+1)).getValue().toString();
                    } catch (Exception exception) {
                        links[i] = "";
                    }
                }
                recyclerView.setAdapter(new EpisodeAdapter(EpisodePickerActivity.this,name,imageURL,links,dbName,count));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}