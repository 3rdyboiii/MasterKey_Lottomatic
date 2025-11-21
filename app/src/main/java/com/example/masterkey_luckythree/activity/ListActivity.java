package com.example.masterkey_luckythree.activity;

import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.utility.NetworkChangeListener;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListActivity extends AppCompatActivity {
    private TextView comboTxt, betsTxt, gameTxt, nameTxt;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private SwipeRefreshLayout swipeRefreshLayout;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
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
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        comboTxt = findViewById(R.id.comboTxt);
        betsTxt = findViewById(R.id.betsTxt);
        gameTxt = findViewById(R.id.gameTxt);
        nameTxt = findViewById(R.id.nameTxt);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshMainLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchdata();
            }
        });
        fetchdata();
    }
    private void fetchdata() {
        swipeRefreshLayout.setRefreshing(true);
        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String currentDate = sdf.format(new Date());
                    String query = "SELECT combo, bets, game, agent, transcode, Type2 FROM BetsTB WHERE CAST([date] AS DATE) = '" + currentDate + "' ORDER BY ID DESC";
                    try (Statement smt = connection.createStatement();
                         ResultSet set = smt.executeQuery(query)) {
                             SpannableStringBuilder comboBuilder = new SpannableStringBuilder();
                             SpannableStringBuilder betsBuilder = new SpannableStringBuilder();
                             StringBuilder gameBuilder = new StringBuilder();
                             SpannableStringBuilder nameBuilder = new SpannableStringBuilder();

                            while (set.next()) {
                                String combo = set.getString("combo");
                                BigDecimal bets = set.getBigDecimal("bets");
                                String game = set.getString("game");
                                String name = set.getString("agent");
                                String type = set.getString("Type2");

                                if (name != null && name.length() > 10) {
                                    name = name.substring(0, 10) + "...";
                                }

                                if (type == null) {
                                    gameBuilder.append(game).append("\n");
                                } else {
                                    gameBuilder.append(game + "-" + type).append("\n");
                                }

                                if (name == null) {
                                    SpannableString redName = new SpannableString("NO NAME\n");
                                    SpannableString redCombo = new SpannableString(combo + "\n");
                                    SpannableString redBets = new SpannableString(bets.toString() + "\n");
                                    redName.setSpan(new ForegroundColorSpan(Color.RED), 0, redName.length(), 0);
                                    redCombo.setSpan(new ForegroundColorSpan(Color.RED), 0, redCombo.length(), 0);
                                    redBets.setSpan(new ForegroundColorSpan(Color.RED), 0, redBets.length(), 0);
                                    nameBuilder.append(redName);
                                    comboBuilder.append(redCombo);
                                    betsBuilder.append(redBets);
                                } else {
                                    nameBuilder.append(name).append("\n");
                                    comboBuilder.append(combo).append("\n");
                                    betsBuilder.append(bets.toString()).append("\n");
                                }
                            }

                            runOnUiThread(() -> {
                            comboTxt.setText(comboBuilder);
                            betsTxt.setText(betsBuilder);
                            gameTxt.setText(gameBuilder.toString());
                            nameTxt.setText(nameBuilder);
                            });
                        }
                    }
                } catch (SQLException e) {
                Log.e("Error:", e.getMessage());
            }
            runOnUiThread(() -> {
                swipeRefreshLayout.setRefreshing(false);
            });
        });
    }
}