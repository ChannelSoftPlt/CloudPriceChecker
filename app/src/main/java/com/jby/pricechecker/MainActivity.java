package com.jby.pricechecker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.droidnet.DroidListener;
import com.droidnet.DroidNet;
import com.jby.pricechecker.others.MySingleton;
import com.jby.pricechecker.sharePreference.SharedPreferenceManager;

import junit.runner.Version;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import im.delight.android.webview.AdvancedWebView;

import static com.jby.pricechecker.requestVariable.RequestVariable.UPDATE_MAIN_ACTIVITY;
import static com.jby.pricechecker.shareObject.CustomToast.CustomToast;

public class MainActivity extends AppCompatActivity implements AdvancedWebView.Listener, DroidListener {
    private DroidNet mDroidNet;

    private RelativeLayout activityMainProgressBarLayout;
    private TextView progressBarLabel;
    AdvancedWebView mWebView;
    private Handler handler;

    private String barcode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideSystemUI();
        setContentView(R.layout.activity_main);
        objectInitialize();
    }

    private void objectInitialize() {
        activityMainProgressBarLayout = findViewById(R.id.product_progress_bar_layout);
        progressBarLabel = findViewById(R.id.progress_bar_label);

        mWebView = (AdvancedWebView) findViewById(R.id.webview);
        mWebView.setListener(this, this);
        mWebView.setMixedContentAllowed(false);

        handler = new Handler();

        mDroidNet = DroidNet.getInstance();
        mDroidNet.addInternetConnectivityListener(this);

    }


    private void objectSetting() {
        if (SharedPreferenceManager.getAuth(this)) {
            preChecking();
        } else {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 101);
                    return;
                }
                checkDeviceAvailability(getSerialNo());
            } catch (Exception e) {
                Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void preChecking() {
        checkTouchScreen();
        if (SharedPreferenceManager.getAPI(this).equals("https://wwww.channelsoft.com.my")) {
            openSettingPage(null);
        } else loadWebView();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                checkDeviceAvailability(getSerialNo());
            } else {
                /*
                 * request again
                 * */
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 101);
                    return;
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    public void checkDeviceAvailability(final String deviceId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://emenu.com.my/price_checker/index.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("status");
                    if (status.equals("1")) {
                        SharedPreferenceManager.setAuth(getApplicationContext(), true);
                        objectSetting();
                        return;
                    }
                    showInvalidDeviceDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showInvalidDeviceDialog();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CustomToast(getApplicationContext(), "Something Went Wrong!");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("device_id", deviceId);
                params.put("read", "1");
                return params;
            }
        };
        MySingleton.getmInstance(this).addToRequestQueue(stringRequest);
    }

    @SuppressLint("MissingPermission")
    public String getSerialNo() {
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            return Build.SERIAL;
        } else return Build.getSerial();
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public String getDeviceIMEI() {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            deviceUniqueIdentifier = tm.getDeviceId();
        }
        if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
            deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return deviceUniqueIdentifier;
    }

    private void showInvalidDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Device Not Found");
        builder.setMessage("Please make sure you have registered this device.");
        builder.setCancelable(false);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });

        builder.setPositiveButton("Okay", null);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void checkTouchScreen() {
        //disable touch screen
        if (SharedPreferenceManager.getTouchScreen(this)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        //enable touch screen
        else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void loadWebView() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWebView.setClickable(false);
                mWebView.setEnabled(false);
                mWebView.loadUrl(SharedPreferenceManager.getAPI(getApplicationContext()));
            }
        }, 400);
    }

    private void showProgressBar(boolean show, String message) {
        activityMainProgressBarLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        progressBarLabel.setText(message);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDroidNet.removeInternetConnectivityChangeListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == UPDATE_MAIN_ACTIVITY) {
            checkTouchScreen();
            showProgressBar(true, "Connecting");
            loadWebView();
        }
    }

    /*--------------------------------------------------scan purpose-----------------------------------------------------------------*/
//    @Override
//    public boolean dispatchKeyEvent(KeyEvent e) {
//
//        if (e.getAction() == KeyEvent.ACTION_DOWN) {
//            char pressedKey = (char) e.getUnicodeChar();
//            barcode += pressedKey;
//        }
//        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
//            scanAction(barcode.trim());
//            barcode = "";
//        }
//        return super.dispatchKeyEvent(e);
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getAction() == KeyEvent.ACTION_DOWN
                && e.getKeyCode() != KeyEvent.KEYCODE_ENTER) { //Not Adding ENTER_KEY to barcode String
            char pressedKey = (char) e.getUnicodeChar();
            barcode += pressedKey;
        }
        if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            Toast.makeText(this, "" + barcode, Toast.LENGTH_SHORT).show();
            scanAction(barcode);
            barcode = "";
        }
        return true;
    }


    private void scanAction(final String result) {
        Toast.makeText(this, "" + result, Toast.LENGTH_SHORT).show();
        switch (result) {
            //log out
            case "2222exit":
                onBackPressed();
                break;
//            setting
            case "3333setting":
                openSettingPage(null);
                break;
            case "4444shutdown":
                shutDown();
                break;
        }
    }

    public void openSettingPage(View view) {
        startActivityForResult(new Intent(this, SettingActivity.class), UPDATE_MAIN_ACTIVITY);
    }

    public void shutDown() {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"su", "0", "reboot", "-p"});
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

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                showProgressBar(false, "");
            }
        }, 500);
    }

    @Override
    public void onPageFinished(String url) {

    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {

    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {

    }

    @Override
    public void onInternetConnectivityChanged(boolean isConnected) {
        if (isConnected) {
            showProgressBar(true, "Connecting");
            objectSetting();
        } else {
            showProgressBar(true, "Something wrong with your network!");
        }
    }
}
