package com.jby.pricechecker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.erikagtierrez.multiple_media_picker.Gallery;
import com.jby.pricechecker.gallery.GalleryAdapter;
import com.jby.pricechecker.others.ExpandableHeightListView;
import com.jby.pricechecker.receiver.AlarmReceiver;
import com.jby.pricechecker.sharePreference.SharedPreferenceManager;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.jby.pricechecker.requestVariable.RequestVariable.READ_STORAGE_PERMISSION;
import static com.jby.pricechecker.requestVariable.RequestVariable.UPDATE_MAIN_ACTIVITY;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener, SelectFromGalleryDialog.SelectFromGalleryDialogCallBack{
    private Toolbar actionbar;
    private TextView actionBarSave;
    private EditText settingActivityApi;
    private TextView settingActivitySampleApi, settingActivityShutDownTimer, settingActivityVersion;
    private Button settingActivitySelectVideoButton;
    //gallery list
    private ExpandableHeightListView settingActivityGalleryList;
    private GalleryAdapter galleryAdapter;
    private boolean isVideo = false;

    private String scanSpeed;

    private TimePickerDialog timePicker;
    private Calendar calendar, mCurrentTime;
    private String time = "-";
    private int hour, minute;

    private ArrayList<String> gallerySelectionResult;
    private boolean isNewGallery = false;
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

        settingActivitySelectVideoButton = findViewById(R.id.activity_setting_select_video_button);

        settingActivityGalleryList = findViewById(R.id.activity_setting_gallery_list);


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

        settingActivitySelectVideoButton.setOnClickListener(this);
        settingActivitySampleApi.setOnClickListener(this);
        settingActivityShutDownTimer.setOnClickListener(this);

        initializeValue();
    }

    private void initializeValue() {
        settingActivityApi.append(SharedPreferenceManager.getAPI(this));

        if(!SharedPreferenceManager.getDisplayTimer(this).equals("default"))
            settingActivityShutDownTimer.setText(SharedPreferenceManager.getDisplayTimer(this));

        if(!SharedPreferenceManager.getVideoPath(this).equals("default"))
            initializeList();

        setVersion();
    }

    private void setUpGalleryList(ArrayList<String> list){
        galleryAdapter = new GalleryAdapter(this, list);
        settingActivityGalleryList.setAdapter(galleryAdapter);
        settingActivityGalleryList.setExpanded(true);
    }

    private void initializeList(){
        String selectedItem = SharedPreferenceManager.getVideoPath(this);
        String[] list = selectedItem.split(",");
        gallerySelectionResult = new ArrayList<>();
        gallerySelectionResult.addAll(Arrays.asList(list));

        setUpGalleryList(gallerySelectionResult);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionbar_save:
                save();
                break;
            case R.id.activity_setting_select_video_button:
                if (checkReadStoragePermission()) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        openGalleryDialog();
                    }
                }
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

    private void save(){
        if(!settingActivityApi.getText().toString().trim().equals("")){
            String api = SharedPreferenceManager.getAPI(this);
            SharedPreferenceManager.setAPI(this, settingActivityApi.getText().toString().trim());

            if(gallerySelectionResult != null && gallerySelectionResult.size() > 0 && isNewGallery)
                storeVideoPath();

            if(calendar != null)
                setUpAlarm();

            if(api.equals("default")){
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
            else{
                setResult(UPDATE_MAIN_ACTIVITY);
                onBackPressed();
            }
            Toast.makeText(this, "Save Successfully!", Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(this, "Please fill in your API.", Toast.LENGTH_SHORT).show();
    }

    private void storeVideoPath(){
        SharedPreferenceManager.setFileType(this, isVideo ? "video" : "picture");

        StringBuilder sb = new StringBuilder();
        // more than one selected item
        if(gallerySelectionResult.size() > 1){
            for(int i = 0 ; i < gallerySelectionResult.size(); i++){
                sb.append(gallerySelectionResult.get(i)).append(",");
            }
            SharedPreferenceManager.setVideoPath(this, sb.toString());
        }
        //if only one item selected
        else {
            SharedPreferenceManager.setVideoPath(this, gallerySelectionResult.get(0));
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /*------------------------------------------------------------------------select video path purpose----------------------------------------------------*/

    static final int OPEN_MEDIA_PICKER = 1;  // Request code

    public void selectFromGallery(int type, int maxSelection) {
        // Mode 1 = both, 2 = photo ,3 = videos
        /*
        *
        * check is video or not
        * */
        isVideo = (type != 2);
        /*
        * open gallery class
        * */
        Intent intent = new Intent(this, Gallery.class);
        // Set the title
        intent.putExtra("title", "Select media");
        intent.putExtra("mode", type);
        intent.putExtra("maxSelection", maxSelection); // Optional
        startActivityForResult(intent, OPEN_MEDIA_PICKER);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != -1  && resultCode == RESULT_OK)
        {
            // Check which request we're responding to
            if (requestCode == OPEN_MEDIA_PICKER) {
                // Make sure the request was successful
                if (data != null) {
                    isNewGallery = true;
                    gallerySelectionResult = data.getStringArrayListExtra("result");
                    setUpGalleryList(gallerySelectionResult);
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_STORAGE_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) openGalleryDialog();
        else checkReadStoragePermission();
    }

    public boolean checkReadStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission. READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission. READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION);
            return false;
        } else {
            return true;
        }
    }

    private void openGalleryDialog(){
        DialogFragment dialogFragment = new SelectFromGalleryDialog();
        FragmentManager fragmentManager = getSupportFragmentManager();
        dialogFragment.show(fragmentManager, "");
    }


    /*--------------------------------------------------------------------end of video path purpose-------------------------------------------------------*/
    public void selectTimeDialog(){
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
                time = String.format(Locale.getDefault(),format, selectedHour, selectedMinute);
                hour = selectedHour;
                minute = selectedMinute;

                settingActivityShutDownTimer.setText(time);

            }
        }, hour, minute, false);
        timePicker.setTitle("Select Time");

        timePicker.show();
    }

    private void setUpAlarm(){
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

    public void setVersion(){
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            settingActivityVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*----------------------------------------------------------------------------other--------------------------------------------------------------------------------------------*/
    //this function returns null when using IO file manager
    public static String getPath(final Context context, final Uri uri) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            }

            // TODO handle non-primary volumes
        }
        // DownloadsProvider
        else if (isDownloadsDocument(uri)) {

            final String id = DocumentsContract.getDocumentId(uri);
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            return getDataColumn(context, contentUri, null, null);
        }
        // MediaProvider
        else if (isMediaDocument(uri)) {
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{
                    split[1]
            };

            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            Log.d("haha", "haha: uri" + cursor);

            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
