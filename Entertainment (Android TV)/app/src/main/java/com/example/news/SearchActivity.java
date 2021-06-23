package com.example.news;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private String search_query;
    private boolean show=false;
    private SearchView searchView;
    private EditText searchText;
    private RecyclerView recyclerView;
    private DatabaseReference reference;
    private String name[];
    private String link[];
    private String image[];
    private String date[];
    private String tag[];
    private String dbName[];
    private ContentData[] contentData;
    private ContentAdapter contentAdapter;
    private boolean dark = false;
    int tap = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        search_query = new String();
        searchText = findViewById(R.id.search_text);
        if(getIntent().hasExtra("dark")) dark = true;
        search_query="";
        searchView = findViewById(R.id.search);
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        if(getIntent().hasExtra("dark") || isInDarkMode())
        {
            RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.search_layout);
            relativeLayout.setBackgroundColor(Color.parseColor("#000000"));
            TextView textView = (TextView) findViewById(R.id.result);
            textView.setTextColor(Color.parseColor("#CCCCCC"));
            ((CardView)findViewById(R.id.search_card)).setCardBackgroundColor(Color.parseColor("#363636"));
            ((EditText)findViewById(R.id.search_text)).setTextColor(Color.WHITE);
            ((EditText)findViewById(R.id.search_text)).setHintTextColor(Color.parseColor("#CCCCCC"));
            dark = true;
        }
        if(getIntent().hasExtra("search"))
        {
            findViewById(R.id.search).setVisibility(View.GONE);
            findViewById(R.id.filter).setVisibility(View.GONE);
            findViewById(R.id.result).setVisibility(View.GONE);
            searchSpecific(getIntent().getStringExtra("search"));
            return;
        }
        if(getIntent().hasExtra("download"))
        {
            findViewById(R.id.filter).setVisibility(View.GONE);
            getItems();
            return;
        }
        if(getIntent().hasExtra("filter"))
        {
            Bundle bundle = getIntent().getExtras();
            if(getIntent().getStringExtra("filter").toLowerCase().equals("tv shows"))
            {
                applyFilter("comedy");
            }
            else applyFilter(bundle.getString("filter").toLowerCase());
        }
        else
        {
            final CardView cardView = (CardView) findViewById(R.id.filter);
            cardView.setVisibility(View.GONE);
            getItems();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
                    cardView.setVisibility(View.VISIBLE);
                    cardView.startAnimation(animation);
                }
            }, 1000);
        }
        findViewById(R.id.search_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchText.hasFocus()) searchText.clearFocus();
                else {
                    searchText.requestFocus();
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(searchText, 0);
                }
            }
        });
        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText.setText("");
                findViewById(R.id.clear).setVisibility(View.GONE);
            }
        });
        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (!hasFocus) inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                else inputMethodManager.showSoftInput(view,0);
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                searchText.requestFocus();
            }
        },720);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i0, final int i1, final int i2) {
                String searchQuery = charSequence.toString();
                if(searchQuery.length()==0){
                    findViewById(R.id.clear).setVisibility(View.GONE);
                    findViewById(R.id.voice).setVisibility(View.VISIBLE);
                }
                else{
                    findViewById(R.id.voice).setVisibility(View.GONE);
                    findViewById(R.id.clear).setVisibility(View.VISIBLE);
                }
                String newText = searchQuery;
                int i,j;
                j = 0;
                if(name==null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onTextChanged(charSequence,i0,i1,i2);
                        }
                    },1000);
                    return;
                }
                try {
                    for (i = 0; i <= name.length - 1; i++) {
                        if (name[i].toLowerCase().contains(newText.toLowerCase()) || date[i].toLowerCase().contains(newText.toLowerCase()) ||
                                tag[i].toLowerCase().contains(newText.toLowerCase())) j++;
                    }
                } catch (Exception e) {
                    newText = "";
                }
                ContentData tempData[] = new ContentData[j];
                j = 0;
                for (i = name.length -1; i >= 0; i--) {
                    if (name[i].toLowerCase().contains(newText.toLowerCase()) || date[i].toLowerCase().contains(newText.toLowerCase()) || tag[i].toLowerCase().contains(newText.toLowerCase())) {
                        tempData[j] = new ContentData(getApplicationContext(),name[i], image[i], link[i], date[i],"",dbName[i]);
                        j++;
                    }
                }
                contentAdapter = new ContentAdapter(tempData, SearchActivity.this, dark);
                recyclerView.setAdapter(contentAdapter);
                TextView textView = (TextView) findViewById(R.id.result);
                textView.setText(tempData.length + " results");
                if (tempData.length == 1) textView.setText("1 result");
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void applyFilter(final String filter)
    {
        final ArrayList<String> list = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                int count=0;
                int i;
                link = new String[Integer.parseInt(snapshot.child(filter.toLowerCase()).getValue().toString())];
                name = new String[Integer.parseInt(snapshot.child(filter.toLowerCase()).getValue().toString())];
                image = new String[Integer.parseInt(snapshot.child(filter.toLowerCase()).getValue().toString())];
                date = new String[Integer.parseInt(snapshot.child(filter.toLowerCase()).getValue().toString())];
                tag = new String[Integer.parseInt(snapshot.child(filter.toLowerCase()).getValue().toString())];
                dbName = new String[Integer.parseInt(snapshot.child(filter.toLowerCase()).getValue().toString())];

                for(i=0;i<link.length;i++)
                {
                    name[i]=snapshot.child(filter.toLowerCase()+(i+1)).getValue().toString();
                    dbName[i] = filter.toLowerCase()+(i+1);
                    link[i]=snapshot.child(filter.toLowerCase()+"link"+(i+1)).getValue().toString();
                    if(filter.toLowerCase().equals("sport")||filter.toLowerCase().equals("kids"))
                        date[i] = filter.toLowerCase();
                    else date[i] = snapshot.child(filter.toLowerCase()+"date"+(i+1)).getValue().toString();
                    if(filter.equals("sport")) image[i] = snapshot.child("sports_image_link").getValue().toString();
                    else image[i] = snapshot.child(filter.toLowerCase()+"image"+(i+1)).getValue().toString();
                    try {
                        tag[count] = snapshot.child(filter.toLowerCase()+"tag"+(i+1)).getValue().toString();
                    }
                    catch (Exception e)
                    {
                        tag[count] = snapshot.child(filter.toLowerCase()+(i+1)).getValue().toString();
                    }
                    count++;
                }
                contentData = new ContentData[name.length];
                for(i=0;i<name.length;i++)
                {
                    contentData[name.length-i-1] = new ContentData(getApplicationContext(),name[i],image[i],link[i],date[i],"",dbName[i]);
                }
                contentAdapter = new ContentAdapter(contentData,SearchActivity.this,dark);
                recyclerView.setAdapter(contentAdapter);
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        String newText;
                        int i,j=0;
                        try {
                            newText = searchText.getText().toString();
                            for (i = 0; i <= name.length - 1; i++) {
                                if (name[i].toLowerCase().contains(newText.toLowerCase()) || date[i].toLowerCase().contains(newText.toLowerCase()) ||
                                        tag[i].toLowerCase().contains(newText.toLowerCase())) j++;
                            }
                        } catch (Exception e) {
                            newText = "";
                        }
                        ContentData tempData[] = new ContentData[j];
                        j = 0;
                        for (i = name.length -1; i >= 0; i--) {
                            if (name[i].toLowerCase().contains(newText.toLowerCase()) || date[i].toLowerCase().contains(newText.toLowerCase()) || tag[i].toLowerCase().contains(newText.toLowerCase())) {
                                tempData[j] = new ContentData(getApplicationContext(),name[i], image[i], link[i], date[i],"",dbName[i]);
                                j++;
                            }
                        }
                        contentAdapter = new ContentAdapter(tempData, SearchActivity.this, dark);
                        recyclerView.setAdapter(contentAdapter);
                        TextView textView = (TextView) findViewById(R.id.result);
                        textView.setText(tempData.length + " results");
                        if (tempData.length == 1) textView.setText("1 result");
                    }
                },500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        TextView textView = findViewById(R.id.filter_text);
        if(filter.toLowerCase().equals("comedy")) textView.setText("TV SHOWS");
        else textView.setText(filter.toUpperCase());
    }
    private void getItems()
    {
        final ArrayList<String> list = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                int i;
                int count = 0;
                dbName = new String[Integer.parseInt(snapshot.child("comedy").getValue().toString())
                        + Integer.parseInt(snapshot.child("sport").getValue().toString())
                        + Integer.parseInt(snapshot.child("movie").getValue().toString())
                        + Integer.parseInt(snapshot.child("classic").getValue().toString())
                        + Integer.parseInt(snapshot.child("kids").getValue().toString())
                        + Integer.parseInt(snapshot.child("news").getValue().toString())];
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
                for (i = Integer.parseInt(snapshot.child("comedy").getValue().toString()); i >= 1; i--) {
                    name[count] = snapshot.child("comedy" + i).getValue().toString();
                    link[count] = snapshot.child("comedylink" + i).getValue().toString();
                    date[count] = "TV show : " + snapshot.child("comedydate" + i).getValue().toString();
                    image[count] = snapshot.child("comedyimage" + i).getValue().toString();
                    dbName[count] = "comedy"+i;
                    try {
                        tag[count] = snapshot.child("comedytag" + i).getValue().toString();
                    } catch (Exception e) {
                        tag[count] = snapshot.child("comedy" + i).getValue().toString();
                    }
                    count++;
                }
                for (i = Integer.parseInt(snapshot.child("sport").getValue().toString()); i >= 1; i--) {
                    name[count] = snapshot.child("sport" + i).getValue().toString();
                    link[count] = snapshot.child("sportlink" + i).getValue().toString();
                    date[count] = "Sport";
                    dbName[count] = "sport"+i;
                    try {
                        tag[count] = snapshot.child("sporttag" + i).getValue().toString();
                    } catch (Exception e) {
                        tag[count] = snapshot.child("sport" + i).getValue().toString();
                    }
                    image[count] = snapshot.child("sports_image_link").getValue().toString();
                    count++;
                }
                for (i = Integer.parseInt(snapshot.child("kids").getValue().toString()); i >= 1; i--) {
                    name[count] = snapshot.child("kids" + i).getValue().toString();
                    link[count] = snapshot.child("kidslink" + i).getValue().toString();
                    date[count] = "Kids";
                    dbName[count] = "kids"+i;
                    image[count] = snapshot.child("kidsimage" + i).getValue().toString();
                    try {
                        tag[count] = snapshot.child("kidstag" + i).getValue().toString();
                    } catch (Exception e) {
                        tag[count] = snapshot.child("kids" + i).getValue().toString();
                    }
                    count++;
                }
                for (i = Integer.parseInt(snapshot.child("news").getValue().toString()); i >= 1; i--) {
                    name[count] = snapshot.child("news" + i).getValue().toString();
                    link[count] = snapshot.child("newslink" + i).getValue().toString();
                    date[count] = "News : " + snapshot.child("newsdate" + i).getValue().toString();
                    image[count] = snapshot.child("newsimage" + i).getValue().toString();
                    dbName[count] = "news"+i;
                    try {
                        tag[count] = snapshot.child("newstag" + i).getValue().toString();
                    } catch (Exception e) {
                        tag[count] = snapshot.child("news" + i).getValue().toString();
                    }
                    count++;
                }
                for (i = Integer.parseInt(snapshot.child("classic").getValue().toString()); i >= 1; i--) {
                    name[count] = snapshot.child("classic" + i).getValue().toString();
                    link[count] = snapshot.child("classiclink" + i).getValue().toString();
                    date[count] = "Classic : " + snapshot.child("classicdate" + i).getValue().toString();
                    dbName[count] = "classic"+i;
                    image[count] = snapshot.child("classicimage" + i).getValue().toString();
                    try {
                        tag[count] = snapshot.child("classictag" + i).getValue().toString();
                    } catch (Exception e) {
                        tag[count] = snapshot.child("classic" + i).getValue().toString();
                    }
                    count++;
                }
                for (i = Integer.parseInt(snapshot.child("movie").getValue().toString()); i >= 1; i--) {
                    name[count] = snapshot.child("movie" + i).getValue().toString();
                    link[count] = snapshot.child("movielink" + i).getValue().toString();
                    date[count] = "Movie : " + snapshot.child("moviedate" + i).getValue().toString();
                    image[count] = snapshot.child("movieimage" + i).getValue().toString();
                    dbName[count] = "movie"+i;
                    try {
                        tag[count] = snapshot.child("movietag" + i).getValue().toString();
                    } catch (Exception e) {
                        tag[count] = snapshot.child("movie" + i).getValue().toString();
                    }
                    count++;
                }
                contentData = new ContentData[name.length];
                for (i = 0; i < name.length; i++) {
                    contentData[i] = new ContentData(getApplicationContext(),name[i], image[i], link[i], date[i],"",dbName[i]);
                }
                contentAdapter = new ContentAdapter(contentData, SearchActivity.this, dark);
                recyclerView.setAdapter(contentAdapter);
                if (getIntent().hasExtra("download")) {
                    i = Integer.parseInt(snapshot.child("movie").getValue().toString()) + Integer.parseInt(snapshot.child("classic").getValue().toString());
                    contentData = new ContentData[i];
                    tag=new String[i];
                    int j = 0;
                    i = Integer.parseInt(snapshot.child("movie").getValue().toString());
                    while (i > 0) {
                        contentData[j] = new ContentData(getApplicationContext(),snapshot.child("movie" + i).getValue().toString(),
                                snapshot.child("movieimage" + i).getValue().toString(), snapshot.child("movielink" + i).getValue().toString(),
                                snapshot.child("moviedate" + i).getValue().toString());
                        try {
                            tag[j] = snapshot.child("movietag" + i).getValue().toString();
                        }
                        catch (Exception e)
                        {
                            tag[j]="";
                        }
                        i--;
                        j++;
                    }
                    i = Integer.parseInt(snapshot.child("classic").getValue().toString());
                    while (i > 0) {
                        contentData[j] = new ContentData(getApplicationContext(),snapshot.child("classic" + i).getValue().toString(),
                                snapshot.child("classicimage" + i).getValue().toString(), snapshot.child("classiclink" + i).getValue().toString(),
                                snapshot.child("classicdate" + i).getValue().toString());
                        try {
                            tag[j] = snapshot.child("classictag" + i).getValue().toString();
                        }
                        catch (Exception e)
                        {
                            tag[j]="";
                        }
                        i--;
                        j++;
                    }
                    ContentData tempData[];
                    String temp[];
                    j = 0;
                    for (i = 0; i < contentData.length; i++) {
                        if (new File(getApplicationContext().getExternalFilesDir(null),
                                contentData[i].getName().replace(' ', '_').replace(':', '_') + ".mp4").exists())
                            j++;
                    }
                    if (j == 0) {
                        Toast.makeText(getApplicationContext(), "No downloads found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    tempData = new ContentData[j];
                    temp=new String[j];
                    j = 0;
                    for (i = 0; i < contentData.length; i++) {
                        if (new File(getApplicationContext().getExternalFilesDir(null),
                                contentData[i].getName().replace(' ', '_').replace(':', '_') + ".mp4").exists()) {
                            tempData[j] = contentData[i];
                            temp[j]=tag[i];
                            j++;
                        }
                    }
                    contentData = tempData;
                    recyclerView.setAdapter(new ContentAdapter(contentData, SearchActivity.this, dark));
                    name = new String[contentData.length];
                    date = new String[contentData.length];
                    image = new String[contentData.length];
                    link = new String[contentData.length];
                    tag=temp;
                    for (i = 0; i < contentData.length; i++) {
                        name[i] = contentData[i].getName();
                        date[i] = contentData[i].getDate();
                        image[i] = contentData[i].getImage();
                        link[i]= contentData[i].getLink();
                    }
                }
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        String newText;
                        int i,j=0;
                        try {
                            newText = searchText.getText().toString();
                            for (i = 0; i <= name.length - 1; i++) {
                                if (name[i].toLowerCase().contains(newText.toLowerCase()) || date[i].toLowerCase().contains(newText.toLowerCase()) ||
                                        tag[i].toLowerCase().contains(newText.toLowerCase())) j++;
                            }
                        } catch (Exception e) {
                            newText = "";
                        }
                        ContentData tempData[] = new ContentData[j];
                        j = 0;
                        for (i = name.length -1; i >= 0; i--) {
                            if (name[i].toLowerCase().contains(newText.toLowerCase()) || date[i].toLowerCase().contains(newText.toLowerCase()) || tag[i].toLowerCase().contains(newText.toLowerCase())) {
                                tempData[j] = new ContentData(getApplicationContext(),name[i], image[i], link[i], date[i],"",dbName[i]);
                                j++;
                            }
                        }
                        contentAdapter = new ContentAdapter(tempData, SearchActivity.this, dark);
                        recyclerView.setAdapter(contentAdapter);
                        TextView textView = (TextView) findViewById(R.id.result);
                        textView.setText(tempData.length + " results");
                        if (tempData.length == 1) textView.setText("1 result");
                    }
                },500);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(findViewById(R.id.options).getVisibility()==View.VISIBLE)
        {
            findViewById(R.id.filter).callOnClick();
            return;
        }
        super.onBackPressed();
        if(getIntent().hasExtra("filter")) overridePendingTransition(R.anim.fade_in,R.anim.zoom_out);
        else overridePendingTransition(R.anim.right_start,R.anim.right_end);
    }

    public void showFilters(final View view)
    {
        Animation start = AnimationUtils.loadAnimation(this,R.anim.zoom_in);
        Animation end = AnimationUtils.loadAnimation(this,R.anim.zoom_out);
        GridLayout gridLayout = (GridLayout) findViewById(R.id.options);
        recyclerView.setVisibility(View.GONE);
        if(!show) {
            View focus = this.getCurrentFocus();
            if(focus!=null&&tap==0)
            {
                tap=1;
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showFilters(view);
                    }
                },300);
                return;
            }
            //searchView.setVisibility(View.GONE);
            gridLayout.setVisibility(View.VISIBLE);
            gridLayout.startAnimation(start);
            show=true;
            tap=0;
        }
        else
        {
            gridLayout.setVisibility(View.GONE);
            gridLayout.startAnimation(end);
            show=false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    recyclerView.setVisibility(View.VISIBLE);
                    //searchView.setVisibility(View.VISIBLE);
                }
            }, 300);
        }
    }

    public void filterSearchList(View view) {
        GridLayout gridLayout = (GridLayout) findViewById(R.id.options);
        String filter_from_card="";
        switch (view.getId()){
            case R.id.all:{
                getItems();
                break;
            }
            case R.id.comedy:{
                applyFilter("comedy");
                break;
            }
            case R.id.movie:{
                applyFilter("movie");
                break;
            }
            case R.id.classic:{
                applyFilter("classic");
                break;
            }
            case R.id.kids:{
                applyFilter("kids");
                break;
            }
            case R.id.sport:{
                applyFilter("sport");
                break;
            }
            case R.id.news:{
                applyFilter("news");
                break;
            }
        }
        gridLayout.setVisibility(View.GONE);
        Animation end = AnimationUtils.loadAnimation(this,R.anim.zoom_out);
        gridLayout.startAnimation(end);
        show=false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.setVisibility(View.VISIBLE);

            }
        }, 300);
    }
    private void searchSpecific(final String str)
    {
        findViewById(R.id.search_card).setVisibility(View.GONE);
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i;
                int count=0;
                if(getIntent().hasExtra("signed_in")) {
                    link = new String[Integer.parseInt(snapshot.child("comedy").getValue().toString())
                            + Integer.parseInt(snapshot.child("sport").getValue().toString())
                            + Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())
                            + Integer.parseInt(snapshot.child("kids").getValue().toString())
                            + Integer.parseInt(snapshot.child("news").getValue().toString())];
                    dbName = new String[Integer.parseInt(snapshot.child("comedy").getValue().toString())
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
                    for (i=Integer.parseInt(snapshot.child("comedy").getValue().toString());i>=1;i--) {
                        name[count] = snapshot.child("comedy" + i).getValue().toString();
                        link[count] = snapshot.child("comedylink" + i).getValue().toString();
                        date[count] = "TV show : "+snapshot.child("comedydate"+i).getValue().toString();
                        dbName[count]="comedy"+i;
                        image[count] = snapshot.child("comedyimage"+i).getValue().toString();
                        try {
                            tag[count] = snapshot.child("comedytag"+i).getValue().toString();
                        }
                        catch (Exception e)
                        {
                            tag[count] = snapshot.child("comedy"+i).getValue().toString();
                        }
                        count++;
                    }
                    for (i=Integer.parseInt(snapshot.child("movie").getValue().toString());i>=1;i--) {
                        name[count] = snapshot.child("movie" + i).getValue().toString();
                        link[count] = snapshot.child("movielink" + i).getValue().toString();
                        date[count] = "Movie : "+snapshot.child("moviedate"+i).getValue().toString();
                        image[count] = snapshot.child("movieimage"+i).getValue().toString();
                        dbName[count]="movie"+i;
                        try {
                            tag[count] = snapshot.child("movietag"+i).getValue().toString();
                        }
                        catch (Exception e)
                        {
                            tag[count] = snapshot.child("movie"+i).getValue().toString();
                        }
                        count++;
                    }
                    for (i=Integer.parseInt(snapshot.child("sport").getValue().toString());i>=1;i--) {
                        name[count] = snapshot.child("sport" + i).getValue().toString();
                        link[count] = snapshot.child("sportlink" + i).getValue().toString();
                        date[count] = "Sport";
                        dbName[count]="sport"+i;
                        try {
                            tag[count] = snapshot.child("sporttag"+i).getValue().toString();
                        }
                        catch (Exception e)
                        {
                            tag[count] = snapshot.child("sport"+i).getValue().toString();
                        }
                        image[count] = snapshot.child("sports_image_link").getValue().toString();
                        count++;
                    }
                    for (i=Integer.parseInt(snapshot.child("kids").getValue().toString());i>=1;i--) {
                        name[count] = snapshot.child("kids" + i).getValue().toString();
                        link[count] = snapshot.child("kidslink" + i).getValue().toString();
                        date[count] = "Kids";
                        dbName[count]="kids"+i;
                        image[count] = snapshot.child("kidsimage"+i).getValue().toString();
                        try {
                            tag[count] = snapshot.child("kidstag"+i).getValue().toString();
                        }
                        catch (Exception e)
                        {
                            tag[count] = snapshot.child("kids"+i).getValue().toString();
                        }
                        count++;
                    }
                }
                else
                {
                    link = new String[Integer.parseInt(snapshot.child("news").getValue().toString())
                            +Integer.parseInt(snapshot.child("classic").getValue().toString())];
                    name = new String[Integer.parseInt(snapshot.child("news").getValue().toString())
                            +Integer.parseInt(snapshot.child("classic").getValue().toString())];
                    date = new String[Integer.parseInt(snapshot.child("news").getValue().toString())
                            +Integer.parseInt(snapshot.child("classic").getValue().toString())];
                    image = new String[Integer.parseInt(snapshot.child("news").getValue().toString())
                            +Integer.parseInt(snapshot.child("classic").getValue().toString())];
                    tag = new String[Integer.parseInt(snapshot.child("news").getValue().toString())
                            +Integer.parseInt(snapshot.child("classic").getValue().toString())];
                }
                for (i=Integer.parseInt(snapshot.child("classic").getValue().toString());i>=1;i--) {
                    name[count] = snapshot.child("classic" + i).getValue().toString();
                    link[count] = snapshot.child("classiclink" + i).getValue().toString();
                    date[count] = "Classic : "+snapshot.child("classicdate"+i).getValue().toString();
                    dbName[count]="classic"+i;
                    image[count] = snapshot.child("classicimage"+i).getValue().toString();
                    try {
                        tag[count] = snapshot.child("classictag"+i).getValue().toString();
                    }
                    catch (Exception e)
                    {
                        tag[count] = snapshot.child("classic"+i).getValue().toString();
                    }
                    count++;
                }
                for (i=Integer.parseInt(snapshot.child("news").getValue().toString());i>=1;i--) {
                    name[count] = snapshot.child("news" + i).getValue().toString();
                    link[count] = snapshot.child("newslink" + i).getValue().toString();
                    date[count] = "News : "+snapshot.child("newsdate"+i).getValue().toString();
                    dbName[count]="news"+i;
                    image[count] = snapshot.child("newsimage"+i).getValue().toString();
                    try {
                        tag[count] = snapshot.child("newstag"+i).getValue().toString();
                    }
                    catch (Exception e)
                    {
                        tag[count] = snapshot.child("news"+i).getValue().toString();
                    }
                    count++;
                }
                contentData = new ContentData[name.length];
                for(i=0;i<name.length;i++)
                {
                    contentData[i]=new ContentData(getApplicationContext(),name[i],image[i],link[i],date[i],"",dbName[i]);
                }
                contentAdapter = new ContentAdapter(contentData,SearchActivity.this,dark);
                recyclerView.setAdapter(contentAdapter);
                int j=0;
                for (i = 0; i <= name.length - 1; i++) {
                    if (name[i].toLowerCase().contains(str.toLowerCase())||date[i].toLowerCase().contains(str.toLowerCase())||
                            tag[i].toLowerCase().contains(str.toLowerCase())) j++;
                }
                ContentData tempData[] = new ContentData[j];
                j=0;
                for(i=0;i<name.length;i++)
                {
                    if (name[i].toLowerCase().contains(str.toLowerCase())||date[i].toLowerCase().contains(str.toLowerCase())||
                            tag[i].toLowerCase().contains(str.toLowerCase()))
                    {
                        tempData[j] = new ContentData(getApplicationContext(),name[i],image[i],link[i],date[i],"",dbName[i]);
                        j++;
                    }
                }
                contentAdapter = new ContentAdapter(tempData,SearchActivity.this,dark);
                recyclerView.setAdapter(contentAdapter);
                TextView textView = (TextView) findViewById(R.id.result);
                textView.setText(tempData.length+" results");
                if(getIntent().hasExtra("user")) UserName.setUsername(getIntent().getStringExtra("user"));
                if(tempData.length==1||getIntent().hasExtra("open"))
                {
                    if(getIntent().hasExtra("open")&&tempData.length>1)
                    {
                        for(i=0;i<tempData.length;i++)
                        {
                            if(tempData[i].getName().equals(getIntent().getStringExtra("search")))
                            {
                                tempData[0]=tempData[i];
                                break;
                            }
                        }
                    }
                    textView.setText("1 result");
                    Intent intent;
                    if(tempData[0].getLink().contains("(video)"))
                    {
                        intent=new Intent(getApplicationContext(),DescriptionActivity.class);
                        intent.putExtra("link",tempData[0].getLink().substring("(video)".length()));
                        intent.putExtra("name",tempData[0].getName());
                        intent.putExtra("image",tempData[0].getImage());
                        intent.putExtra("description",tempData[0].getDate());
                        intent.putExtra("add_to_quick_picks",tempData[0].getDataBaseName());
                        if(dark) intent.putExtra("dark",true);
                        startActivity(intent);
                        finish();
                    }
                    else if(tempData[0].getLink().equals("webseries")){
                        intent = new Intent(getApplicationContext(),SeasonPickerActivity.class);
                        intent.putExtra("name",tempData[0].getName());
                        intent.putExtra("dbName",tempData[0].getDataBaseName());
                        intent.putExtra("image",tempData[0].getImage());
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        printHistory(tempData[0].getName());
                        new Sync().uploadHistory(getApplicationContext());
                        new Sync().addToQuickPicks(getApplicationContext(),tempData[0].getDataBaseName());
                        startActivity(intent);
                    }
                    else if(!getIntent().hasExtra("personal"))
                    {
                        intent = new Intent(getApplicationContext(), WebActivity.class);
                        intent.putExtra("link", tempData[0].getLink());
                        printHistory(tempData[0].getName());
                        new Sync().uploadHistory(getApplicationContext());
                        new Sync().addToQuickPicks(getApplicationContext(),tempData[0].getDataBaseName());
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    public void printHistory(String str)
    {
        try {
            printDate();
            FileOutputStream fos = openFileOutput("History.txt", MODE_APPEND);
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
    public void voiceSearch(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent,108);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 108){
            if(data==null) return;
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            ((EditText)findViewById(R.id.search_text)).setText(results.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private boolean isInDarkMode() {
        try{
            FileInputStream fis = openFileInput("Theme.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            return br.readLine().equals("true");
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}