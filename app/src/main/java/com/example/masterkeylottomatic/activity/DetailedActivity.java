package com.example.masterkeylottomatic.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.adapter.detail_adapter;
import com.example.masterkeylottomatic.helper.ConSQL;
import com.example.masterkeylottomatic.item.detail_Item;
import com.example.masterkeylottomatic.utility.NetworkChangeListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetailedActivity extends AppCompatActivity {

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Navigate back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

    RecyclerView DisplayList;
    private detail_adapter adapter;
    private List<detail_Item> itemList;

    private View loading;

    Connection connection;
    private TextView player, code, amount, startDateTextView, endDateTextView;
    private String username = "", referral = "", startDate = "", endDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        player = findViewById(R.id.playerTxt);
        code = findViewById(R.id.codeTxt);
        amount = findViewById(R.id.amountTxt);
        startDateTextView = findViewById(R.id.dateFromTxt);
        startDateTextView.setOnClickListener(v -> showDatePicker(true));
        endDateTextView = findViewById(R.id.dateToTxt);
        endDateTextView.setOnClickListener(v -> showDatePicker(false));
        loading = findViewById(R.id.loadingAnimation);
        DisplayList = findViewById(R.id.displayList);
        itemList = new ArrayList<>();
        adapter = new detail_adapter(itemList);
        DisplayList.setLayoutManager(new LinearLayoutManager(this));
        DisplayList.setAdapter(adapter);

        username = getIntent().getStringExtra("username");
        referral = getIntent().getStringExtra("referral");

        player.setText(username);
        code.setText(referral);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Referral Tree");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        startDate = sdf.format(new Date());
        startDateTextView.setText("Start: " + startDate);

        fetchreferred(startDate, "");
        fetchTotalGross(startDate, "");
    }
    private void showDatePicker(boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String formattedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;

            if (isStartDate) {
                startDate = formattedDate;
                startDateTextView.setText("Start: " + formattedDate);
                endDate = ""; // Reset endDate if choosing a new start date
                endDateTextView.setText("Select End Date (Optional)");
            } else {
                endDate = formattedDate;
                endDateTextView.setText("End: " + formattedDate);
            }

            // Fetch data after selecting the date(s)
            fetchreferred(startDate, endDate);
            fetchTotalGross(startDate, endDate);

        }, year, month, day);


        

        // Disable future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Show DatePickerDialog
        datePickerDialog.show();
    }
    private void fetchTotalGross(String start, String end) {
        executorService.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    String query = "SELECT SUM(b.bets) as total FROM BetsTB b " +
                            "LEFT JOIN PlayerTB p ON b.username = p.username " +
                            "AND (b.date IS NULL OR CAST(b.date AS DATE) BETWEEN ? AND ?) " +
                            "WHERE p.referredBy = ?";

                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, start);
                        preparedStatement.setString(2, end.isEmpty() ? start : end);
                        preparedStatement.setString(3, referral);

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                String total = resultSet.getString("total");

                                runOnUiThread(() -> {
                                    amount.setText(total);
                                });
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                Log.e("Error:", e.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to fetch data. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void fetchreferred(String start, String end) {
        Dialog loadingDialog = new Dialog(this, R.style.DialogTheme);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.fetching_bets);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false);

        // Show loading dialog
        runOnUiThread(loadingDialog::show);

        itemList.clear();
        executorService.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    // ✅ Adjusted Query to fetch all referred users, even if they have no bets
                    String query = "SELECT p.username, p.name, p.coins, p.phone, p.referralCode, p.referredBy, " +
                            "COALESCE(SUM(b.bets), 0) AS totalBets " +
                            "FROM PlayerTB p " +
                            "LEFT JOIN BetsTB b ON p.username = b.username " +
                            "AND (b.date IS NULL OR CAST(b.date AS DATE) BETWEEN ? AND ?) " +
                            "WHERE p.referredBy = ? " +
                            "GROUP BY p.username, p.name, p.coins, p.phone, p.referralCode, p.referredBy " +
                            "ORDER BY p.username";

                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, start);
                        preparedStatement.setString(2, end.isEmpty() ? start : end); // Use start date if end date is empty
                        preparedStatement.setString(3, referral);

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                String username = resultSet.getString("username");
                                String name = resultSet.getString("name");
                                String code = resultSet.getString("coins");

                                detail_Item item = new detail_Item(username, name, code);
                                itemList.add(item);
                            }

                            runOnUiThread(() -> {
                                loadingDialog.dismiss();
                                adapter.notifyDataSetChanged();

                                if (itemList.isEmpty()) {
                                    DisplayList.setVisibility(View.GONE);
                                    loading.setVisibility(View.VISIBLE);
                                } else {
                                    DisplayList.setVisibility(View.VISIBLE);
                                    loading.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                Log.e("Error:", e.getMessage());
                runOnUiThread(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Failed to fetch data. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
