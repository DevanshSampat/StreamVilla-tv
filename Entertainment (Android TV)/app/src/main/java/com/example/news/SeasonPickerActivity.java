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

public class SeasonPickerActivity extends AppCompatActivity {
    private int count;
    private String imageURL;
    private String dbName;
    private String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_season_picker);
        if(new Theme(this).isInDarkMode()) findViewById(R.id.layout).setBackgroundColor(Color.BLACK);
        name = getIntent().getStringExtra("name");
        dbName = getIntent().getStringExtra("dbName");
        imageURL = getIntent().getStringExtra("image");
        Picasso.with(this).load(imageURL).into((ImageView)findViewById(R.id.image));
        ((TextView)findViewById(R.id.header)).setText(name);
        final RecyclerView recyclerView = findViewById(R.id.recycle);
        recyclerView.requestFocus();
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerView.setHasFixedSize(true);
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    count = Integer.parseInt(snapshot.child(dbName + "seasons").getValue().toString());
                }
                catch (Exception e){
                    count = 0;
                }
                recyclerView.setAdapter(new SeasonAdapter(SeasonPickerActivity.this,name,imageURL,dbName,count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}