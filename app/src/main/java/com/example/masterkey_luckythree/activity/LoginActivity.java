package com.example.masterkey_luckythree.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.helper.Account;
import com.example.masterkey_luckythree.helper.AppVersion;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.utility.NetworkChangeListener;

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
                    String query = "SELECT username, password, super, [group] FROM AdminTB WHERE username = ? AND password = ?";
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