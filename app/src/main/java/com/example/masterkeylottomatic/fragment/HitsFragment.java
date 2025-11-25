package com.example.masterkeylottomatic.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.adapter.hits_adapter;
import com.example.masterkeylottomatic.helper.Account;
import com.example.masterkeylottomatic.helper.ConSQL;
import com.example.masterkeylottomatic.helper.OnDateChangedListener;
import com.example.masterkeylottomatic.item.hits_Item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HitsFragment extends Fragment implements OnDateChangedListener {

    private TextView result2pm, result5pm, result9pm, hitsresult, emptyView;
    private String Name = "", Result = "";
    private FloatingActionButton scanBtn;
    private SwipeRefreshLayout refresh;
    private ProgressBar progressBar; // Add progress bar reference
    private String currentDate;

    private RecyclerView HitsList;
    private hits_adapter adapter;
    private List<hits_Item> hitsItemList = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isAdapterInitialized = false;

    @Override
    public void onDateChanged(String selectedDate) {
        this.currentDate = selectedDate;
        refreshData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hits, container, false);

        emptyView = view.findViewById(R.id.emptyView);
        HitsList = view.findViewById(R.id.hitsList);
        progressBar = view.findViewById(R.id.progressBar); // Initialize progress bar
        refresh = view.findViewById(R.id.swipeRefreshLayout); // Initialize swipe refresh

        HitsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new hits_adapter(hitsItemList);
        HitsList.setAdapter(adapter);

        // Set up swipe refresh listener
        refresh.setOnRefreshListener(() -> {
            refreshData();
        });

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIfEmpty();
            }
        });

        // Show loading initially
        showLoading(true);

        if (currentDate != null) {
            refreshData();
        } else {
            // Set default date to today if not set yet
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            currentDate = sdf.format(new Date());
            refreshData();
        }

        isAdapterInitialized = true;

        if (currentDate != null) {
            refreshData();
        } else {
            // Set default date to today if not set yet
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            currentDate = sdf.format(new Date());
            refreshData();
        }

        return view;
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            HitsList.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            refresh.setRefreshing(false); // Stop swipe refresh animation
        }
    }

    private void checkIfEmpty() {
        if (adapter.getItemCount() == 0) {
            HitsList.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            HitsList.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void refreshData() {
        // Check if adapter is initialized before using it
        if (!isAdapterInitialized) {
            return; // Skip if adapter is not ready yet
        }

        // Clear existing data
        hitsItemList.clear();
        adapter.notifyDataSetChanged();

        // Fetch new data with currentDate
        fetchWinningCode(currentDate);
    }

    private void fetchWinningCode(String date) {
        Account account = Account.getInstance(requireContext());
        boolean isSuperUser = account.isSuperUser();
        String userGroup = account.getGroup();

        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {

                    String query;
                    if (isSuperUser) {
                        query = "SELECT ID, transcode, combo, bets, prize, game, draw, agent FROM EntryTB WHERE combo = result AND CAST([date] AS DATE) = ? AND [group] = ? ORDER BY ID DESC";
                    } else {
                        query = "SELECT ID, transcode, combo, bets, prize, game, draw, agent FROM EntryTB WHERE combo = result AND CAST([date] AS DATE) = ? AND [group] = ? ORDER BY ID DESC";
                    }

                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, currentDate);
                        if (!isSuperUser) {
                            preparedStatement.setString(2, userGroup);
                        }
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {

                            while (resultSet.next()) {
                                String ID = resultSet.getString("ID");
                                String trans = resultSet.getString("transcode");
                                String game = resultSet.getString("game");
                                String combo = resultSet.getString("combo");
                                String agent = resultSet.getString("agent");
                                String draw = resultSet.getString("draw");
                                BigDecimal bets = resultSet.getBigDecimal("bets");
                                BigDecimal prize = resultSet.getBigDecimal("prize");

                                DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
                                String Bets = decimalFormat.format(bets);
                                String Prize = decimalFormat.format(prize);

                                final String finalID = ID;
                                final String finaltrans = trans;
                                final String finalgame = game;
                                final String finalcombo = combo;
                                final String finalagent = agent;
                                final String finaldraw = draw;
                                final String finalbets = Bets;
                                final String finalprize = Prize;

                                if (getActivity() == null || !isAdded()) return;

                                requireActivity().runOnUiThread(() -> {
                                    hits_Item Item = new hits_Item(finalID, finalagent, finalcombo, finalbets, finalprize, finaltrans, finalgame, finaldraw);
                                    adapter.addItem(Item);
                                });
                            }

                        }
                    }
                }
            } catch (SQLException e) {
                Log.e("Error:", e.getMessage());

                // Show error on UI thread
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            } finally {
                // Hide loading indicator on UI thread
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    checkIfEmpty(); // Check if data is empty after loading
                });
            }
        });
    }
}
