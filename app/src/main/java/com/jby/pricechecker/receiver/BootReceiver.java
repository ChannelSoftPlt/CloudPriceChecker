package com.jby.pricechecker.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.jby.pricechecker.MainActivity;
import com.jby.pricechecker.sharePreference.SharedPreferenceManager;

import java.util.Objects;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)) {
            //open activity automatically
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            setUpTimer(context);
        }
    }

    private void setUpTimer(Context context) {
        if (SharedPreferenceManager.getShutDownTimer(context) != 0) {
            //alarm setting
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
            AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (manager != null) {
                manager.set(AlarmManager.RTC_WAKEUP, SharedPreferenceManager.getShutDownTimer(context), pendingIntent);
            }
        }
    }
}