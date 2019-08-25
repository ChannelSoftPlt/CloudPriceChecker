package com.jby.pricechecker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.pricechecker.db.DbUser;
import com.jby.pricechecker.sharePreference.SharedPreferenceManager;

import java.util.Objects;

public class ChangeUserDetailActivity extends AppCompatActivity {
    private Toolbar actionbar;
    private TextView actionBarSave;
    private EditText changeUserDetailActivityNewUsername,changeUserDetailActivityNewPassword, changeUserDetailActivityConfirmPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_user_detail);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        actionbar = findViewById(R.id.toolbar);
        actionBarSave = findViewById(R.id.actionbar_save);

        changeUserDetailActivityNewUsername = findViewById(R.id.activity_change_user_detail_new_username);
        changeUserDetailActivityNewPassword = findViewById(R.id.activity_change_user_detail_new_password);
        changeUserDetailActivityConfirmPassword = findViewById(R.id.activity_change_user_detail_confirm_password);
    }

    private void objectSetting() {
        setSupportActionBar(actionbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Setting");
        actionBarSave.setVisibility(View.VISIBLE);

        actionbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        actionBarSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                checking();
            }
        });
    }

    private void checking(){
        String username = changeUserDetailActivityNewUsername.getText().toString().trim();
        String password = changeUserDetailActivityNewPassword.getText().toString().trim();
        String confirmPassword = changeUserDetailActivityConfirmPassword.getText().toString().trim();
        if(!username.equals("") && !password.equals("") && !confirmPassword.equals(""))
        {
            //if password is match
            if(password.equals(confirmPassword)){
                //perform update action
                boolean update = new DbUser(this).updateUser(SharedPreferenceManager.getUserID(this), username, password);
                //if success
                if(update){
                    //set set time login = false
                    SharedPreferenceManager.setFirstTimeLogin(this, "false");
                    //check api is set or not
                    if(SharedPreferenceManager.getAPI(this).equals("default"))
                        startActivity(new Intent(this, SettingActivity.class));
                    else
                        startActivity(new Intent(this, MainActivity.class));
                    finish();
                }
                else Toast.makeText(this, "Something Went Wrong!", Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(this, "Your password is not matched!", Toast.LENGTH_SHORT).show();
        }
        else Toast.makeText(this, "Every field above is required!", Toast.LENGTH_SHORT).show();

        actionBarSave.setEnabled(true);
    }

}
