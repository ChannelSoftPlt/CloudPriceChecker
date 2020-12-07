package com.jby.pricechecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.jby.pricechecker.receiver.AlarmReceiver;
import com.jby.pricechecker.sharePreference.SharedPreferenceManager;


import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import static com.jby.pricechecker.requestVariable.RequestVariable.UPDATE_MAIN_ACTIVITY;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar actionbar;
    private TextView actionBarSave;
    private EditText settingActivityApi;
    private CheckBox settingActivityTouchScreen;
    private TextView settingActivitySampleApi, settingActivityShutDownTimer, settingActivityVersion;


    private TimePickerDialog timePicker;
    private Calendar calendar, mCurrentTime;
    private String time = "-";
    private int hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionbar = findViewById(R.id.toolbar);
        actionBarSave = findViewById(R.id.actionbar_save);

        settingActivityApi = findViewById(R.id.activity_setting_api);
        settingActivitySampleApi = findViewById(R.id.activity_setting_sample_api);
        settingActivityShutDownTimer = findViewById(R.id.activity_setting_shut_down_timer);
        settingActivityVersion = findViewById(R.id.activity_setting_version);
        settingActivityTouchScreen = findViewById(R.id.touchScreen);
    }

    private void objectSetting() {
        setSupportActionBar(actionbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Setting");

        actionbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        actionBarSave.setVisibility(View.VISIBLE);
        actionBarSave.setOnClickListener(this);

        settingActivitySampleApi.setOnClickListener(this);
        settingActivityShutDownTimer.setOnClickListener(this);

        initializeValue();
    }

    private void initializeValue() {
        settingActivityApi.append(SharedPreferenceManager.getAPI(this));

        if (!SharedPreferenceManager.getDisplayTimer(this).equals("default"))
            settingActivityShutDownTimer.setText(SharedPreferenceManager.getDisplayTimer(this));

        settingActivityTouchScreen.setChecked(SharedPreferenceManager.getTouchScreen(this));

        setVersion();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.actionbar_save:
                save();
                break;
            case R.id.activity_setting_shut_down_timer:
                selectTimeDialog();
                break;
            case R.id.activity_setting_sample_api:
                DialogFragment dialogFragment = new SampleApiDialog();
                FragmentManager fm = getSupportFragmentManager();
                dialogFragment.show(fm, "");
                break;
        }
    }

    private void save() {
        String api = SharedPreferenceManager.getAPI(this);
        SharedPreferenceManager.setAPI(this, settingActivityApi.getText().toString().trim());

        SharedPreferenceManager.setTouchScreen(this, settingActivityTouchScreen.isChecked());

        if (calendar != null)
            setUpAlarm();

        if (api.equals("default")) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            setResult(UPDATE_MAIN_ACTIVITY);
            onBackPressed();
        }
        Toast.makeText(this, "Save Successfully!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /*--------------------------------------------------------------------end of video path purpose-------------------------------------------------------*/
    public void selectTimeDialog() {
        mCurrentTime = Calendar.getInstance();
        hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
        minute = mCurrentTime.get(Calendar.MINUTE);

        timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override

            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);
                calendar.set(Calendar.SECOND, 0);
                String format = "%02d:%02d";
                time = String.format(Locale.getDefault(), format, selectedHour, selectedMinute);
                hour = selectedHour;
                minute = selectedMinute;

                settingActivityShutDownTimer.setText(time);

            }
        }, hour, minute, false);
        timePicker.setTitle("Select Time");

        timePicker.show();
    }

    private void setUpAlarm() {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        SharedPreferenceManager.setShutDownTimer(this, calendar.getTimeInMillis());
        SharedPreferenceManager.setCurrentDate(this, "default");
        SharedPreferenceManager.setDisplayTimer(this, time);
    }

    public void setVersion() {
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            settingActivityVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
