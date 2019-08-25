package com.jby.pricechecker;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.jby.pricechecker.others.FullScreenVideoView;
import com.jby.pricechecker.sharePreference.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;

import static com.jby.pricechecker.requestVariable.RequestVariable.UPDATE_MAIN_ACTIVITY;

public class MainActivity extends AppCompatActivity implements DisplayProductDialog.DisplayProductDialogCallBack, MediaPlayer.OnCompletionListener {
    private LinearLayout activityMainScanLayout;
    private ImageView activityMainImageView;
    private FullScreenVideoView activityMainVideoView;
    private RelativeLayout activityMainParentLayout, activityMainProgressBarLayout;

    private Handler handler;

    private ImageView activityMainScanIcon;

    private ArrayList<String> gallerySelectionResult;
    private int currentPlayPosition;
    private int currentPlayVideo = 0;

    private String barcode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        objectInitialize();
    }

    private void objectInitialize() {

        activityMainImageView = findViewById(R.id.activity_main_image);
        activityMainScanIcon = findViewById(R.id.activity_main_scan_icon);
        activityMainVideoView = findViewById(R.id.activity_main_video);
        activityMainParentLayout = findViewById(R.id.activity_main_parent_layout);

        activityMainScanLayout = findViewById(R.id.activity_main_scan_layout);

        activityMainProgressBarLayout = findViewById(R.id.product_progress_bar_layout);
        handler = new Handler();
        objectSetting();
    }

    private void objectSetting() {
        activityMainVideoView.setOnCompletionListener(this);

        Glide.with(this).load(R.drawable.scan_icon).into(activityMainScanIcon);
        setUpFadeAnimation(activityMainScanLayout);
        //general setup
        checkingPath();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                activityMainProgressBarLayout.setVisibility(View.GONE);
                activityMainParentLayout.setVisibility(View.VISIBLE);
            }
        }, 300);
    }

    private void checkingPath() {
        String fileType = SharedPreferenceManager.getFileType(this);
        //mean file is added
        if (!fileType.equals("default")) {
            if (fileType.equals("video")) setUpVideoView();
            else setUpImageView();

        }
    }

    private void setUpImageView() {
        Uri imageUrl = Uri.parse(initializeList(0));
        activityMainImageView.setImageURI(imageUrl);
        activityMainImageView.setVisibility(View.VISIBLE);

        //hide
        activityMainVideoView.setVisibility(View.GONE);
    }

    private void setUpVideoView() {
        Uri videoUri = Uri.parse(initializeList(currentPlayVideo));
        activityMainVideoView.setVideoURI(videoUri);
        activityMainVideoView.setVisibility(View.VISIBLE);
        activityMainVideoView.start();

        //hide
        activityMainImageView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (activityMainVideoView != null) {
            activityMainVideoView.seekTo(currentPlayPosition);
            activityMainVideoView.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentPlayPosition = activityMainVideoView.getCurrentPosition(); //stopPosition is an int
        if (activityMainVideoView.isPlaying())
            activityMainVideoView.pause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == UPDATE_MAIN_ACTIVITY) {
            checkingPath();
        }
    }


    private void setUpFadeAnimation(final LinearLayout textView) {
        // Start from 0.1f if you desire 90% fade animation
        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(100);
        fadeIn.setStartOffset(500);
        // End to 0.1f if you desire 90% fade animation
        final Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(100);
        fadeOut.setStartOffset(500);

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                // start fadeOut when fadeIn ends (continue)
                textView.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                // start fadeIn when fadeOut ends (repeat)
                textView.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationStart(Animation arg0) {
            }
        });

        textView.startAnimation(fadeOut);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (currentPlayVideo < gallerySelectionResult.size() - 1) currentPlayVideo++;
        else currentPlayVideo = 0;

        setUpVideoView();
    }

    private String initializeList(int position) {
        String selectedItem = SharedPreferenceManager.getVideoPath(this);
        String[] list = selectedItem.split(",");
        gallerySelectionResult = new ArrayList<>();

        gallerySelectionResult.addAll(Arrays.asList(list));
        return gallerySelectionResult.get(position);
    }


    /*--------------------------------------------------scan purpose-----------------------------------------------------------------*/
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {

        if(e.getAction()==KeyEvent.ACTION_DOWN){
            char pressedKey = (char) e.getUnicodeChar();
            barcode += pressedKey;
        }
        if (e.getAction()==KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            scanAction(barcode.trim());
            barcode="";
        }
        return super.dispatchKeyEvent(e);
    }

    private void scanAction(final String result) {
        switch (result) {
            //close app
            case "1111logout":
                logOut();
                break;
            //log out
            case "2222exit":
                onBackPressed();
                break;
//            setting
            case "3333setting":
                startActivityForResult(new Intent(this, SettingActivity.class), UPDATE_MAIN_ACTIVITY);
                break;
            case "4444shutdown":
                shutDown();
                break;
            default:
                openProductDialog(result.trim());
                break;
        }
    }

    private void logOut() {
        SharedPreferenceManager.setUserID(this, "default");
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void openProductDialog(String barcode) {
        onPause();
        android.support.v4.app.DialogFragment dialogFragment = new DisplayProductDialog();
        FragmentManager fm = getSupportFragmentManager();
        Bundle bundle = new Bundle();

        bundle.putString("item_barcode", barcode);
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    private void shutDown() {
        try {
            Process proc = Runtime.getRuntime()
                    .exec(new String[]{"su", "-c", "reboot -p"});
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //    --------------------------------------------------full screen-----------------------------------------------------------------------
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
}
