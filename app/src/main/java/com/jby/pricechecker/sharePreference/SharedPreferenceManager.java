package com.jby.pricechecker.sharePreference;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

/**
 * Created by wypan on 2/24/2017.
 */

public class SharedPreferenceManager {

    private static String Auth = "auth";
    private static String API = "api";
    private static String ShutDownTimer = "shutDownTimer";
    private static String TouchScreen = "touch_screen";

    private static SharedPreferences getSharedPreferences(Context context) {
        String SharedPreferenceFileName = "PriceChecker";
        return context.getSharedPreferences(SharedPreferenceFileName, Context.MODE_PRIVATE);
    }

    public static void clear(Context context) {
        getSharedPreferences(context).edit().clear().apply();
    }

    public static String getAPI(Context context) {
        return getSharedPreferences(context).getString(API, "https://www.channelsoft.com.my");
    }

    public static void setAPI(Context context, String api) {
        getSharedPreferences(context).edit().putString(API, api).apply();
    }


    public static String getShutDownTimer(Context context) {
        return getSharedPreferences(context).getString(ShutDownTimer, "default");
    }

    public static void setShutDownTimer(Context context, String shutDownTimer) {
        getSharedPreferences(context).edit().putString(ShutDownTimer, shutDownTimer).apply();
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
