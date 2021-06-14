package com.example.news;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddContentActivity extends AppCompatActivity {

    DatabaseReference reference;
    private String name="";
    private String value="";
    private String content_type;
    private int count;
    private String findName;
    private String findValue;
    private int i=-1;
    private String latest[];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_content);
        if(getIntent().hasExtra("dark")) setDarkTheme();
        findViewById(R.id.find_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findName();
            }
        });
        findViewById(R.id.find_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findValue();
            }
        });
    }
    private void setDarkTheme()
    {
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.add_content_layout);
        relativeLayout.setBackgroundColor(Color.parseColor("#000000"));
        EditText editText = (EditText) findViewById(R.id.name);
        editText.setHintTextColor(Color.parseColor("#CCCCCC"));
        editText.setTextColor(Color.parseColor("#CCCCCC"));
        editText = (EditText) findViewById(R.id.value);
        editText.setHintTextColor(Color.parseColor("#CCCCCC"));
        editText.setTextColor(Color.parseColor("#CCCCCC"));
        editText = (EditText) findViewById(R.id.image);
        editText.setHintTextColor(Color.parseColor("#CCCCCC"));
        editText.setTextColor(Color.parseColor("#CCCCCC"));
        editText = (EditText) findViewById(R.id.info);
        editText.setHintTextColor(Color.parseColor("#CCCCCC"));
        editText.setTextColor(Color.parseColor("#CCCCCC"));
        Button button = (Button) findViewById(R.id.submit);
        button.setBackgroundColor(Color.parseColor("#363636"));
        button.setTextColor(Color.WHITE);
    }
    public void submit(View v)
    {
        reference = FirebaseDatabase.getInstance().getReference();
        final EditText image = (EditText) findViewById(R.id.image);
        final EditText entity_name = (EditText) findViewById(R.id.name);
        final EditText entity_value = (EditText) findViewById(R.id.value);
        value = entity_value.getText().toString();
        final EditText info = (EditText) findViewById(R.id.info);
        String image_link;
        String info_data;
        name=entity_name.getText().toString();
        if(name.trim().length()==0||value.trim().length()==0)
        {
            Toast.makeText(this,"Invalid data",Toast.LENGTH_SHORT).show();
            onBackPressed();
            return;
        }
        if(content_type.equals("other")) {
            reference.child(name).setValue(value);
            Toast.makeText(this, name + " set to " + value, Toast.LENGTH_SHORT).show();
        }
        else
        {
            image_link=image.getText().toString();
            info_data=info.getText().toString();
            if(content_type.equals("sport"))
            {
                reference.child(content_type+(count+1)).setValue(name);
                reference.child(content_type+"link"+(count+1)).setValue(value);
                count++;
                reference.child(content_type).setValue(count);
            }
            else if(content_type.equals("kids"))
            {
                if(image_link.trim().length()==0)
                {
                    Toast.makeText(getApplicationContext(),"Invalid data",Toast.LENGTH_SHORT).show();
                    onBackPressed();
                    return;
                }
                reference.child(content_type+(count+1)).setValue(name);
                reference.child(content_type+"link"+(count+1)).setValue(value);
                reference.child(content_type+"image"+(count+1)).setValue(image_link);
                count++;
                reference.child(content_type).setValue(count);
            }
            else
            {
                if(image_link.trim().length()==0||info_data.trim().length()==0)
                {
                    Toast.makeText(getApplicationContext(),"Invalid data",Toast.LENGTH_SHORT).show();
                    onBackPressed();
                    return;
                }
                reference.child(content_type+(count+1)).setValue(name);
                reference.child(content_type+"link"+(count+1)).setValue(value);
                reference.child(content_type+"image"+(count+1)).setValue(image_link);
                reference.child(content_type+"date"+(count+1)).setValue(info_data);
                count++;
                reference.child(content_type).setValue(count);
            }
            for(i=4;i>=1;i--)
            {
                latest[i]=latest[i-1];
            }
            latest[0]=content_type+count;
            for(i=0;i<=4;i++)
            {
                reference.child("latest"+(i+1)).setValue(latest[i]);
            }
            Toast.makeText(getApplicationContext(),"Content Added",Toast.LENGTH_SHORT).show();
        }
        onBackPressed();
    }
    public void selectContent(View v)
    {
        if(v==findViewById(R.id.news)) content_type="news";
        else if(v==findViewById(R.id.classics)) content_type="classic";
        else if(v==findViewById(R.id.movies)) content_type="movie";
        else if(v==findViewById(R.id.sports)) content_type="sport";
        else if(v==findViewById(R.id.tv_shows)) content_type="comedy";
        else if(v==findViewById(R.id.kids)) content_type="kids";
        else content_type = "other";
        findViewById(R.id.submit).setVisibility(View.VISIBLE);
        findViewById(R.id.name).setVisibility(View.VISIBLE);
        findViewById(R.id.value).setVisibility(View.VISIBLE);
        if(!content_type.equals("sport")&&!content_type.equals("other")) findViewById(R.id.image).setVisibility(View.VISIBLE);
        if(!content_type.equals("sport")&&!content_type.equals("kids")
        &&!content_type.equals("other")) findViewById(R.id.info).setVisibility(View.VISIBLE);
        TextView textView = (TextView) findViewById(R.id.add_title);
        textView.setText(content_type.toUpperCase());
        textView.setVisibility(View.VISIBLE);
        if(!content_type.equals("other")) {
            findViewById(R.id.find_name).setVisibility(View.GONE);
            findViewById(R.id.find_value).setVisibility(View.GONE);
            reference = FirebaseDatabase.getInstance().getReference();
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    count = Integer.parseInt(snapshot.child(content_type).getValue().toString());
                    latest=new String[5];
                    for(i=0;i<=4;i++)
                    {
                        latest[i]=snapshot.child("latest"+(i+1)).getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else
            {
                findViewById(R.id.find_name).setVisibility(View.VISIBLE);
                findViewById(R.id.find_value).setVisibility(View.VISIBLE);
            }
        findViewById(R.id.options).setVisibility(View.GONE);
    }
    private void findName()
    {
        final EditText editText = findViewById(R.id.name);
        findName = editText.getText().toString();
        if(findName.trim().length()==0) return;
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i,j;
                String type[] = new String[]{"news","comedy","movie","sport","classic","kids"};
                for(i=0;i<type.length;i++) {
                    for (j = 1; j <= Integer.parseInt(snapshot.child(type[i]).getValue().toString()); j++) {
                        if (snapshot.child(type[i] + j).getValue().toString().equals(findName))
                            findName = type[i]+j;
                    }
                }
                editText.setText(findName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void findValue()
    {
        EditText editText = findViewById(R.id.name);
        findName = editText.getText().toString();
        final EditText valueText = findViewById(R.id.value);
        if(findName.trim().length()==0) return;
        FirebaseDatabase.getInstance().getReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    findValue = snapshot.child(findName).getValue().toString();
                    valueText.setText(findValue);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}