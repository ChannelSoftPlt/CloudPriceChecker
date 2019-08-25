package com.jby.pricechecker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.jby.pricechecker.db.DbUser;
import com.jby.pricechecker.sharePreference.SharedPreferenceManager;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText loginActivityUsername, loginActivityPassword;
    private Button loginActivityLogin;
    private DbUser dbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        isLogin();
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        loginActivityUsername = findViewById(R.id.activity_login_username);
        loginActivityPassword = findViewById(R.id.activity_login_password);
        loginActivityLogin = findViewById(R.id.activity_login_login_button);

        dbUser = new DbUser(this);
        Stetho.initializeWithDefaults(this);
    }

    private void objectSetting() {
        loginActivityLogin.setOnClickListener(this);
        preSetUser();
    }

    @Override
    public void onClick(View view) {
        view.setEnabled(false);
        switch(view.getId()){
            case R.id.activity_login_login_button:
                checkingInput();
                break;
        }
    }

    private void checkingInput(){
        String username = loginActivityUsername.getText().toString().trim();
        String password = loginActivityPassword.getText().toString().trim();
        if(username.length() > 0 && password.length() > 0){
            login(username, password);
        }
        else
            Toast.makeText(this, "All Field Above is Required!", Toast.LENGTH_SHORT).show();

        loginActivityLogin.setEnabled(true);
    }

    private void login(String username, String password){
        String userID = dbUser.login(username, password);
        if(userID == null)
            Toast.makeText(this, "Invalid username or password!", Toast.LENGTH_SHORT).show();
        else{
            SharedPreferenceManager.setUserID(this, userID);
            checkingAfterLoginSuccess();
        }

        loginActivityLogin.setEnabled(true);
    }

    private void checkingAfterLoginSuccess(){
        if(!SharedPreferenceManager.getFirstTimeLogin(this).equals("default")){
            if(SharedPreferenceManager.getAPI(this).equals("default"))
                startActivity(new Intent(this, SettingActivity.class));
            else
                startActivity(new Intent(this, MainActivity.class));
        }
        else
            startActivity(new Intent(this, ChangeUserDetailActivity.class));
        finish();
    }

    private void isLogin(){
        if(!SharedPreferenceManager.getUserID(this).equals("default"))
           checkingAfterLoginSuccess();
    }

    private void preSetUser(){
        if(dbUser.countUser() <= 0)
        {
            dbUser.insertUser("user", "123456");
            dbUser.insertUser("admin", "88888888");

        }
    }

}
