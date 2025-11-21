package com.example.masterkey_luckythree.fragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.adapter.entry_adapter;
import com.example.masterkey_luckythree.helper.Account;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.helper.OnDateChangedListener;
import com.example.masterkey_luckythree.item.combo_item;
import com.example.masterkey_luckythree.item.entry_item;
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

public class EntryListFragment extends Fragment implements OnDateChangedListener {

    RecyclerView DisplayList;
    private entry_adapter adapter;
    private List<entry_item> itemList = new ArrayList<>(); // Initialize immediately

    private String currentDate;
    private boolean isAdapterInitialized = false;

    private FloatingActionButton deleteBtn;
    private SearchView searchView;

    private AlertDialog dialog;
    private View dialogView;

    Connection connection;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onDateChanged(String selectedDate) {
        this.currentDate = selectedDate;
        refreshData(); // Refresh data with new date
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_entry_list, container, false);

        DisplayList = view.findViewById(R.id.displayList);
        deleteBtn = view.findViewById(R.id.deleteBtn);
        searchView = view.findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query){
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        deleteBtn.setVisibility(View.GONE);

        itemList = new ArrayList<>();
        adapter = new entry_adapter(itemList);
        DisplayList.setLayoutManager(new LinearLayoutManager(getActivity()));
        DisplayList.setAdapter(adapter);

        adapter.setOnSelectionChangedListener(hasSelectedItems -> {
            deleteBtn.setVisibility(hasSelectedItems ? View.VISIBLE : View.GONE);
        });

        // Handle delete button click
        deleteBtn.setOnClickListener(v -> showDeleteConfirmationDialog());

        getActivity().setTitle("Entry List");

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

    private void refreshData() {
        // Check if adapter is initialized before using it
        if (!isAdapterInitialized) {
            return; // Skip if adapter is not ready yet
        }

        // Clear existing data
        itemList.clear();
        adapter.notifyDataSetChanged();

        // Fetch new data with currentDate
        fetchEntry(currentDate);
    }

    private void filterList(String text) {
        List<entry_item> filteredList = new ArrayList<>();
        for(entry_item item : itemList){
            if(item.getCombo().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            } else if(item.getAgent().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            }else if(item.getDraw().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            } else if(item.getGame().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            } else if(item.getTranscode().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            }
        }
        if(filteredList.isEmpty()) {
            Toast.makeText(getActivity(),"No data found", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setFilteredList(filteredList);
        }
    }

    private void fetchEntry(String currentDate) {
        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    Account account = Account.getInstance(requireContext());
                    boolean isSuperUser = account.isSuperUser();
                    String userGroup = account.getGroup();

                    String query;
                    if (isSuperUser) {
                        query = "SELECT ID, transcode, combo, bets, prize, game, draw, agent FROM EntryTB WHERE CAST([date] AS DATE) = ? AND [group] IN ('COTABATO', 'COTABATO-2', 'COTABATO-3', 'MAGUINDANAO', 'LANAO') ORDER BY ID DESC";
                    } else {
                        query = "SELECT ID, transcode, combo, bets, prize, game, draw, agent FROM EntryTB WHERE CAST([date] AS DATE) = ? AND [group] = ? ORDER BY ID DESC";
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
                                    entry_item Item = new entry_item(finalID, finalagent, finalcombo, finalbets, finalprize, finaltrans, finalgame, finaldraw);
                                    adapter.addItem(Item);
                                });
                            }

                        }
                    }
                }
            } catch (SQLException e) {
                Log.e("Error:", e.getMessage());
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        List<entry_item> selectedItems = adapter.getSelectedItems();

        if (selectedItems.isEmpty()) return;

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_confirmdialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.custom_dialog_bg));
        dialog.setCancelable(false);

        Button positiveButton = dialog.findViewById(R.id.positiveButton);
        Button negativeButton = dialog.findViewById(R.id.negativeButton);
        TextView title = dialog.findViewById(R.id.dialogTitle);
        TextView description = dialog.findViewById(R.id.dialogDescription);

        title.setText("Confirmation:");
        description.setText("Do you wish to DELETE selected item/s?");

        positiveButton.setOnClickListener(buttonView -> {
            deleteSelectedItemsFromDatabase(selectedItems);
            dialog.dismiss();
        });

        negativeButton.setOnClickListener(buttonView -> {
            dialog.dismiss();
        });
        dialog.show();
    }
    private void deleteSelectedItemsFromDatabase(List<entry_item> selectedItems) {
        List<String> idsToDelete = new ArrayList<>();
        for (entry_item item : selectedItems) {
            idsToDelete.add(item.getId());
        }

        executor.execute(() -> {
            for (String id : idsToDelete) {
                deleteItemFromDatabase(id);
            }

            // Update RecyclerView on the main thread
            requireActivity().runOnUiThread(() -> {
                // Remove items from the adapter's data source
                itemList.removeAll(selectedItems);
                adapter.notifyDataSetChanged(); // Notify that data has changed

                adapter.clearSelectedItems(); // Clear the selected positions

                showmessage(); // Show success message
            });
        });
    }


    private void deleteItemFromDatabase(String ID) {
        try {
            ConSQL c = new ConSQL();
            Connection connection = c.conclass();
            String query = "DELETE FROM EntryTB WHERE ID = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, ID);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            Log.e("SQL Error", e.getMessage());
        }
    }

    private void showmessage() {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_errordialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.custom_dialog_bg));
        dialog.setCancelable(false);

        Button positiveButton = dialog.findViewById(R.id.positiveButton);
        TextView title = dialog.findViewById(R.id.dialogTitle);
        TextView description = dialog.findViewById(R.id.dialogDescription);

        title.setText("Info:");
        description.setText("Item/s has been successfully DELETED!");

        positiveButton.setOnClickListener(buttonView -> {
            dialog.dismiss();
        });
        dialog.show();
    }

}