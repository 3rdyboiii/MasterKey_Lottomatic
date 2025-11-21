package com.example.masterkey_luckythree.fragment;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.adapter.ViewPagerAdapter;
import com.example.masterkey_luckythree.adapter.game_adapter;
import com.example.masterkey_luckythree.adapter.home_adapter;
import com.example.masterkey_luckythree.helper.Account;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.item.entry_item;
import com.example.masterkey_luckythree.item.game_item;
import com.example.masterkey_luckythree.item.home_item;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private game_adapter adapter;
    private List<game_item> itemList;
    private View loading;
    private TextView firstdraw, seconddraw, thirddraw, hits, gross, group;
    private RecyclerView DisplayList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    private TextView dateText;
    private ImageView calendarButton;
    private SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private SimpleDateFormat queryFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Date currentSelectedDate = new Date();

    private Connection connection;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        DisplayList = view.findViewById(R.id.displayList);
        group = view.findViewById(R.id.groupTxt);

        Account account = Account.getInstance(requireContext());
        group.setText("•    " + account.getGroup() + "    •");

        initializeViews(view);
        setupDateSelection();
        setupRefreshLayout();
        getActivity().setTitle("Home");
        refreshData();
        return view;
    }

    private void initializeViews(View view) {
        DisplayList = view.findViewById(R.id.displayList);
        loading = view.findViewById(R.id.loadingAnimation);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        firstdraw = view.findViewById(R.id.firstdrawTxt);
        seconddraw = view.findViewById(R.id.seconddrawTxt);
        thirddraw = view.findViewById(R.id.thirddrawTxt);
        hits = view.findViewById(R.id.hitsTxt);
        gross = view.findViewById(R.id.grossTxt);

        dateText = view.findViewById(R.id.dateTxt);
        calendarButton = view.findViewById(R.id.calendarBtn);

        itemList = new ArrayList<>();
        adapter = new game_adapter(itemList);
        DisplayList.setLayoutManager(new LinearLayoutManager(getContext()));
        DisplayList.setAdapter(adapter);
    }

    private void setupDateSelection() {
        updateDateDisplay();
        calendarButton.setOnClickListener(v -> showDatePicker());
        dateText.setOnClickListener(v -> showDatePicker());
    }

    private void setupRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    private void refreshData() {
        swipeRefreshLayout.setRefreshing(true);

        firstdraw.setText("₱ 0.00");
        seconddraw.setText("₱ 0.00");
        thirddraw.setText("₱ 0.00");
        hits.setText("₱ 0.00");
        gross.setText("₱ 0.00");

        fetchTotal();
        fetchTotalPerGame();
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentSelectedDate);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    Date selectedDate = selectedCalendar.getTime();

                    if (isDateValid(selectedDate)) {
                        currentSelectedDate = selectedDate;
                        updateDateDisplay();
                        refreshData();
                    } else {
                        Toast.makeText(requireContext(),
                                "Please select a valid date",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        dateText.setText(displayFormat.format(currentSelectedDate));
    }

    private String getSelectedDateForQuery() {
        return queryFormat.format(currentSelectedDate);
    }

    private boolean isDateValid(Date date) {
        return !date.after(new Date());
    }

    private void fetchTotal() {
        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    Account account = Account.getInstance(requireContext());
                    boolean isSuperUser = account.isSuperUser();
                    String userGroup = account.getGroup();

                    String query;
                    if (isSuperUser) {
                        query = "SELECT SUM(CASE WHEN draw = '2pm' THEN bets ELSE 0 END) AS 'first', " +
                                "SUM(CASE WHEN draw = '5pm' THEN bets ELSE 0 END) AS 'second', " +
                                "SUM(CASE WHEN draw = '9pm' THEN bets ELSE 0 END) AS 'third', " +
                                "SUM(CASE WHEN combo = result THEN prize ELSE 0 END) AS 'Hits', " +
                                "SUM(bets) AS 'total' FROM EntryTB WHERE CAST([date] AS DATE) = ? " +
                                "AND [group] IN ('COTABATO', 'COTABATO-2', 'COTABATO-3', 'MAGUINDANAO', 'LANAO')";
                    } else {
                        query = "SELECT SUM(CASE WHEN draw = '2pm' THEN bets ELSE 0 END) AS 'first', " +
                                "SUM(CASE WHEN draw = '5pm' THEN bets ELSE 0 END) AS 'second', " +
                                "SUM(CASE WHEN draw = '9pm' THEN bets ELSE 0 END) AS 'third', " +
                                "SUM(CASE WHEN combo = result THEN prize ELSE 0 END) AS 'Hits', " +
                                "SUM(bets) AS 'total' FROM EntryTB WHERE CAST([date] AS DATE) = ? " +
                                "AND [group] = ?";
                    }
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, getSelectedDateForQuery());
                        if (!isSuperUser) {
                            preparedStatement.setString(2, userGroup);
                        }
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                Double totalAmount = resultSet.getDouble("total");
                                String first = resultSet.getString("first") != null ? resultSet.getString("first") : "0.00";
                                String second = resultSet.getString("second") != null ? resultSet.getString("second") : "0.00";
                                String third = resultSet.getString("third") != null ? resultSet.getString("third") : "0.00";
                                String hitsAmount = resultSet.getString("Hits") != null ? resultSet.getString("Hits") : "0.00";

                                DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
                                String total = decimalFormat.format(totalAmount);

                                if (getActivity() == null || !isAdded()) return;

                                requireActivity().runOnUiThread(() -> {
                                    firstdraw.setText("₱" + first);
                                    seconddraw.setText("₱" + second);
                                    thirddraw.setText("₱" + third);
                                    hits.setText("₱" + hitsAmount);
                                    gross.setText("₱" + total);
                                    checkAndStopRefresh();
                                });
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                handleDatabaseError(e);
            }
        });
    }

    private void fetchTotalPerGame() {
        if (!isAdded() || getActivity() == null) {
            return; // Fragment not attached, don't proceed
        }
        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    Account account = Account.getInstance(requireContext());
                    boolean isSuperUser = account.isSuperUser();
                    String userGroup = account.getGroup();

                    // Determine game types based on day of week
                    android.icu.text.SimpleDateFormat sdf = new android.icu.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date date = sdf.parse(getSelectedDateForQuery());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    String game2D = (dayOfWeek == Calendar.SUNDAY) ? "S2" : "L2";
                    String game3D = (dayOfWeek == Calendar.SUNDAY) ? "TS3" : "TL3";

                    // Build the query based on user type
                    String query;
                    if (isSuperUser) {
                        query = "SELECT " +
                                "SUM(CASE WHEN game = ? THEN bets ELSE 0 END) AS 'total_2d', " +
                                "SUM(CASE WHEN game = ? AND combo = result THEN prize ELSE 0 END) AS 'hits_2d', " +
                                "SUM(CASE WHEN game = ? THEN bets ELSE 0 END) AS 'total_3d', " +
                                "SUM(CASE WHEN game = ? AND combo = result THEN prize ELSE 0 END) AS 'hits_3d', " +
                                "SUM(CASE WHEN game = '4D' THEN bets ELSE 0 END) AS 'total_4d', " +
                                "SUM(CASE WHEN game = '4D' AND combo = result THEN prize ELSE 0 END) AS 'hits_4d', " +

                                "SUM(CASE WHEN game = ? AND draw = '2pm' THEN bets ELSE 0 END) AS '2pm_2d', " +
                                "SUM(CASE WHEN game = ? AND draw = '5pm' THEN bets ELSE 0 END) AS '5pm_2d', " +
                                "SUM(CASE WHEN game = ? AND draw = '9pm' THEN bets ELSE 0 END) AS '9pm_2d', " +

                                "SUM(CASE WHEN game = ? AND draw = '2pm' THEN bets ELSE 0 END) AS '2pm_3d', " +
                                "SUM(CASE WHEN game = ? AND draw = '5pm' THEN bets ELSE 0 END) AS '5pm_3d', " +
                                "SUM(CASE WHEN game = ? AND draw = '9pm' THEN bets ELSE 0 END) AS '9pm_3d', " +

                                "SUM(CASE WHEN game = '4D' AND draw = '9pm' THEN bets ELSE 0 END) AS '9pm_4d', " +

                                "SUM(CASE WHEN game = ? AND draw = '2pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_2d_2pm', " +
                                "SUM(CASE WHEN game = ? AND draw = '5pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_2d_5pm', " +
                                "SUM(CASE WHEN game = ? AND draw = '9pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_2d_9pm', " +

                                "SUM(CASE WHEN game = ? AND draw = '2pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_3d_2pm', " +
                                "SUM(CASE WHEN game = ? AND draw = '5pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_3d_5pm', " +
                                "SUM(CASE WHEN game = ? AND draw = '9pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_3d_9pm', " +

                                "SUM(CASE WHEN game = '4D' AND draw = '9pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_4d_9pm' " +

                                "FROM EntryTB WHERE CAST([date] AS DATE) = ? AND [group] IN ('COTABATO', 'COTABATO-2', 'COTABATO-3', 'MAGUINDANAO', 'LANAO')";
                    } else {
                        query = "SELECT " +
                                "SUM(CASE WHEN game = ? THEN bets ELSE 0 END) AS 'total_2d', " +
                                "SUM(CASE WHEN game = ? AND combo = result THEN prize ELSE 0 END) AS 'hits_2d', " +
                                "SUM(CASE WHEN game = ? THEN bets ELSE 0 END) AS 'total_3d', " +
                                "SUM(CASE WHEN game = ? AND combo = result THEN prize ELSE 0 END) AS 'hits_3d', " +
                                "SUM(CASE WHEN game = '4D' THEN bets ELSE 0 END) AS 'total_4d', " +
                                "SUM(CASE WHEN game = '4D' AND combo = result THEN prize ELSE 0 END) AS 'hits_4d', " +

                                "SUM(CASE WHEN game = ? AND draw = '2pm' THEN bets ELSE 0 END) AS '2pm_2d', " +
                                "SUM(CASE WHEN game = ? AND draw = '5pm' THEN bets ELSE 0 END) AS '5pm_2d', " +
                                "SUM(CASE WHEN game = ? AND draw = '9pm' THEN bets ELSE 0 END) AS '9pm_2d', " +

                                "SUM(CASE WHEN game = ? AND draw = '2pm' THEN bets ELSE 0 END) AS '2pm_3d', " +
                                "SUM(CASE WHEN game = ? AND draw = '5pm' THEN bets ELSE 0 END) AS '5pm_3d', " +
                                "SUM(CASE WHEN game = ? AND draw = '9pm' THEN bets ELSE 0 END) AS '9pm_3d', " +

                                "SUM(CASE WHEN game = '4D' AND draw = '9pm' THEN bets ELSE 0 END) AS '9pm_4d', " +

                                "SUM(CASE WHEN game = ? AND draw = '2pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_2d_2pm', " +
                                "SUM(CASE WHEN game = ? AND draw = '5pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_2d_5pm', " +
                                "SUM(CASE WHEN game = ? AND draw = '9pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_2d_9pm', " +

                                "SUM(CASE WHEN game = ? AND draw = '2pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_3d_2pm', " +
                                "SUM(CASE WHEN game = ? AND draw = '5pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_3d_5pm', " +
                                "SUM(CASE WHEN game = ? AND draw = '9pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_3d_9pm', " +

                                "SUM(CASE WHEN game = '4D' AND draw = '9pm' AND combo = result THEN prize ELSE 0 END) AS 'hits_4d_9pm' " +

                                "FROM EntryTB WHERE CAST([date] AS DATE) = ? AND [group] = ?";
                    }

                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        // Set parameters for game types (2D and 3D)
                        preparedStatement.setString(1, game2D);
                        preparedStatement.setString(2, game2D);
                        preparedStatement.setString(3, game3D);
                        preparedStatement.setString(4, game3D);

                        // Set parameters for draw-specific bets (2D and 3D)
                        preparedStatement.setString(5, game2D);
                        preparedStatement.setString(6, game2D);
                        preparedStatement.setString(7, game2D);
                        preparedStatement.setString(8, game3D);
                        preparedStatement.setString(9, game3D);
                        preparedStatement.setString(10, game3D);

                        // Set parameters for hits per draw (2D and 3D)
                        preparedStatement.setString(11, game2D);
                        preparedStatement.setString(12, game2D);
                        preparedStatement.setString(13, game2D);
                        preparedStatement.setString(14, game3D);
                        preparedStatement.setString(15, game3D);
                        preparedStatement.setString(16, game3D);

                        // Set the date parameter
                        preparedStatement.setString(17, getSelectedDateForQuery());

                        // For non-super users, add the group parameter
                        if (!isSuperUser) {
                            preparedStatement.setString(18, userGroup);
                        }

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            List<game_item> newItems = new ArrayList<>();

                            if (resultSet.next()) {
                                // 2D Game
                                newItems.add(new game_item(
                                        "L2 GAME",
                                        resultSet.getString("2pm_2d"),
                                        resultSet.getString("5pm_2d"),
                                        resultSet.getString("9pm_2d"),
                                        resultSet.getString("hits_2d_2pm"),
                                        resultSet.getString("hits_2d_5pm"),
                                        resultSet.getString("hits_2d_9pm"),
                                        resultSet.getString("total_2d")
                                ));

                                // 3D Game
                                newItems.add(new game_item(
                                        "3D GAME",
                                        resultSet.getString("2pm_3d"),
                                        resultSet.getString("5pm_3d"),
                                        resultSet.getString("9pm_3d"),
                                        resultSet.getString("hits_3d_2pm"),
                                        resultSet.getString("hits_3d_5pm"),
                                        resultSet.getString("hits_3d_9pm"),
                                        resultSet.getString("total_3d")
                                ));

                                // 4D Game (only 9pm)
                                newItems.add(new game_item(
                                        "4D GAME",
                                        "0.00",  // 2pm
                                        "0.00",  // 5pm
                                        resultSet.getString("9pm_4d"),
                                        "0.00",  // hits 2pm
                                        "0.00",  // hits 5pm
                                        resultSet.getString("hits_4d_9pm"),
                                        resultSet.getString("total_4d")
                                ));
                            }

                            // Update UI
                            if (isAdded() && getActivity() != null) {
                                requireActivity().runOnUiThread(() -> {
                                    if (isAdded()) { // Double-check
                                        itemList.clear();
                                        itemList.addAll(newItems);
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                Log.e("DatabaseError", "Error fetching game data", e);
                if (getActivity() != null && isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error loading game data", Toast.LENGTH_SHORT).show());
                }
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void checkAndStopRefresh() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void handleDatabaseError(SQLException e) {
        Log.e("DatabaseError", "Date: " + getSelectedDateForQuery(), e);
        try {
            if (connection != null) {
                connection.rollback();
            }
        } catch (SQLException rollbackEx) {
            Log.e("RollbackError", rollbackEx.getMessage());
        }

        requireActivity().runOnUiThread(() -> {
            Toast.makeText(requireContext(),
                    "Failed to load data for " + displayFormat.format(currentSelectedDate),
                    Toast.LENGTH_SHORT).show();

            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}