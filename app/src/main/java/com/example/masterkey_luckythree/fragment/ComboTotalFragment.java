package com.example.masterkey_luckythree.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Calendar;
import java.util.Collections;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.adapter.ComboExpandableListAdapter;
import com.example.masterkey_luckythree.adapter.ComboPagerAdapter;
import com.example.masterkey_luckythree.adapter.combo_adapter;
import com.example.masterkey_luckythree.adapter.entry_adapter;
import com.example.masterkey_luckythree.helper.Account;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.helper.OnDateChangedListener;
import com.example.masterkey_luckythree.item.combo_item;
import com.example.masterkey_luckythree.item.entry_item;

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

public class ComboTotalFragment extends Fragment implements OnDateChangedListener {
    private ComboPagerAdapter pagerAdapter;
    private List<combo_item> itemList = new ArrayList<>();
    private List<combo_item> filteredList = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private SearchView searchView;
    private ImageView filterBtn;
    private TextView totalTxt;
    private double currentTotalSum = 0.0;

    private ExpandableListView expandableListView;
    private ComboExpandableListAdapter expandableListAdapter;
    private int currentSort = 1; // 1=combo asc, 2=combo desc, 3=amount asc, 4=amount desc
    private String currentFilter = "all";
    private String currentDraw = "2PM";
    private String currentDate;

    @Override
    public void onDateChanged(String selectedDate) {
        this.currentDate = selectedDate;
        refreshData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_combo_total, container, false);

        expandableListView = view.findViewById(R.id.expandableListView);
        searchView = view.findViewById(R.id.searchView);
        filterBtn = view.findViewById(R.id.filterBtn);
        totalTxt = view.findViewById(R.id.totalTxt);

        setupSearch();
        setupFilterButton();

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

    private void refreshData() {
        fetchPerComboTotal(currentDate);
    }

    private void setupExpandableListView(List<combo_item> items) {
        requireActivity().runOnUiThread(() -> {
            if (items.isEmpty()) {
                // Show empty state
                TextView emptyView = getView().findViewById(R.id.emptyView);
                if (emptyView != null) {
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText("No combos found");
                }
                expandableListView.setVisibility(View.GONE);
            } else {
                // Hide empty state and show data
                View emptyView = getView().findViewById(R.id.emptyView);
                if (emptyView != null) emptyView.setVisibility(View.GONE);

                expandableListView.setVisibility(View.VISIBLE);
                filteredList = new ArrayList<>(items);
                expandableListAdapter = new ComboExpandableListAdapter(getContext(), filteredList);
                expandableListView.setAdapter(expandableListAdapter);

                // Force redraw
                expandableListView.requestLayout();
                expandableListView.post(() -> {
                    expandableListView.invalidateViews();
                });
            }
        });
    }

