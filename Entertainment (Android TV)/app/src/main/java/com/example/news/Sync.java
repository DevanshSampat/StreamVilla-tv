package com.example.news;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Sync {
    private DocumentReference documentReference;
    private boolean add=false;
    private int attempt;
    private ArrayList<String> watchList;
    private String quickPicks;
    public void uploadHistory(Context context)
    {
        if(UserName.getUsername(context)==null) return;
        documentReference= FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername(context));
        try {
            FileInputStream fis = context.openFileInput("History.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String str,data_to_be_uploaded="";
            while((str=br.readLine())!=null) data_to_be_uploaded = data_to_be_uploaded + str + "\n";
            documentReference.update("Watch History",data_to_be_uploaded);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addToWatchList(final Context context, final String str){
        FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername(context)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try{
                    watchList = (ArrayList<String>) documentSnapshot.get("Watchlist");
                } catch (Exception e) {
                    watchList = new ArrayList<String>();
                }
                if(watchList==null) watchList = new ArrayList<String>();
                watchList.add(str);
                FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername(context))
                        .update("Watchlist",watchList);
                UserName.setWatchlist(watchList);
            }
        });
    }
    public void removeFromWatchList(final Context context, final String str){
        FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername(context)).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                try{
                    watchList = (ArrayList<String>) documentSnapshot.get("Watchlist");
                } catch (Exception e) {
                    watchList = new ArrayList<String>();
                }
                if(watchList==null) return;
                watchList.remove(str);
                FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername(context))
                        .update("Watchlist",watchList);
                UserName.setWatchlist(watchList);
                context.sendBroadcast(new Intent("REMOVED_FROM_WATCHLIST"));
            }
        });
    }
    public void addToQuickPicks(final Context context, final String str)
    {
        if(UserName.getUsername(context)==null) return;
        quickPicks = UserName.getQuickPicks();
        if(quickPicks==null)
        {
            if(attempt==1) return;
            attempt++;
            documentReference= FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername(context));
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    quickPicks = documentSnapshot.getString("Quick Picks");
                    UserName.setQuickPicks(quickPicks);
                    addToQuickPicks(context, str);
                }
            });
            return;
        }
        String[] list = new String[5];
        String temp=str+"\n";
        int count=1;
        for(int i=1;i<=5;i++)
        {
            String name = quickPicks.substring(0,quickPicks.indexOf('\n'));
            if(!name.equals(str)&&count<5)
            {
                temp=temp+name+"\n";
                count++;
            }
            if(i<5) quickPicks=quickPicks.substring(quickPicks.indexOf('\n')+1);
        }
        quickPicks=temp;
        documentReference= FirebaseFirestore.getInstance().collection("Users").document(UserName.getUsername(context));
        documentReference.update("Quick Picks",quickPicks);
        UserName.setQuickPicks(quickPicks);
    }
}
