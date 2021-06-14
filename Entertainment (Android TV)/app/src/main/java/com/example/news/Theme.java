package com.example.news;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Theme {
    private Context context;
    public Theme(Context context){this.context = context;}
    public boolean isInDarkMode(){
        try{
            FileInputStream fis = context.openFileInput("Theme.txt");
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