    /*private void setupViewPager(List<combo_item> items) {
        // Group items by their first digit (0-9)
        List<List<combo_item>> groupedItems = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            groupedItems.add(new ArrayList<>());
        }

        for (combo_item item : items) {
            if (item.getCombo().length() > 0) {
                int firstDigit = Character.getNumericValue(item.getCombo().charAt(0));
                groupedItems.get(firstDigit).add(item);
            }
        }

        pagerAdapter = new ComboPagerAdapter(groupedItems);
        viewPager.setAdapter(pagerAdapter);
    }*/

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return true;
            }
        });
    }

    private void setupFilterButton() {
        filterBtn.setOnClickListener(v -> showFilterDialog());
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Filter & Sort Options");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filter_sort, null);
        builder.setView(dialogView);

        RadioGroup sortGroup = dialogView.findViewById(R.id.sortGroup);
        RadioGroup filterGroup = dialogView.findViewById(R.id.filterGroup);
        RadioGroup drawGroup = dialogView.findViewById(R.id.drawGroup);

        // Set current selections
        switch (currentSort) {
            case 2: sortGroup.check(R.id.combosortDesc); break;
            case 3: sortGroup.check(R.id.sortAsc); break;
            case 4: sortGroup.check(R.id.sortDesc); break;
            default: sortGroup.check(R.id.combosortAsc); // Default to ascending
        }

        switch (currentFilter) {
            case "L2": filterGroup.check(R.id.filterL2); break;
            case "3D": filterGroup.check(R.id.filter3D); break;
            case "4D": filterGroup.check(R.id.filter4D); break;
            default: filterGroup.check(R.id.filterAll);
        }

        switch (currentDraw) {
            case "5PM": drawGroup.check(R.id.draw5pm); break;
            case "9PM": drawGroup.check(R.id.draw9pm); break;
            default: drawGroup.check(R.id.draw2pm); // Default to 2PM
        }

        builder.setPositiveButton("Apply", (dialog, which) -> {
            // Handle sort selection
            int selectedSortId = sortGroup.getCheckedRadioButtonId();
            if (selectedSortId == R.id.sortDesc) {
                currentSort = 4; // amount desc
            } else if (selectedSortId == R.id.sortAsc) {
                currentSort = 3; // amount asc
            } else if (selectedSortId == R.id.combosortDesc) {
                currentSort = 2; // combo desc
            } else if (selectedSortId == R.id.combosortAsc) {
                currentSort = 1; // combo asc
            }

            // Handle filter selection
            int selectedFilterId = filterGroup.getCheckedRadioButtonId();
            if (selectedFilterId == R.id.filterL2) {
                currentFilter = "L2";
            } else if (selectedFilterId == R.id.filter3D) {
                currentFilter = "3D";
            } else if (selectedFilterId == R.id.filter4D) {
                currentFilter = "4D";
            } else {
                currentFilter = "all";
            }

            int selectedDrawId = drawGroup.getCheckedRadioButtonId();
            if (selectedDrawId == R.id.draw5pm) {
                currentDraw = "5PM";
            } else if (selectedDrawId == R.id.draw9pm) {
                currentDraw = "9PM";
            } else {
                currentDraw = "2PM"; // Default to 2PM
            }

            sortData();
            filterData(searchView.getQuery().toString());
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void sortData() {
        switch (currentSort) {
            case 1: // Combo asc
                Collections.sort(filteredList, (o1, o2) -> o1.getCombo().compareTo(o2.getCombo()));
                break;
            case 2: // Combo desc
                Collections.sort(filteredList, (o1, o2) -> o2.getCombo().compareTo(o1.getCombo()));
                break;
            case 3: // Amount asc
                Collections.sort(filteredList, (o1, o2) -> {
                    double amount1 = Double.parseDouble(o1.getTotal().replace(",", ""));
                    double amount2 = Double.parseDouble(o2.getTotal().replace(",", ""));
                    return Double.compare(amount1, amount2);
                });
                break;
            case 4: // Amount desc
                Collections.sort(filteredList, (o1, o2) -> {
                    double amount1 = Double.parseDouble(o1.getTotal().replace(",", ""));
                    double amount2 = Double.parseDouble(o2.getTotal().replace(",", ""));
                    return Double.compare(amount2, amount1);
                });
                break;
        }
    }

    private void filterData(String query) {
        filteredList.clear();
        currentTotalSum = 0.0;

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        boolean isSunday = (dayOfWeek == Calendar.SUNDAY);

        for (combo_item item : itemList) {
            // Apply search filter
            boolean matchesSearch = item.getCombo().toLowerCase().contains(query.toLowerCase());

            // Apply game type filter
            boolean matchesGameType = true;
            switch (currentFilter) {
                case "L2":
                    matchesGameType = item.getGame().equals(isSunday ? "S2" : "L2");
                    break;
                case "3D":
                    matchesGameType = item.getGame().equals(isSunday ? "TS3" : "TL3");
                    break;
                case "4D":
                    matchesGameType = item.getGame().equals("4D");
                    break;
                default: // "all"
                    matchesGameType = true;
            }

            // Apply draw filter
            boolean matchesDraw = true;
            if (!currentDraw.equals("all")) {
                matchesDraw = item.getDraw().equals(currentDraw);
            }

            if (matchesSearch && matchesGameType && matchesDraw) {
                filteredList.add(item);
                try {
                    currentTotalSum += Double.parseDouble(item.getTotal().replace(",", ""));
                } catch (NumberFormatException e) {
                    Log.e("TotalError", "Error parsing total: " + item.getTotal());
                }
            }
        }

        // Sort the filtered results
        sortData();

        expandableListAdapter.updateData(filteredList);
        updateTotalText();
    }

    private void updateTotalText() {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        String formattedTotal = decimalFormat.format(currentTotalSum);
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                totalTxt.setText(formattedTotal);
            });
        }
    }

    private void fetchPerComboTotal(String date) {
        executor.execute(() -> {
            // Check if fragment is still attached before proceeding
            if (getActivity() == null || !isAdded()) {
                return;
            }

            itemList.clear();
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    // Get context safely
                    Context context = getActivity();
                    if (context == null) return;

                    Account account = Account.getInstance(context);
                    boolean isSuperUser = account.isSuperUser();
                    String userGroup = account.getGroup();

                    String query;
                    if (isSuperUser) {
                        query = "SELECT [combo], [game], [draw], SUM([bets]) AS total " +
                                "FROM EntryTB " +
                                "WHERE CAST([date] AS DATE) = ? AND [group] IN ('COTABATO', 'COTABATO-2', 'COTABATO-3', 'MAGUINDANAO', 'LANAO') " +
                                "GROUP BY [combo], [game], [draw] " +
                                "ORDER BY combo ASC";
                    } else {
                        query = "SELECT [combo], [game], [draw], SUM([bets]) AS total " +
                                "FROM EntryTB " +
                                "WHERE CAST([date] AS DATE) = ? AND [group] = ? " +
                                "GROUP BY [combo], [game], [draw] " +
                                "ORDER BY combo ASC";
                    }
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, currentDate);
                        if (!isSuperUser) {
                            preparedStatement.setString(2, userGroup);
                        }
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            while (resultSet.next()) {
                                String combo = resultSet.getString("combo");
                                String game = resultSet.getString("game");
                                String draw = resultSet.getString("draw");
                                BigDecimal total = resultSet.getBigDecimal("total");

                                DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
                                String formattedTotal = decimalFormat.format(total);

                                // Check again before adding to list
                                if (getActivity() == null || !isAdded()) return;

                                combo_item item = new combo_item(combo, formattedTotal, game, draw);
                                itemList.add(item);
                            }

                            // Check before updating UI
                            if (getActivity() == null || !isAdded()) return;

                            requireActivity().runOnUiThread(() -> {
                                // Check one more time in UI thread
                                if (isAdded() && getActivity() != null) {
                                    setupExpandableListView(itemList);
                                    filterData(searchView.getQuery().toString());
                                }
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                Log.e("Error:", e.getMessage());

                // Check before showing toast
                if (getActivity() == null || !isAdded()) return;

                requireActivity().runOnUiThread(() -> {
                    if (isAdded() && getActivity() != null) {
                        Toast.makeText(getActivity(), "Error loading data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}