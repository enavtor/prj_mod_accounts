package com.droidmare.accounts.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.Locale;

/**
 * Extracts STB and app information
 * @author Carolina on 23/10/2017.
 */
public class STBUtils {

    /**
     * Gets serial number of current device
     * @return Device serial number
     */
    public static String getSTBSerialNumber() {
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O)
            return Build.SERIAL;
        return Build.getSerial();
    }


    /**
     * Gets display name of current device
     * @return Device display name
     */
    public static String getSTBDisplayName(){
        return Build.DISPLAY;
    }


    /**
     * Gets current app version number
     * @return App version number of current application
     */
    public static int getAppVersionNumber(Context context) {
        try{return context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode;}
        catch(PackageManager.NameNotFoundException nfe){return 0;}
    }


    /**
     * Gets current app version name
     * @return App version name of current application
     */
    public static String getAppVersionName(Context context) {
        try{return context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;}
        catch(PackageManager.NameNotFoundException nfe){return getSTBDisplayName();}
    }


    /**
     * Gets the country of the device
     * @return Country name
     */
    public static String getCountryName(){
        return Locale.getDefault().getCountry();
    }


    /**
     * Gets if camera is connected
     * @return True if camera is connected, false otherwise
     */
    public static boolean isCameraConnected(){
        String DEV_VIDEO_prefix="/dev/video";
        int DEV_NUM=5;
        int devNum=0;
        for(int i=0;i<DEV_NUM;i++){
            String path=DEV_VIDEO_prefix+i;
            if (new File(path).exists())
                devNum++;
        }
        return devNum>0;
    }


    /**
     * Gets memory used
     * @return Percentage of memory used
     */
    public static int percentageOfMemoryUsed(){
        StatFs stat=new StatFs(Environment.getDataDirectory().getPath());
        long total=stat.getTotalBytes();
        long used=stat.getFreeBytes();
        return (int)(used*100/total);
    }


    /**
     * Gets battery level
     * @param intent Broadcast intent
     * @return Percentage of remaining battery
     */
    public static int percentageOfRemainingBattery(Intent intent){
        int level=intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale=intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if(level==0)level=100;
        return level*100/scale;
    }


    /**
     * Gets mass storage path
     * @return Path to mass storage
     */
    public static File getMassStoragePath(){
        File storagePath=new File("/storage");
        File[] files=storagePath.listFiles();
        for(File file:files){
            if(file.isDirectory() && !file.getName().equals("emulated") && !file.getName().equals("self"))
                return file;
        }
        return null;
    }

}
