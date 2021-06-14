package com.example.news;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class UserName {
    private static String username,quickPicks;
    private static ArrayList<String> watchList;
    public static String getUsername()
    {
        return username;
    }
    public static String getUsername(Context context){
        try {
            FileInputStream fis = context.openFileInput("email_id.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            return br.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void setUsername(String str)
    {
        username=str;
    }
    public static void setUsername(Context context,String str){
        File file = new File(context.getFilesDir(),"email_id.txt");
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = context.openFileOutput("email_id.txt",Context.MODE_PRIVATE);
            fos.write(str.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void setQuickPicks(String str){
        quickPicks=str;
    }
    public static String getQuickPicks(){
        return quickPicks;
    }

    public static void setWatchlist(Object watchList) {
        try{
            UserName.watchList = (ArrayList<String>) watchList;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static ArrayList<String> getWatchList(){return watchList;}
}
