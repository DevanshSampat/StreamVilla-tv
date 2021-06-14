package com.example.news;

public class DownloadFileData {
    private static int percent;
    private static String text;
    private static String timeLeft;
    private static long size;
    private static long time;
    private static long prev_time_diff;
    private static long downloadedBytes;
    private static String speed;
    private static boolean paused;
    private static String download_backup;
    public static boolean isPaused() {
        return paused;
    }

    public static void setPaused(boolean isPaused) {
        DownloadFileData.paused = isPaused;
    }

    public static String getSpeed() {
        return speed;
    }

    public static void setSpeed(String speed) {
        DownloadFileData.speed = speed;
    }


    public static void setDownloadedBytes(long downloadedBytes) {
        long diff = downloadedBytes - DownloadFileData.downloadedBytes;
        double rate = (double) diff/prev_time_diff;
        DownloadFileData.downloadedBytes = downloadedBytes;
        long time_remaining = (long) ((size-downloadedBytes)/rate);
        time_remaining = time_remaining/1000;
        String time_to_set = time_remaining + " sec";
        if(time_remaining>60&&time_to_set.contains("sec")){
            time_remaining = time_remaining/60;
            time_to_set = time_remaining + " min";
        }
        if(time_remaining>60&&time_to_set.contains("min")){
            time_remaining = time_remaining/60;
            time_to_set = time_remaining + " hr";
        }
        if(time_remaining>24&&time_to_set.contains("hr")){
            time_remaining = time_remaining/24;
            time_to_set = time_remaining + " day(s)";
        }
        if(time_remaining<=59&&time_remaining>0) setTimeLeft(time_to_set+" remaining");
        else return;
        speed = " BPS";
        rate = rate*1000;
        if(rate>=1000){
            rate = rate/1024;
            speed = " KBPS";
        }
        if(rate>=1000&&speed.contains(" KBPS")){
            rate = rate/1024;
            speed = " MBPS";
        }
        if(rate>=1000&&speed.contains(" MBPS")){
            rate = rate/1024;
            speed = " GBPS";
        }
        String convertedRate = String.valueOf(rate);
        convertedRate = convertedRate.substring(0,convertedRate.indexOf('.')+3);
        speed = convertedRate+speed;
        setSpeed(speed);
    }


    public static void setSize(long size) {
        DownloadFileData.size = size;
    }

    public static long getTime() {
        return time;
    }

    public static void setTime(long time) {
        prev_time_diff = time - DownloadFileData.time;
        DownloadFileData.time = time;
    }

    public static void setTimeLeft(String timeLeft) {DownloadFileData.timeLeft = timeLeft;}

    public static String getTimeLeft() {return timeLeft;}
    public static int getPercent(){return percent;}
    public static void setPercent(int x){percent=x;}
    public static String getText(){return text;}
    public static void setText(String str){
        text=str;
        if(!str.equals("Long Press to Resume")) setDownloadBackup(str);
    }

    public static String getDownloadBackup() {
        return download_backup;
    }

    public static void setDownloadBackup(String download_backup) {
        DownloadFileData.download_backup = download_backup;
    }
}
