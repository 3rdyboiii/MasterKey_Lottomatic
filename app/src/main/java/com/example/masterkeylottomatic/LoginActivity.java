package com.example.masterkeylottomatic;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.example.masterkeylottomatic.activity.MainActivity;
import com.example.masterkeylottomatic.helper.Account;
import com.example.masterkeylottomatic.helper.AppVersion;
import com.example.masterkeylottomatic.helper.ConSQL;
import com.example.masterkeylottomatic.utility.NetworkChangeListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private EditText user;
    private EditText pass;
    private TextView vers;
    private CheckBox rememberMe;
    private SharedPreferences sharedPreferences;
    private View progressLayout;
    private TextView progressMessage;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(networkChangeListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        user = findViewById(R.id.usernameTxt);
        pass = findViewById(R.id.passwordTxt);
        vers = findViewById(R.id.versionTxt);
        progressLayout = findViewById(R.id.progressLayout);
        progressMessage = findViewById(R.id.progressMessage);
        Button login = findViewById(R.id.loginBtn);
        rememberMe = findViewById(R.id.rememberMeCheckBox);

        sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        checkRememberedCredentials();

        login.setOnClickListener(this::login_Click);
        vers.setText("v" + AppVersion.VERSION_NAME);

        checkForUpdate();
    }
    private void checkRememberedCredentials() {
        String savedUsername = sharedPreferences.getString("username", null);
        String savedPassword = sharedPreferences.getString("password", null);
        boolean isRemembered = sharedPreferences.getBoolean("rememberMe", false);

        if (isRemembered) {
            user.setText(savedUsername);
            pass.setText(savedPassword);
            rememberMe.setChecked(true);
        }
    }
    private void login_Click(View view) {
        if (user.getText().toString().isEmpty()) {
            user.setError("Username is required");
            return;
        }
        if (pass.getText().toString().isEmpty()) {
            pass.setError("Password is required");
            return;
        }

        String username = user.getText().toString();
        String password = pass.getText().toString();

        showProgress();

        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    String query = "SELECT username, password, super, [group] FROM AdminTB WHERE username = ? AND password = ? AND [group] = 'BICOL'";
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, username);
                        preparedStatement.setString(2, password);
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                int superUser = resultSet.getInt("super");
                                String group = resultSet.getString("group");

                                Account account = Account.getInstance(this);
                                account.setGroup(group);
                                account.setSuperUser(superUser == 1);

                                runOnUiThread(() -> {
                                    if (rememberMe.isChecked()) {
                                        saveCredentials(username, password, true);
                                    } else {
                                        clearCredentials();
                                        pass.setText("");
                                    }

                                    // Hide ProgressBar once login is successful
                                    hideProgress();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("isSuperUser", superUser == 1);
                                    startActivity(intent);
                                });
                            } else {
                                runOnUiThread(() -> {
                                    user.setError("Invalid username or password");
                                    pass.setText("");
                                    // Hide ProgressBar if login fails
                                    hideProgress();
                                });
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Hide ProgressBar if there's an error
                    hideProgress();
                });
            }
        });
    }

    private void checkForUpdate() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/3rdyboiii/MasterKey_Lottomatic/main/updates/latest_version.json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                int latestVersionCode = json.getInt("versionCode");
                String apkUrl = json.getString("apkUrl");

                if (latestVersionCode > BuildConfig.VERSION_CODE) {
                    runOnUiThread(() -> promptUpdate(apkUrl));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void promptUpdate(String apkUrl) {
        new AlertDialog.Builder(this)
                .setTitle("Update Required")
                .setMessage("A new version is available. You must update to continue.")
                .setCancelable(false) // can't dismiss
                .setPositiveButton("Update Now", (dialog, which) -> downloadAndInstallApk(apkUrl))
                .show();
    }

    private void downloadAndInstallApk(String apkUrl) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle("Downloading Update");
        request.setDescription("Please wait...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "app-latest.apk");

        DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        long downloadId = manager.enqueue(request);

        // Create a progress dialog
        AlertDialog progressDialog = new AlertDialog.Builder(this)
                .setTitle("Downloading Update")
                .setMessage("0%")
                .setCancelable(false)
                .show();

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    Uri apkUri = manager.getUriForDownloadedFile(downloadId);
                    installApk(apkUri);
                    unregisterReceiver(this);
                    progressDialog.dismiss();
                }
            }
        };

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        androidx.core.content.ContextCompat.registerReceiver(
                this,
                onComplete,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
        );

        // Optional: poll progress
        new Thread(() -> {
            boolean downloading = true;
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);
            while (downloading) {
                Cursor cursor = manager.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    int bytesDownloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytesTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    if (bytesTotal > 0) {
                        int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
                        runOnUiThread(() -> progressDialog.setMessage(progress + "%"));
                    }
                    int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                        downloading = false;
                    }
                    cursor.close();
                }
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void installApk(Uri apkUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void showProgress() {
        progressMessage.setText("Logging in...");
        progressLayout.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progressLayout.setVisibility(View.GONE);
    }
    private void saveCredentials(String username, String password, boolean rememberMeChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putBoolean("rememberMe", rememberMeChecked);
        editor.apply();
    }

    private void clearCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("username");
        editor.remove("password");
        editor.remove("rememberMe");
        editor.apply();
    }
}