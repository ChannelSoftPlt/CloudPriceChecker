package com.jby.pricechecker.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jby.pricechecker.sharePreference.SharedPreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // show toast
        Log.d("haha", "Alarm Manager: isRunning ");
        setUpNewwShutDownTimer(context);
        shutDown();
    }

    private void setUpNewwShutDownTimer(Context context){
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date); // e.g. 2015-01-18
        //if current date is different
        if(!currentDate.equals(SharedPreferenceManager.getCurrentDate(context))){
            //set new timer before close
            long newTimer = SharedPreferenceManager.getShutDownTimer(context) + 86400000;
            SharedPreferenceManager.setShutDownTimer(context, newTimer);
            //set new current date
            SharedPreferenceManager.setCurrentDate(context, currentDate);
        }
    }

    private void shutDown(){
//        try {
//            Process proc = Runtime.getRuntime()
//                    .exec(new String[]{ "su", "-c", "reboot -p" });
//            proc.waitFor();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }
}