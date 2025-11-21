package com.example.masterkey_luckythree.activity;

import android.app.Dialog;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.masterkey_luckythree .R;
import com.example.masterkey_luckythree.helper.AppVersion;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.utility.NetworkChangeListener;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewAccount extends AppCompatActivity {

    private EditText userText, passText, nameText, codeText, versionText, groupText;
    private String selectedCode = "";
    private Spinner referredSpinner;
    private Button submit;
    Connection connection;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }
    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        userText = findViewById(R.id.usernameText);
        passText = findViewById(R.id.passwordText);
        nameText = findViewById(R.id.nameText);
        codeText = findViewById(R.id.codeText);
        groupText = findViewById(R.id.groupText);
        versionText = findViewById(R.id.versionText);

        submit = findViewById(R.id.submitButton);
        submit.setOnClickListener(v -> submitForm());
    }

    private void submitForm() {
        if (checkForm()) {
            String user = userText.getText().toString();
            String pass = passText.getText().toString();
            String name = nameText.getText().toString();
            String code = codeText.getText().toString();
            String group = groupText.getText().toString();
            String version = versionText.getText().toString();

            executor.execute(() -> {
                ConSQL c = new ConSQL();
                connection = c.conclass();
                if (connection != null) {
                    try {
                        // Check if username already exists
                        String checkQuery = "SELECT COUNT(*) FROM UserTB WHERE username = ? and code = ?";
                        PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                        checkStatement.setString(1, user);
                        checkStatement.setString(2, code);
                        ResultSet resultSet = checkStatement.executeQuery();

                        if (resultSet.next() && resultSet.getInt(1) > 0) {
                            runOnUiThread(() -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setTitle("Error");
                                builder.setMessage("Username/Code already exists. Please choose a different username/code.");
                                builder.setPositiveButton("OK", null);
                                builder.show();
                            });

                            resultSet.close();
                            checkStatement.close();
                            return; // Stop further execution
                        }

                        resultSet.close();
                        checkStatement.close();

                        // Insert the new account
                        String query = "INSERT INTO UserTB (username, password, name, code, version, [group]) VALUES (?, ?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, user);
                        preparedStatement.setString(2, pass);
                        preparedStatement.setString(3, name);
                        preparedStatement.setString(4, code);
                        preparedStatement.setString(5, version);
                        preparedStatement.setString(6, group);
                        preparedStatement.executeUpdate();

                        preparedStatement.close();

                        runOnUiThread(this::popupmessage); // Ensure UI updates run on the main thread
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Error");
                            builder.setMessage("Account creation failed due to an error.");
                            builder.setPositiveButton("OK", null);
                            builder.show();
                        });
                    }
                }
            });
        }
    }

    private boolean checkForm() {
        if (userText.getText().toString().isEmpty()) {
            userText.setError("Username is required");
            return false;
        }
        if (passText.getText().toString().isEmpty()) {
            passText.setError("Password is required");
            return false;
        }
        if (nameText.getText().toString().isEmpty()) {
            nameText.setError("Name is required");
            return false;
        }
        if (codeText.getText().toString().isEmpty()) {
            codeText.setError("Code number is required");
            return false;
        }
        if (versionText.getText().toString().isEmpty()) {
            versionText.setError("Version number is required");
            return false;
        }
        return true;
    }
    private String generateReferralCode() {
        // Using SecureRandom to generate a random alphanumeric code
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int codeLength = 8;  // Length of referral code
        StringBuilder referralCode = new StringBuilder(codeLength);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < codeLength; i++) {
            int randomIndex = random.nextInt(characters.length());
            referralCode.append(characters.charAt(randomIndex));
        }

        return referralCode.toString();
    }
    private void popupmessage() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove the title bar
        dialog.setContentView(R.layout.dialog_success); // Set your custom layout
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); // Make the background transparent
        dialog.setCancelable(false);

        TextView detailTxt = dialog.findViewById(R.id.detailsTxt);
        detailTxt.setText("Account Created!");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            finish();
        }, 2000);

        dialog.show();
    }
}