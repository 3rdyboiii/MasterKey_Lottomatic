package com.example.masterkey_luckythree.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class PrintDataManager {
    private static final String PREF_NAME = "PrintDataPrefs";
    private static final String KEY_PRINT_DATA = "printData";

    public static void savePrintData(Context context, String data) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PRINT_DATA, data);
        editor.apply();
    }

    public static String getSavedPrintData(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_PRINT_DATA, null);
    }

    public static void clearPrintData(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("PrintDataPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("printData");
        editor.apply();
    }
}
