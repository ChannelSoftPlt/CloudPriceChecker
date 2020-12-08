package com.jby.pricechecker.receiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.util.Log;


import com.jby.pricechecker.sharePreference.SharedPreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        Bundle bundle = new Bundle();
//        bundle.putBoolean("refresh", true);
//        bundle.putString("alarm_id", intent.getStringExtra("alarm_id"));
//
//        Intent i = new Intent("alarmManager");
//        i.putExtras(bundle);
//        Log.d("haha", "Alarm Fired");
//        Log.d("haha", intent.getStringExtra("alarm_id"));
//        context.sendBroadcast(i);
        shutDown();
    }

    /*
     * set shut down timer
     * */
    public static void setShutDownTimer(Context context) {
        //alarm setting
        AlarmManager alarmManager;
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("alarm_id", "shut_down_timer");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 3, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            Calendar cal = Calendar.getInstance();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

            String shutDownTimer = SharedPreferenceManager.getShutDownTimer(context);
            Date currentTime = formatter.parse(formatter.format(cal.getTime()));
            Date shutDownTime = formatter.parse(shutDownTimer);

            if (currentTime.before(shutDownTime) || currentTime == shutDownTime) {
                String[] timer = shutDownTimer.split(":");
                cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timer[0]));
                cal.set(Calendar.MINUTE, Integer.parseInt(timer[1]));

                alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            } else {
                Log.d("MainActivity", "Shut Down Time is over!");
            }

        } catch (Exception e) {
            Log.d("MainActivity", "unable to set shut down timer");
            e.printStackTrace();
        }
    }

    public void shutDown() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "0", "reboot", "-p"});
            process.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}