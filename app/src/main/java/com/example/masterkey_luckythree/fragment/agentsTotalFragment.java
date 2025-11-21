package com.example.masterkey_luckythree.fragment;

import android.app.DatePickerDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.adapter.home_adapter;
import com.example.masterkey_luckythree.helper.Account;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.item.home_item;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class agentsTotalFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Shutdown the executor when fragment is destroyed
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    private RecyclerView DisplayList;
    private home_adapter adapter;
    private List<home_item> itemList;
    private List<home_item> filteredList;
    private ImageView calendarButton;
    private SearchView searchView;
    private Date currentSelectedDate = new Date();
    private SimpleDateFormat queryFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_agents_total, container, false);

        DisplayList = view.findViewById(R.id.displayList);
        calendarButton = view.findViewById(R.id.calendarBtn);
        searchView = view.findViewById(R.id.searchView);

        // Setup lists and adapter
        itemList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new home_adapter(getContext(), filteredList);

        DisplayList.setLayoutManager(new LinearLayoutManager(getActivity()));
        DisplayList.setAdapter(adapter);

        getActivity().setTitle("Agent Gross");

        // Setup functionality
        setupDateSelection();
        setupSearchView();
        fetchTotalPerAgent();

        return view;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAgents(newText);
                return true;
            }
        });
    }

    private void filterAgents(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(itemList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (home_item item : itemList) {
                if (item.getAgent().toLowerCase().contains(lowerCaseQuery) ||
                        item.getGroup().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupDateSelection() {
        calendarButton.setOnClickListener(v -> showDatePicker());
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
                        fetchTotalPerAgent(); // Refresh data with new date
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

    private boolean isDateValid(Date date) {
        return !date.after(new Date());
    }

    private String getSelectedDateForQuery() {
        return queryFormat.format(currentSelectedDate);
    }

    private void fetchTotalPerAgent() {
        if (!isAdded() || getActivity() == null) {
            return;
        }
        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date date = sdf.parse(getSelectedDateForQuery());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    Account account = Account.getInstance(requireContext());
                    boolean isSuperUser = account.isSuperUser();
                    String userGroup = account.getGroup();

                    // Determine game types based on day of week
                    String game2D = (dayOfWeek == Calendar.SUNDAY) ? "S2" : "L2";
                    String game3D = (dayOfWeek == Calendar.SUNDAY) ? "TS3" : "TL3";

                    // Enhanced query to get bets and hits per agent, per game, per draw
                    String query = "SELECT " +
                            "agent AS 'Agent', " +
                            "ISNULL([group], 'No Group') AS 'Group', " +

                            // 2D Bets per draw
                            "SUM(CASE WHEN game = ? AND draw = '2pm' THEN bets ELSE 0 END) AS 'first_2d', " +
                            "SUM(CASE WHEN game = ? AND draw = '5pm' THEN bets ELSE 0 END) AS 'second_2d', " +
                            "SUM(CASE WHEN game = ? AND draw = '9pm' THEN bets ELSE 0 END) AS 'third_2d', " +

                            // 2D Hits per draw
                            "SUM(CASE WHEN game = ? AND draw = '2pm' AND combo = result THEN prize ELSE 0 END) AS 'first_2d_hits', " +
                            "SUM(CASE WHEN game = ? AND draw = '5pm' AND combo = result THEN prize ELSE 0 END) AS 'second_2d_hits', " +
                            "SUM(CASE WHEN game = ? AND draw = '9pm' AND combo = result THEN prize ELSE 0 END) AS 'third_2d_hits', " +

                            // 3D Bets per draw
                            "SUM(CASE WHEN game = ? AND draw = '2pm' THEN bets ELSE 0 END) AS 'first_3d', " +
                            "SUM(CASE WHEN game = ? AND draw = '5pm' THEN bets ELSE 0 END) AS 'second_3d', " +
                            "SUM(CASE WHEN game = ? AND draw = '9pm' THEN bets ELSE 0 END) AS 'third_3d', " +

                            // 3D Hits per draw
                            "SUM(CASE WHEN game = ? AND draw = '2pm' AND combo = result THEN prize ELSE 0 END) AS 'first_3d_hits', " +
                            "SUM(CASE WHEN game = ? AND draw = '5pm' AND combo = result THEN prize ELSE 0 END) AS 'second_3d_hits', " +
                            "SUM(CASE WHEN game = ? AND draw = '9pm' AND combo = result THEN prize ELSE 0 END) AS 'third_3d_hits', " +

                            // 4D Bets and Hits (only 9pm)
                            "SUM(CASE WHEN game = '4D' AND draw = '9pm' THEN bets ELSE 0 END) AS 'third_4d', " +
                            "SUM(CASE WHEN game = '4D' AND draw = '9pm' AND combo = result THEN prize ELSE 0 END) AS 'third_4d_hits', " +

                            // Totals
                            "SUM(bets) AS 'total' " +

                            "FROM EntryTB WHERE CAST([date] AS DATE) = ? " +
                            (isSuperUser ? "AND [group] IN ('COTABATO', 'COTABATO-2', 'COTABATO-3', 'MAGUINDANAO', 'LANAO') " : "AND [group] = ? ") +
                            "GROUP BY agent, [group] ORDER BY agent";

                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        // Set game type parameters (2D and 3D)
                        preparedStatement.setString(1, game2D);
                        preparedStatement.setString(2, game2D);
                        preparedStatement.setString(3, game2D);
                        preparedStatement.setString(4, game2D);
                        preparedStatement.setString(5, game2D);
                        preparedStatement.setString(6, game2D);
                        preparedStatement.setString(7, game3D);
                        preparedStatement.setString(8, game3D);
                        preparedStatement.setString(9, game3D);
                        preparedStatement.setString(10, game3D);
                        preparedStatement.setString(11, game3D);
                        preparedStatement.setString(12, game3D);

                        // Set date parameter
                        preparedStatement.setString(13, getSelectedDateForQuery());

                        // Set group parameter for non-super users
                        if (!isSuperUser) {
                            preparedStatement.setString(14, userGroup);
                        }

                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            List<home_item> newItems = new ArrayList<>();
                            DecimalFormat df = new DecimalFormat("#,##0.00");

                            while (resultSet.next()) {
                                // Get all values from result set
                                String agent = resultSet.getString("Agent");
                                String group = resultSet.getString("Group");

                                // 2D values
                                BigDecimal first2D = resultSet.getBigDecimal("first_2d");
                                BigDecimal second2D = resultSet.getBigDecimal("second_2d");
                                BigDecimal third2D = resultSet.getBigDecimal("third_2d");
                                BigDecimal first2DHits = resultSet.getBigDecimal("first_2d_hits");
                                BigDecimal second2DHits = resultSet.getBigDecimal("second_2d_hits");
                                BigDecimal third2DHits = resultSet.getBigDecimal("third_2d_hits");

                                // 3D values
                                BigDecimal first3D = resultSet.getBigDecimal("first_3d");
                                BigDecimal second3D = resultSet.getBigDecimal("second_3d");
                                BigDecimal third3D = resultSet.getBigDecimal("third_3d");
                                BigDecimal first3DHits = resultSet.getBigDecimal("first_3d_hits");
                                BigDecimal second3DHits = resultSet.getBigDecimal("second_3d_hits");
                                BigDecimal third3DHits = resultSet.getBigDecimal("third_3d_hits");

                                // 4D values
                                BigDecimal third4D = resultSet.getBigDecimal("third_4d");
                                BigDecimal third4DHits = resultSet.getBigDecimal("third_4d_hits");

                                // Totals
                                BigDecimal total = resultSet.getBigDecimal("total");

                                // Create new home_item with all the data
                                newItems.add(new home_item(
                                        total,
                                        first2D, second2D, third2D,
                                        first3D, second3D, third3D,
                                        third4D,
                                        first2DHits, second2DHits, third2DHits,
                                        first3DHits, second3DHits, third3DHits,
                                        third4DHits,
                                        agent,
                                        group
                                ));
                            }

                            // Update UI on main thread
                            requireActivity().runOnUiThread(() -> {
                                itemList.clear();
                                itemList.addAll(newItems);
                                filterAgents(searchView.getQuery().toString());
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                Log.e("DatabaseError", "Error fetching agent data", e);
                if (getActivity() != null && isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error loading agent data", Toast.LENGTH_SHORT).show());
                }
            } catch (ParseException e) {
                Log.e("DateError", "Error parsing date", e);
                if (getActivity() != null && isAdded()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error processing date", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}