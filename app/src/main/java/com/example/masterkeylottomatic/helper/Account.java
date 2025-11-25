package com.example.masterkeylottomatic.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Account {
    private static Account instance;
    private String username;
    private String name;
    private String phone;
    private String group;
    private boolean isSuperUser;
    private static final String PREFS_NAME = "account_prefs";
    private static final String KEY_NAME = "name";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_GROUP = "group";
    private SharedPreferences sharedPreferences;

    private Account(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        name = sharedPreferences.getString(KEY_NAME, null);
        username = sharedPreferences.getString(KEY_USERNAME, null);
        phone = sharedPreferences.getString(KEY_PHONE, null);
        group = sharedPreferences.getString(KEY_GROUP, null);
    }

    public static synchronized Account getInstance(Context context) {
        if (instance == null) {
            instance = new Account(context);
        }
        return instance;
    }

    public String getUsername() { return  username; }
    public void setUsername(String username) {
        this.username = username;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USERNAME, username);
        editor.apply();

        Log.d("Account", "Username saved to SharedPreferences: " + username);
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_NAME, name);
        editor.apply();

        Log.d("Account", "Name saved to SharedPreferences: " + name);
    }

    public String getPhone() {return phone; }
    public void setPhone(String phone) {
        this.phone = phone;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_PHONE, phone);
        editor.apply();

        Log.d("Account", "Phone saved to SharedPreferences: " + phone);
    }

    public String getGroup() { return group; }
    public void setGroup(String group) {
        this.group = group;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_GROUP, group);
        editor.apply();
    }

    public boolean isSuperUser() {
        return isSuperUser;
    }
    public void setSuperUser(boolean superUser) {
        isSuperUser = superUser;
    }
}
