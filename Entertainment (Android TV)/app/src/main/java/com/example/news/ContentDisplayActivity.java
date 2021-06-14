package com.example.news;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ContentDisplayActivity extends AppCompatActivity {

    private boolean dark = false;
    private RecyclerView recyclerView;
    private EditText searchText;
    private ContentData[] contentData, data;
    private String sort[];
    private int track = 0;
    private ItemTouchHelper itemTouchHelper;
    private int position;
    private long downloadId = 0L;
    private boolean downloading;
    private int temp_position;
    private String tempName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_display);
        searchText = findViewById(R.id.search_text);
        String headline;
        if (getIntent().getStringExtra("content_type").equals("comedy")) headline = "TV show";
        else {
            headline = getIntent().getStringExtra("content_type");
            headline = headline.substring(0, 1).toUpperCase() + headline.substring(1);
        }
        if (headline.charAt(headline.length() - 1) != 's') headline = headline + "s";
        searchText.setHint("Search " + headline);
        if (getIntent().getStringExtra("content_type").equals("news")) {
            sort = new String[]{"All", "Hindi", "English", "Marathi", "Gujarati"};
            TextView textView = findViewById(R.id.sort_text);
            textView.setText("Language : " + sort[0]);
            try {
                FileInputStream fis = getApplicationContext().openFileInput("NewsLanguagePreference.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                try {
                    track = Integer.parseInt(br.readLine());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getItems();
                            TextView text = findViewById(R.id.sort_text);
                            text.setText("Language : " + sort[track]);
                        }
                    }, 500);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            sort = new String[]{"Latest", "Name"};
            TextView textView = findViewById(R.id.sort_text);
            textView.setText("Sort by : " + sort[0]);
        }
        findViewById(R.id.sort_by).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                track = track + 1;
                track = track % sort.length;
                getItems();
                TextView textView = findViewById(R.id.sort_text);
                if (getIntent().getStringExtra("content_type").equals("news"))
                    textView.setText("Language : " + sort[track]);
                else textView.setText("Sort by : " + sort[track]);
                recyclerView.scrollToPosition(0);
            }
        });
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
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = String.valueOf(charSequence).toLowerCase();
                int j = 0;
                if (contentData == null) return;
                for (int index = 0; index < contentData.length; index++) {
                    String name = contentData[index].getName().toLowerCase();
                    String tag = contentData[index].getTags().toLowerCase();
                    String date = contentData[index].getDate().toLowerCase();
                    boolean search_match = name.contains(query) || tag.contains(query) || date.contains(query);
                    if (getIntent().hasExtra("download")) {
                        if (search_match) {
                            if (new File(getApplicationContext().getExternalFilesDir(null),
                                    contentData[index].getName().replace(' ', '_').replace(':', '_') + ".mp4").exists())
                                j++;
                        }
                    } else if (search_match)
                        j++;
                }
                ContentData tempdata[] = new ContentData[j];
                j = 0;
                for (int index = 0; index < contentData.length; index++) {
                    String name = contentData[index].getName().toLowerCase();
                    String tag = contentData[index].getTags().toLowerCase();
                    String date = contentData[index].getDate().toLowerCase();
                    if (getIntent().hasExtra("download")) {
                        if (name.contains(query) || tag.contains(query) || date.contains(query)) {
                            if (new File(getApplicationContext().getExternalFilesDir(null),
                                    contentData[index].getName().replace(' ', '_').replace(':', '_') + ".mp4").exists()) {
                                tempdata[j] = contentData[index];
                                j++;
                            }
                        }
                    } else if (name.contains(query) || tag.contains(query) || date.contains(query)) {
                        tempdata[j] = contentData[index];
                        j++;
                    }
                }
                ContentAdapter contentAdapter = new ContentAdapter(tempdata, ContentDisplayActivity.this, dark);
                if(!getIntent().hasExtra("content_type")||getIntent().hasExtra("download")) contentAdapter.setAnimate(false);
                recyclerView.setAdapter(contentAdapter);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (searchText.getText().toString().length() > 0) {
                    findViewById(R.id.clear).setVisibility(View.VISIBLE);
                    findViewById(R.id.voice).setVisibility(View.GONE);
                }
                else {
                    findViewById(R.id.clear).setVisibility(View.GONE);
                    findViewById(R.id.voice).setVisibility(View.VISIBLE);
                }
            }
        });
        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (getIntent().hasExtra("dark")) {
            dark = true;
            CardView cardView = findViewById(R.id.search_card);
            cardView.setCardBackgroundColor(Color.parseColor("#363636"));
            findViewById(R.id.relative_layout).setBackgroundColor(Color.parseColor("#000000"));
            searchText.setTextColor(Color.parseColor("#FFFFFF"));
            searchText.setHintTextColor(Color.parseColor("#CCCCCC"));
        }
        if (getIntent().hasExtra("offline")||!checkInternetConnection()) {
            findViewById(R.id.sort_by).setVisibility(View.GONE);
            try {
                FileInputStream fis = getApplicationContext().openFileInput("DownloadedVideos.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String str;
                int j = 0;
                while ((str = br.readLine()) != null) {
                    if (new File(getApplicationContext().getExternalFilesDir(null),
                            str.replace(':', '_').replace(' ', '_') + ".mp4").exists()) j++;
                }
                if (j == 0) {
                    onBackPressed();
                    Toast.makeText(this, "No Downloads Found", Toast.LENGTH_SHORT).show();
                    return;
                }
                contentData = new ContentData[j];
                j = 0;
                fis.close();
                fis = getApplicationContext().openFileInput("DownloadedVideos.txt");
                br = new BufferedReader(new InputStreamReader(fis));
                while ((str = br.readLine()) != null) {
                    if (new File(getApplicationContext().getExternalFilesDir(null),
                            str.replace(':', '_').replace(' ', '_') + ".mp4").exists()) {
                        contentData[j] = new ContentData(getApplicationContext(), str,
                                "/storage/emulated/0/android/data/com.example.news/files/" + str.replace(':', '_').replace(' ', '_') + ".jpg",
                                "", "");
                        j++;
                    }
                }
                ContentAdapter contentAdapter = new ContentAdapter(contentData, ContentDisplayActivity.this, dark);
                contentAdapter.setAnimate(false);
                recyclerView.setAdapter(contentAdapter);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                final ContentAdapter adapter = (ContentAdapter) recyclerView.getAdapter();
                data = adapter.getContentData();
                position = viewHolder.getAdapterPosition();
                if (direction == ItemTouchHelper.RIGHT) {
                    if (data[position].getLink().contains("(video)") || getIntent().hasExtra("download")) {
                        Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
                        intent.putExtra("name", data[position].getName());
                        if (!getIntent().hasExtra("download"))
                            intent.putExtra("link", data[position].getLink().substring("(video)".length()));
                        intent.putExtra("online", true);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(data[position].getLink()));
                        startActivity(intent);
                    }
                    if (data[position].getDataBaseName() != null)
                        new Sync().addToQuickPicks(getApplicationContext(), data[position].getDataBaseName());
                    printHistory(data[position].getName());
                    new Sync().uploadHistory(getApplicationContext());
                }
                else if (direction == ItemTouchHelper.LEFT) {
                    final String name = data[position].getName();
                    if (getIntent().hasExtra("download")) {
                        Snackbar.make(recyclerView, "Delete " + name + "?", Snackbar.LENGTH_LONG).setAction(
                                "Delete", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        long download_id = 0L;
                                        try {
                                            FileInputStream fis = getApplicationContext().openFileInput("download_id_for" +
                                                    name.replace(':', '_').replace(' ', '_') + ".txt");
                                            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                                            try {
                                                download_id = Long.parseLong(br.readLine());
                                            } catch (NumberFormatException ne) {
                                                ne.printStackTrace();
                                                download_id = 0L;
                                            }
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        DownloadManager manager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                                        manager.remove(download_id);
                                        new File(getApplicationContext().getExternalFilesDir(null),
                                                name.replace(' ', '_').replace(':', '_') + ".mp4").delete();
                                        new File(getApplicationContext().getFilesDir(), "download_id_for" +
                                                name.replace(':', '_').replace(' ', '_') + ".txt").delete();
                                        adapter.notifyItemRemoved(position);
                                        Snackbar.make(recyclerView, "Deleted " + name, BaseTransientBottomBar.LENGTH_SHORT).show();
                                        FileInputStream fis = null;
                                        try {
                                            fis = getApplicationContext().openFileInput("isDownloading.txt");
                                            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                                            if (br.readLine().equals(name)) {
                                                new File(getApplicationContext().getFilesDir(), "isDownloading.txt").delete();
                                            }
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        if (adapter.getContentData().length == 1) {
                                            Toast.makeText(getApplicationContext(), "No Downloads", Toast.LENGTH_SHORT).show();
                                            onBackPressed();
                                            return;
                                        }
                                        EditText editText = findViewById(R.id.search_text);
                                        editText.setText(editText.getText().toString() + " ");
                                        editText.setText(editText.getText().toString().substring(0, editText.getText().toString().length() - 1));
                                    }
                                }
                        ).setActionTextColor(Color.parseColor("#FF4444")).show();
                    }
                    if (!data[position].getLink().contains("(video)") && !getIntent().hasExtra("download")) {
                        Snackbar.make(recyclerView, "Cannot download " + name, Snackbar.LENGTH_LONG).setActionTextColor(Color.parseColor("#00B0FF"))
                                .setAction("open link", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse(data[position].getLink()));
                                        startActivity(intent);
                                        printHistory(name);
                                        new Sync().uploadHistory(getApplicationContext());
                                        new Sync().addToQuickPicks(getApplicationContext(), data[position].getDataBaseName());
                                    }
                                }).show();
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemInserted(position);
                        return;
                    }
                    if (new File(getApplicationContext().getExternalFilesDir(null),
                            name.replace(' ', '_').replace(':', '_') + ".mp4").exists()) {
                        if (!getIntent().hasExtra("download")) {
                            Snackbar.make(recyclerView, name + " already exists on this device", Snackbar.LENGTH_LONG).setAction("Play", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (data[position].getLink().contains("(video)") || getIntent().hasExtra("download")) {
                                        Intent intent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
                                        intent.putExtra("name", data[position].getName());
                                        if (!getIntent().hasExtra("download"))
                                            intent.putExtra("link", data[position].getLink().substring("(video)".length()));
                                        intent.putExtra("online", true);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        printHistory(name);
                                        new Sync().uploadHistory(getApplicationContext());
                                        new Sync().addToQuickPicks(getApplicationContext(), data[position].getDataBaseName());
                                    }
                                }
                            }).setActionTextColor(Color.parseColor("#43A047")).show();
                        }
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemInserted(position);
                        return;
                    }
                    if (new File(getApplicationContext().getFilesDir(), "isDownloading.txt").exists()) {
                        String title = "";
                        try {
                            FileInputStream fis = getApplicationContext().openFileInput("isDownloading.txt");
                            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                            title = br.readLine();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        new DownloadNotification().enqueueDownload(getApplicationContext(),data[position].getName(),
                                data[position].getLink().substring("(video)".length()));
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemInserted(position);
                        return;
                    }
                    tempName = data[position].getName();
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(data[position].getLink().substring("(video)".length())));
                    request.setTitle(name);
                    request.setDescription("Entertainment");
                    request.allowScanningByMediaScanner();
                    request.setDestinationInExternalFilesDir(getApplicationContext(), "",
                            name.replace(' ', '_').replace(':', '_') + ".mp4");
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                    request.setVisibleInDownloadsUi(false);
                    final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    downloadId = Objects.requireNonNull(manager).enqueue(request);
                    Snackbar.make(recyclerView, "Downloading " + name, Snackbar.LENGTH_LONG)
                            .setActionTextColor(Color.parseColor("#FF4444"))
                            .setAction("Cancel", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    manager.remove(downloadId);
                                    new File(getApplicationContext().getFilesDir(), "isDownloading.txt").delete();
                                    new File(getApplicationContext().getFilesDir(), "download_id_for_"
                                            + tempName.replace(' ', '_').replace(':', '_') + ".txt").delete();
                                    NotificationManagerCompat.from(getApplicationContext()).cancel(18);
                                    new DownloadNotification().goToNextTask(ContentDisplayActivity.this,dark);
                                }
                            })
                            .show();
                    File file = new File(getApplicationContext().getFilesDir(), "download_id_for_"
                            + name.replace(' ', '_').replace(':', '_') + ".txt");
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        FileOutputStream fileOutputStream = getApplicationContext().openFileOutput("download_id_for_"
                                + name.replace(' ', '_').replace(':', '_') + ".txt", MODE_PRIVATE);
                        fileOutputStream.write((downloadId + "\n").getBytes());
                        fileOutputStream.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (!new File(getApplicationContext().getFilesDir(), "isDownloading.txt").exists()) {
                        try {
                            new File(getApplicationContext().getFilesDir(), "isDownloading.txt").createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        FileOutputStream fos = getApplicationContext().openFileOutput("isDownloading.txt", MODE_PRIVATE);
                        fos.write(name.getBytes());
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    new DownloadNotification().generateDownloadNotification(getApplicationContext(),ContentDisplayActivity.this,dark);
                }
                adapter.notifyItemRemoved(position);
                adapter.notifyItemInserted(position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (getIntent().hasExtra("download")) {
                    new RecyclerViewSwipeDecorator.Builder(getApplicationContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addSwipeRightBackgroundColor(Color.parseColor("#43A047"))
                            .addSwipeRightActionIcon(R.drawable.ic_baseline_play_arrow_24)
                            .addSwipeRightLabel("Watch")
                            .setSwipeRightLabelColor(Color.WHITE)
                            .setSwipeLeftLabelColor(Color.WHITE)
                            .addSwipeLeftBackgroundColor(Color.parseColor("#FF4444"))
                            .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                            .addSwipeLeftLabel("Delete")
                            .create()
                            .decorate();
                } else {
                    new RecyclerViewSwipeDecorator.Builder(getApplicationContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addSwipeRightBackgroundColor(Color.parseColor("#43A047"))
                            .addSwipeRightActionIcon(R.drawable.ic_baseline_play_arrow_24)
                            .addSwipeRightLabel("Watch")
                            .setSwipeRightLabelColor(Color.WHITE)
                            .setSwipeLeftLabelColor(Color.WHITE)
                            .addSwipeLeftBackgroundColor(Color.parseColor("#0C61C8"))
                            .addSwipeLeftActionIcon(R.drawable.ic_baseline_arrow_download_24)
                            .addSwipeLeftLabel("Download")
                            .create()
                            .decorate();
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        String type = getIntent().getStringExtra("content_type");
        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        if (type.equals("classic") || type.equals("movie") || getIntent().hasExtra("download"))
            itemTouchHelper.attachToRecyclerView(recyclerView);
        getItems();
    }

    private void getItems() {
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ContentAdapter testAdapter = (ContentAdapter) recyclerView.getAdapter();
                if (getIntent().hasExtra("download")) {
                    contentData = new ContentData[Integer.parseInt(snapshot.child("movie").getValue().toString())
                            + Integer.parseInt(snapshot.child("classic").getValue().toString())];
                    int count = 0;
                    for (int i = 1; i <= Integer.parseInt(snapshot.child("movie").getValue().toString()); i++) {
                        String tags = "";
                        try {
                            tags = snapshot.child("movietag" + i).getValue().toString();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        contentData[count] = new ContentData(getApplicationContext(), snapshot.child("movie" + i).getValue().toString(),
                                snapshot.child("movieimage" + i).getValue().toString(), snapshot.child("movielink" + i).getValue().toString(),
                                snapshot.child("moviedate" + i).getValue().toString(), tags, "movie" + i);
                        count++;
                    }
                    for (int i = 1; i <= Integer.parseInt(snapshot.child("classic").getValue().toString()); i++) {
                        String tags = "";
                        try {
                            tags = snapshot.child("classictag" + i).getValue().toString();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        contentData[count] = new ContentData(getApplicationContext(), snapshot.child("classic" + i).getValue().toString(),
                                snapshot.child("classicimage" + i).getValue().toString(), snapshot.child("classiclink" + i).getValue().toString(),
                                snapshot.child("classicdate" + i).getValue().toString(), tags, "classic" + i);
                        count++;
                    }
                    findViewById(R.id.search_text).setVisibility(View.VISIBLE);
                    findViewById(R.id.sort_by).setVisibility(View.VISIBLE);
                    contentData = sortContent(contentData, sort[track].toLowerCase());
                    count = 0;
                    for (int i = 0; i < contentData.length; i++) {
                        if (new File(getApplicationContext().getExternalFilesDir(null),
                                contentData[i].getName().replace(' ', '_').replace(':', '_') + ".mp4").exists())
                            count++;
                    }
                    if (count == 0) {
                        onBackPressed();
                        Toast.makeText(ContentDisplayActivity.this, "No Downloads Found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ContentData tempData[] = new ContentData[count];
                    count = 0;
                    for (int i = 0; i < contentData.length; i++) {
                        if (new File(getApplicationContext().getExternalFilesDir(null),
                                contentData[i].getName().replace(' ', '_').replace(':', '_') + ".mp4").exists()) {
                            tempData[count] = contentData[i];
                            count++;
                        }
                    }
                    contentData = tempData;
                    ContentAdapter adapter = new ContentAdapter(contentData, ContentDisplayActivity.this, dark);
                    if(!getIntent().hasExtra("content_type")||getIntent().hasExtra("download")) adapter.setAnimate(false);
                    if(testAdapter!=null) testAdapter.setContentData(contentData);
                    else recyclerView.setAdapter(adapter);
                } else {
                    String type = getIntent().getStringExtra("content_type");
                    contentData = new ContentData[Integer.parseInt(snapshot.child(type).getValue().toString())];
                    for (int i = 0; i < contentData.length; i++) {
                        int j = contentData.length - i;
                        String tag = "", date = "", image = snapshot.child("sports_image_link").getValue().toString();
                        try {
                            image = snapshot.child(type + "image" + j).getValue().toString();
                            date = snapshot.child(type + "date" + j).getValue().toString();
                            tag = snapshot.child(type + "tag" + j).getValue().toString();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        contentData[i] = new ContentData(getApplicationContext(), snapshot.child(type + j).getValue().toString(),
                                image, snapshot.child(type + "link" + j).getValue().toString(),
                                date, tag, type + j);
                    }
                    ContentAdapter contentAdapter = new ContentAdapter(contentData, ContentDisplayActivity.this, dark);
                    if(testAdapter!=null) testAdapter.setContentData(contentData);
                    else recyclerView.setAdapter(contentAdapter);
                    if (getIntent().getStringExtra("content_type").equals("news")) {
                        File file = new File(getApplicationContext().getFilesDir(), "NewsLanguagePreference.txt");
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            FileOutputStream fos = getApplicationContext().openFileOutput("NewsLanguagePreference.txt", MODE_PRIVATE);
                            fos.write(String.valueOf(track).getBytes());
                            fos.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (track == 0) return;
                        ContentData tempData[];
                        int j = 0;
                        for (int i = 0; i < contentData.length; i++) {
                            if (contentData[i].getDate().equals(sort[track])) j++;
                        }
                        tempData = new ContentData[j];
                        j = 0;
                        for (int i = 0; i < contentData.length; i++) {
                            if (contentData[i].getDate().equals(sort[track])) {
                                tempData[j] = contentData[i];
                                j++;
                            }
                        }
                        contentData = tempData;
                        if(testAdapter!=null) testAdapter.setContentData(contentData);
                        else recyclerView.setAdapter(new ContentAdapter(contentData, ContentDisplayActivity.this, dark));
                    } else if (!getIntent().getStringExtra("content_type").equals("news") &&
                            !getIntent().getStringExtra("content_type").equals("sport") &&
                            !getIntent().getStringExtra("content_type").equals("kids")) {
                        contentData = sortContent(contentData, sort[track].toLowerCase());
                    } else if (getIntent().getStringExtra("content_type").equals("sport") || getIntent().getStringExtra("content_type").equals("kids"))
                        findViewById(R.id.sort_by).setVisibility(View.GONE);
                }
                String query = searchText.getText().toString();
                if (query.length() == 0) findViewById(R.id.clear).setVisibility(View.GONE);
                else {
                    query = query.toLowerCase();
                    int j = 0;
                    for (int index = 0; index < contentData.length; index++) {
                        String name = contentData[index].getName().toLowerCase();
                        String tag = contentData[index].getTags().toLowerCase();
                        String date = contentData[index].getDate().toLowerCase();
                        if (name.contains(query) || tag.contains(query) || date.contains(query))
                            j++;
                    }
                    ContentData tempdata[] = new ContentData[j];
                    j = 0;
                    for (int index = 0; index < contentData.length; index++) {
                        String name = contentData[index].getName().toLowerCase();
                        String tag = contentData[index].getTags().toLowerCase();
                        String date = contentData[index].getDate().toLowerCase();
                        if (name.contains(query) || tag.contains(query) || date.contains(query)) {
                            tempdata[j] = contentData[index];
                            j++;
                        }
                    }
                    ContentAdapter contentAdapter = new ContentAdapter(tempdata,ContentDisplayActivity.this, dark);
                    if(testAdapter!=null) testAdapter.setContentData(tempdata);
                    recyclerView.setAdapter(contentAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.right_start, R.anim.right_end);
    }

    private ContentData[] sortContent(ContentData unsortedData[], String type) {
        ContentData temp_data;
        int i, j;

        for (i = unsortedData.length - 2; i >= 0; i--) {
            for (j = unsortedData.length - 2; j >= 0; j--) {
                if ((type.equals("name") && unsortedData[j].getName().compareTo(unsortedData[j + 1].getName()) > 0) ||
                        (type.equals("latest") && unsortedData[j].getDate().substring(0, 4).compareTo(unsortedData[j + 1].getDate().substring(0, 4)) < 0)) {
                    temp_data = unsortedData[j];
                    unsortedData[j] = unsortedData[j + 1];
                    unsortedData[j + 1] = temp_data;
                }
            }
        }
        return unsortedData;
    }

    private void printDate() {
        File temp_history = new File(getFilesDir(), "TempHistory.txt");
        try {
            File file = new File(getFilesDir(), "Date.txt");
            if (!file.exists()) {
                file.createNewFile();
                try {
                    FileOutputStream fileOutputStream = openFileOutput("Date.txt", MODE_PRIVATE);
                    fileOutputStream.write("00000000".getBytes());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileInputStream fis = null;
            fis = openFileInput("Date.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            Calendar calendar = Calendar.getInstance();
            if (!sb.toString().substring(0, 4).equals(String.valueOf(calendar.get(Calendar.YEAR)))) {
                try {
                    FileOutputStream fileOutputStream = openFileOutput("History.txt", MODE_PRIVATE);
                    fileOutputStream.write(("History of " + String.valueOf(calendar.get(Calendar.YEAR))).getBytes());
                    fileOutputStream.close();
                    if (!sb.toString().substring(0, 4).equals("0000")) ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!sb.toString().equals(calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.DAY_OF_MONTH))) {
                try {
                    FileOutputStream fos = openFileOutput("Date.txt", MODE_PRIVATE);
                    fos.write((calendar.get(Calendar.YEAR) + "" + (calendar.get(Calendar.MONTH) + 1) + "" + calendar.get(Calendar.DAY_OF_MONTH)).getBytes());
                    fos.close();
                    File file_history = new File(getFilesDir(), "History.txt");
                    fos = openFileOutput("History.txt", MODE_APPEND);
                    fos.write(("\n" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (1 + calendar.get(Calendar.MONTH)) + "\n" + "\n").getBytes());
                    fos = openFileOutput("TempHistory.txt", MODE_APPEND);
                    fos.write(("\n" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (1 + calendar.get(Calendar.MONTH)) + "\n" + "\n").getBytes());
                    try {
                        FileOutputStream fileOutputStream = openFileOutput("Backup.txt", MODE_PRIVATE);
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

    public void printHistory(String str) {
        try {
            printDate();
            FileOutputStream fos = openFileOutput("History.txt", MODE_APPEND);
            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.HOUR_OF_DAY) < 10) fos.write("0".getBytes());
            fos.write((calendar.get(Calendar.HOUR_OF_DAY) + ":").getBytes());
            if (calendar.get(Calendar.MINUTE) < 10) fos.write("0".getBytes());
            fos.write((calendar.get(Calendar.MINUTE) + "\t\t" + str + "\n").getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (getIntent().hasExtra("offline")||!checkInternetConnection()) {
            findViewById(R.id.sort_by).setVisibility(View.GONE);
            try {
                FileInputStream fis = getApplicationContext().openFileInput("DownloadedVideos.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String str;
                int j = 0;
                while ((str = br.readLine()) != null) {
                    if (new File(getApplicationContext().getExternalFilesDir(null),
                            str.replace(':', '_').replace(' ', '_') + ".mp4").exists()) j++;
                }
                if (j == 0) {
                    onBackPressed();
                    Toast.makeText(this, "No Downloads Found", Toast.LENGTH_SHORT).show();
                    return;
                }
                contentData = new ContentData[j];
                j = 0;
                fis.close();
                fis = getApplicationContext().openFileInput("DownloadedVideos.txt");
                br = new BufferedReader(new InputStreamReader(fis));
                while ((str = br.readLine()) != null) {
                    if (new File(getApplicationContext().getExternalFilesDir(null),
                            str.replace(':', '_').replace(' ', '_') + ".mp4").exists()) {
                        contentData[j] = new ContentData(getApplicationContext(), str,
                                "/storage/emulated/0/android/data/com.example.news/files/" + str.replace(':', '_').replace(' ', '_') + ".jpg",
                                "", "");
                        j++;
                    }
                }
                ContentAdapter contentAdapter = new ContentAdapter(contentData, ContentDisplayActivity.this, dark);
                contentAdapter.setAnimate(false);
                recyclerView.setAdapter(contentAdapter);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else getItems();
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
    public boolean checkInternetConnection()
    {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetwork()==null)
        {
            return false;
        }
        return true;
    }

}