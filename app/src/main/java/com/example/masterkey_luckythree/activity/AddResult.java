package com.example.masterkey_luckythree.activity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.helper.ConSQL;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddResult extends AppCompatActivity {

    private EditText result3DInput, result4DInput, result6DInput, Pick3Input, Last3DInput, Last4DInput, Last6DInput;
    private Button submit3DButton, submit4DButton, submit6DButton, submitPick3Button, submitLast3DButton, submitLast4DButton, submitLast6DButton, refreshButton;
    private RadioGroup radioGroup;
    private String draw = "", selectedOption = "", currentDate;
    Connection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_result);

        result3DInput = findViewById(R.id.result3DInput);
        result4DInput = findViewById(R.id.result4DInput);
        result6DInput = findViewById(R.id.result6DInput);
        Pick3Input = findViewById(R.id.Pick3Input);
        Last3DInput = findViewById(R.id.Last3DInput);
        Last4DInput = findViewById(R.id.Last4DInput);
        Last6DInput = findViewById(R.id.Last6DInput);
        submit3DButton = findViewById(R.id.submit3DButton);
        submit3DButton.setOnClickListener( v -> submit3Dresult());
        submit4DButton = findViewById(R.id.submit4DButton);
        submit4DButton.setOnClickListener( v -> submit4Dresult());
        submit6DButton = findViewById(R.id.submit6DButton);
        submit6DButton.setOnClickListener( v -> submit6Dresult());
        submitPick3Button = findViewById(R.id.submitPick3Button);
        submitPick3Button.setOnClickListener( v -> submitPick3result());
        submitLast3DButton = findViewById(R.id.submitLast3DButton);
        submitLast3DButton.setOnClickListener( v -> submitLast3Dresult());
        submitLast4DButton = findViewById(R.id.submitLast4DButton);
        submitLast4DButton.setOnClickListener( v -> submitLast4Dresult());
        submitLast6DButton = findViewById(R.id.submitLast6DButton);
        submitLast6DButton.setOnClickListener( v -> submitLast6Dresult());
        refreshButton = findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener( v -> fetchresult());
        radioGroup = findViewById(R.id.RadioGroup);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentDate = sdf.format(new Date());
    }
    private void clearInput() {
        result3DInput.setText("");
        result4DInput.setText("");
        result6DInput.setText("");
        Pick3Input.setText("");
        Last3DInput.setText("");
        Last4DInput.setText("");
        Last6DInput.setText("");
    }
    private void fetchresult() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a draw", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Fetching results...", Toast.LENGTH_LONG).show();
        clearInput();
        int selectedResult = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedResult);
        selectedOption = selectedRadioButton.getText().toString();
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        ConSQL c = new ConSQL();
        connection = c.conclass();
        if (connection != null) {
            fetchResultsByGame((dayOfWeek == Calendar.SUNDAY) ? "TS3" : "TL3", result3DInput);
            fetchResultsByGame("4D", result4DInput);
            fetchResultsByGame((dayOfWeek == Calendar.SUNDAY) ? "S2" : "L2", result6DInput);
        } else {
            Toast.makeText(this, "Database connection failed", Toast.LENGTH_LONG).show();
        }
    }

    private void fetchResultsByGame(String game, EditText resultTextView) {
        try {
            String query = "SELECT result FROM BetsTB WHERE game = ? AND draw = ? and CAST([date] as date) = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, game);
                stmt.setString(2, selectedOption);
                stmt.setString(3, currentDate);
                try (ResultSet set = stmt.executeQuery()) {
                    if (set.next()) {
                        String result = set.getString("result");
                        if (result == null) {
                            result = "";
                        }
                        resultTextView.setText(result);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching total bets for " + game + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void fetchLast2ResultsByGame(String game, EditText totalTextView, String Type) {
        try {
            String query = "SELECT result FROM BetsTB WHERE game = ? AND draw = ? AND Type2 = ? and CAST([date] as date) = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, game);
                stmt.setString(2, selectedOption);
                stmt.setString(3, Type);
                stmt.setString(4, currentDate);
                try (ResultSet set = stmt.executeQuery()) {
                    if (set.next()) {
                        String result = set.getString("result");
                        if (result == null) {
                            result = "";
                        }
                        totalTextView.setText(result);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error fetching total bets for " + game + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    @SuppressLint("StaticFieldLeak")
    private void submit3Dresult() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a draw", Toast.LENGTH_SHORT).show();
            return;
        }
        String result = result3DInput.getText().toString();
        int selectedResult = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedResult);
        String selectedOption = selectedRadioButton.getText().toString();

        new AsyncTask<Void, Void, Boolean>() {
            private AlertDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                AlertDialog.Builder builder = new AlertDialog.Builder(AddResult.this);
                builder.setMessage("Updating result...");
                progressDialog = builder.create();
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                ConSQL c = new ConSQL();
                Connection connection = c.conclass();
                if (connection != null) {
                    try {
                        String query = "UPDATE BetsTB SET Result = ? WHERE game = '3D' AND draw = ? and CAST([date] as date) = ?";
                        try (PreparedStatement ps = connection.prepareStatement(query)) {
                            ps.setString(1, result);
                            ps.setString(2, selectedOption);
                            ps.setString(3, currentDate);
                            int rowsAffected = ps.executeUpdate();
                            return rowsAffected > 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (success) {
                    Toast.makeText(AddResult.this, "Result updated successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddResult.this, "Failed to update result", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
    @SuppressLint("StaticFieldLeak")
    private void submit4Dresult() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a draw", Toast.LENGTH_SHORT).show();
            return;
        }
        String result = result4DInput.getText().toString();
        int selectedResult = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedResult);
        selectedOption = selectedRadioButton.getText().toString();

        new AsyncTask<Void, Void, Boolean>() {
            private AlertDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                AlertDialog.Builder builder = new AlertDialog.Builder(AddResult.this);
                builder.setMessage("Updating result...");
                progressDialog = builder.create();
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                ConSQL c = new ConSQL();
                Connection connection = c.conclass();
                if (connection != null) {
                    try {
                        String query = "UPDATE BetsTB SET Result = ? WHERE game = '4D' AND draw = ? and CAST([date] as date) = ?";
                        try (PreparedStatement ps = connection.prepareStatement(query)) {
                            ps.setString(1, result);
                            ps.setString(2, selectedOption);
                            ps.setString(3, currentDate);
                            int rowsAffected = ps.executeUpdate();
                            return rowsAffected > 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (success) {
                    Toast.makeText(AddResult.this, "Result updated successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddResult.this, "Failed to update result", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
    @SuppressLint("StaticFieldLeak")
    private void submit6Dresult() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a draw", Toast.LENGTH_SHORT).show();
            return;
        }
        String result = result6DInput.getText().toString();
        int selectedResult = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedResult);
        selectedOption = selectedRadioButton.getText().toString();

        new AsyncTask<Void, Void, Boolean>() {
            private AlertDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                AlertDialog.Builder builder = new AlertDialog.Builder(AddResult.this);
                builder.setMessage("Updating result...");
                progressDialog = builder.create();
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                ConSQL c = new ConSQL();
                Connection connection = c.conclass();
                if (connection != null) {
                    try {
                        String query = "UPDATE BetsTB SET Result = ? WHERE game = '6D' AND draw = ? and CAST([date] as date) = ?";
                        try (PreparedStatement ps = connection.prepareStatement(query)) {
                            ps.setString(1, result);
                            ps.setString(2, selectedOption);
                            ps.setString(3, currentDate);
                            int rowsAffected = ps.executeUpdate();
                            return rowsAffected > 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (success) {
                    Toast.makeText(AddResult.this, "Result updated successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddResult.this, "Failed to update result", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
    @SuppressLint("StaticFieldLeak")
    private void submitPick3result() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a draw", Toast.LENGTH_SHORT).show();
            return;
        }
        String result = Pick3Input.getText().toString();
        int selectedResult = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedResult);
        selectedOption = selectedRadioButton.getText().toString();

        new AsyncTask<Void, Void, Boolean>() {
            private AlertDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                AlertDialog.Builder builder = new AlertDialog.Builder(AddResult.this);
                builder.setMessage("Updating result...");
                progressDialog = builder.create();
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                ConSQL c = new ConSQL();
                Connection connection = c.conclass();
                if (connection != null) {
                    try {
                        String query = "UPDATE BetsTB SET Result = ? WHERE game = 'PICK3' AND draw = ? and CAST([date] as date) = ?";
                        try (PreparedStatement ps = connection.prepareStatement(query)) {
                            ps.setString(1, result);
                            ps.setString(2, selectedOption);
                            ps.setString(3, currentDate);
                            int rowsAffected = ps.executeUpdate();
                            return rowsAffected > 0;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (success) {
                    Toast.makeText(AddResult.this, "Result updated successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddResult.this, "Failed to update result", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
    @SuppressLint("StaticFieldLeak")
    private void submitLast3Dresult() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a draw", Toast.LENGTH_SHORT).show();
            return;
        }
        String result3D = Last3DInput.getText().toString();
        int selectedResult = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedResult);
        selectedOption = selectedRadioButton.getText().toString();

        new AsyncTask<Void, Void, Boolean>() {
            private AlertDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                AlertDialog.Builder builder = new AlertDialog.Builder(AddResult.this);
                builder.setMessage("Updating result...");
                progressDialog = builder.create();
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                ConSQL c = new ConSQL();
                Connection connection = c.conclass();
                if (connection != null) {
                    try {
                        String query = "UPDATE BetsTB SET result = ? WHERE game = 'LAST2' AND draw = ? AND Type2 = '3D' and CAST([date] as date) = ?";
                        PreparedStatement ps = connection.prepareStatement(query);
                        ps.setString(1, result3D);
                        ps.setString(2, selectedOption);
                        ps.setString(3, currentDate);
                        int rowsAffected = ps.executeUpdate();
                        return rowsAffected > 0;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (success) {
                    Toast.makeText(AddResult.this, "Result updated successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddResult.this, "Failed to update result", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
    @SuppressLint("StaticFieldLeak")
    private void submitLast4Dresult() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a draw", Toast.LENGTH_SHORT).show();
            return;
        }
        String result4D = Last4DInput.getText().toString();
        int selectedResult = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedResult);
        selectedOption = selectedRadioButton.getText().toString();

        new AsyncTask<Void, Void, Boolean>() {
            private AlertDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                AlertDialog.Builder builder = new AlertDialog.Builder(AddResult.this);
                builder.setMessage("Updating result...");
                progressDialog = builder.create();
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                ConSQL c = new ConSQL();
                Connection connection = c.conclass();
                if (connection != null) {
                    try {
                        String query = "UPDATE BetsTB SET result = ? WHERE game = 'LAST2' AND draw = ? AND Type2 = '4D' and CAST([date] as date) = ?";
                        PreparedStatement ps = connection.prepareStatement(query);
                        ps.setString(1, result4D);
                        ps.setString(2, selectedOption);
                        ps.setString(3, currentDate);
                        int rowsAffected = ps.executeUpdate();
                        return rowsAffected > 0;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (success) {
                    Toast.makeText(AddResult.this, "Result updated successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddResult.this, "Failed to update result", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
    @SuppressLint("StaticFieldLeak")
    private void submitLast6Dresult() {
        if (radioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select a draw", Toast.LENGTH_SHORT).show();
            return;
        }
        String result6D = Last6DInput.getText().toString();
        int selectedResult = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedResult);
        selectedOption = selectedRadioButton.getText().toString();

        new AsyncTask<Void, Void, Boolean>() {
            private AlertDialog progressDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                AlertDialog.Builder builder = new AlertDialog.Builder(AddResult.this);
                builder.setMessage("Updating result...");
                progressDialog = builder.create();
                progressDialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... voids) {
                ConSQL c = new ConSQL();
                Connection connection = c.conclass();
                if (connection != null) {
                    try {
                        String query = "UPDATE BetsTB SET result = ? WHERE game = 'LAST2' AND draw = ? AND Type2 = '6D' and CAST([date] as date) = ?";
                        PreparedStatement ps = connection.prepareStatement(query);
                        ps.setString(1, result6D);
                        ps.setString(2, selectedOption);
                        ps.setString(3, currentDate);
                        int rowsAffected = ps.executeUpdate();
                        return rowsAffected > 0;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                super.onPostExecute(success);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (success) {
                    Toast.makeText(AddResult.this, "Result updated successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddResult.this, "Failed to update result", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
}