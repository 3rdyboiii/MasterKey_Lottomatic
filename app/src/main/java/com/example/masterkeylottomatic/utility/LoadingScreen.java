package com.example.masterkeylottomatic.utility;

import android.app.AlertDialog;
import android.content.Context;

public class LoadingScreen {

    private AlertDialog progressDialog;
    private final Context context;

    // Constructor to initialize the context
    public LoadingScreen(Context context) {
        this.context = context.getApplicationContext(); // Use application context to prevent memory leaks
    }
}
