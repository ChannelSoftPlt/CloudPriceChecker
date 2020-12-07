package com.jby.pricechecker.sharePreference;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by wypan on 2/24/2017.
 */

public class SharedPreferenceManager {

    private static String Auth = "auth";
    private static String UserID = "uid";
    private static String API = "api";
    private static String VideoPath = "videoPath";
    private static String FileType = "fileType";
    private static String FirstTimeLogin = "firstTimeLogin";
    private static String ShutDownTimer = "shutDownTimer";
    private static String DisplayTimer = "displayTimer";
    private static String CurrentDate = "currentDate";
    private static String TouchScreen = "touch_screen";

    private static SharedPreferences getSharedPreferences(Context context) {
        String SharedPreferenceFileName = "PriceChecker";
        return context.getSharedPreferences(SharedPreferenceFileName, Context.MODE_PRIVATE);
    }

    public static void clear(Context context){
        getSharedPreferences(context).edit().clear().apply();
    }

    /*
    *       User Shared Preference
    * */


    public static String getUserID(Context context) {
        return getSharedPreferences(context).getString(UserID, "default");
    }

    public static void setUserID(Context context, String userID) {
        getSharedPreferences(context).edit().putString(UserID, userID).apply();
    }


    public static String getAPI(Context context) {
        return getSharedPreferences(context).getString(API, "https://www.channelsoft.com.my");
    }

    public static void setAPI(Context context, String api) {
        getSharedPreferences(context).edit().putString(API, api).apply();
    }


    public static String getVideoPath(Context context) {
        return getSharedPreferences(context).getString(VideoPath, "default");
    }

    public static void setVideoPath(Context context, String videoPath) {
        getSharedPreferences(context).edit().putString(VideoPath, videoPath).apply();
    }

    public static String getFirstTimeLogin(Context context) {
        return getSharedPreferences(context).getString(FirstTimeLogin, "default");
    }

    public static void setFirstTimeLogin(Context context, String firstTimeLogin) {
        getSharedPreferences(context).edit().putString(FirstTimeLogin, firstTimeLogin).apply();
    }

    public static String getFileType(Context context) {
        return getSharedPreferences(context).getString(FileType, "default");
    }

    public static void setFileType(Context context, String fileType) {
        getSharedPreferences(context).edit().putString(FileType, fileType).apply();
    }

    public static long getShutDownTimer(Context context) {
        return getSharedPreferences(context).getLong(ShutDownTimer, 0);
    }

    public static void setShutDownTimer(Context context, long shutDownTimer) {
        getSharedPreferences(context).edit().putLong(ShutDownTimer, shutDownTimer).apply();
    }

    public static String getDisplayTimer(Context context) {
        return getSharedPreferences(context).getString(DisplayTimer, "default");
    }

    public static void setDisplayTimer(Context context, String displayTimer) {
        getSharedPreferences(context).edit().putString(DisplayTimer, displayTimer).apply();
    }
    public static String getCurrentDate(Context context) {
        return getSharedPreferences(context).getString(CurrentDate, "default");
    }

    public static void setCurrentDate(Context context, String currentDate) {
        getSharedPreferences(context).edit().putString(CurrentDate, currentDate).apply();
    }

    public static boolean getTouchScreen(Context context) {
        return getSharedPreferences(context).getBoolean(TouchScreen, false);
    }

    public static void setTouchScreen(Context context, boolean touchScreen) {
        getSharedPreferences(context).edit().putBoolean(TouchScreen, touchScreen).apply();
    }

    public static boolean getAuth(Context context) {
        return getSharedPreferences(context).getBoolean(Auth, false);
    }

    public static void setAuth(Context context, boolean auth) {
        getSharedPreferences(context).edit().putBoolean(Auth, auth).apply();
    }

}
