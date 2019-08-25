package com.jby.pricechecker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by user on 3/11/2018.
 */
public class DbUser extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Database";
    private static final int DATABASE_VERSION = 1;

    private static final String TB_USER = "tb_user";
    private static final String USER_ID = "user_id";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CREATED_AT = "created_at";
    private static final String UPDATED_AT = "updated_at";


    private static final String CREATE_TABLE_USER = "CREATE TABLE "+ TB_USER +
            "(" + USER_ID + " INTEGER PRIMARY KEY, " +
            USERNAME + " Text, " +
            PASSWORD + " Text, " +
            CREATED_AT + " Text, " +
            UPDATED_AT + " Text)";

    private String timeStamp;
    private Context context;

    public DbUser(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TB_USER);
        onCreate(sqLiteDatabase);
    }

    //create new user
    public void insertUser(String username, String password)
    {
        //initialize db
        SQLiteDatabase db = this.getWritableDatabase();
        timeStamp = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        //initialize content value
        ContentValues contentValues = new ContentValues();
        //setup the value that you want to store
        contentValues.put(USERNAME, username);
        contentValues.put(PASSWORD, password);
        contentValues.put(CREATED_AT, timeStamp);
        //perform insert action
        db.insert(TB_USER, null, contentValues);
    }

    public String login(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String userID = null;
        String sql = "SELECT " + USER_ID +
                " FROM "+ TB_USER +
                " WHERE " + USERNAME + " =? " + "AND " + PASSWORD + " =? ";

        Cursor crs = db.rawQuery(sql, new String[]{username, password});
        while (crs.moveToNext()) {
            userID = crs.getString(crs.getColumnIndex("user_id"));
            Log.d("haha", "haha: " + userID);
        }

        db.close();
        crs.close();
        return userID;
    }

    public boolean updateUser(String userID, String username, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(USERNAME, username);
        contentValues.put(PASSWORD, password);

        return db.update(TB_USER, contentValues, USER_ID + " = ?", new String[] { userID}) != -1;
    }

    public int countUser(){
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + USER_ID + " FROM " +TB_USER;
        Cursor cursor = db.rawQuery(sql, null);
        count = cursor.getCount();
        cursor.close();
        return count;
    }
}
