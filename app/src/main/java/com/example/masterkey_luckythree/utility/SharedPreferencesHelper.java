package com.example.masterkey_luckythree.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "MyPrefs";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Method to save a string value
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // Method to retrieve a string value
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // Method to remove a key-value pair
    public void remove(String key) {
        editor.remove(key);
        editor.apply();
    }

    // Method to clear all preferences
    public void clear() {
        editor.clear();
        editor.apply();
    }
}
