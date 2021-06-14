package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class WatchlistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<String> watchList;
    private ContentData[] contentData;
    private BroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchlist);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onRestart();
            }
        };
        registerReceiver(broadcastReceiver,new IntentFilter("REMOVED_FROM_WATCHLIST"));
        recyclerView = findViewById(R.id.recycle);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (new Theme(this).isInDarkMode())
            findViewById(R.id.layout).setBackgroundColor(Color.BLACK);
        try {
            watchList = UserName.getWatchList();
            contentData = new ContentData[watchList.size()];
            if(watchList!=null){
                loadWatchList();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername(getApplicationContext()))
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try {
                    watchList = (ArrayList<String>) documentSnapshot.get("Watchlist");
                    contentData = new ContentData[watchList.size()];
                    if (watchList.size() == 0) {
                        Toast.makeText(getApplicationContext(), "Watchlist is empty", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    loadWatchList();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Watchlist is empty", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void loadWatchList() {
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserName.setWatchlist(watchList);
                if(watchList.size()==0){
                    Toast.makeText(getApplicationContext(), "Watchlist is empty", Toast.LENGTH_SHORT).show();
                    finish();
                }
                for(int i=0;i<watchList.size();i++){
                    String genre = null;
                    String number = null;
                    String item = watchList.get(i);
                    String[] category = new String[]{"comedy","movie","classic"};
                    for(int j=0;j<category.length;j++){
                        if(item.startsWith(category[j])){
                            genre = category[j];
                            number = item.substring(category[j].length());
                            break;
                        }
                    }
                    contentData[i] = new ContentData(WatchlistActivity.this,snapshot.child(genre+number).getValue().toString(),
                            snapshot.child(genre+"image"+number).getValue().toString(),
                            snapshot.child(genre+"link"+number).getValue().toString(),
                            snapshot.child(genre+"date"+number).getValue().toString(),
                            "",genre+number);
                }
                ContentAdapter contentAdapter = new ContentAdapter(contentData,WatchlistActivity.this,new Theme(getApplicationContext()).isInDarkMode());
                contentAdapter.setAnimate(false);
                recyclerView.setAdapter(contentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        try{
            watchList = UserName.getWatchList();
            contentData = new ContentData[watchList.size()];
            if(watchList.size()==0) {
                Toast.makeText(getApplicationContext(),"Watchlist is empty",Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            loadWatchList();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),"Watchlist is empty",Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}