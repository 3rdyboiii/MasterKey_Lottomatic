package com.example.masterkey_luckythree.activity;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.helper.MyTableList;
import com.example.masterkey_luckythree.utility.NetworkChangeListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SoldoutActivity extends AppCompatActivity {

    private TextView soldoutTxt;
    private FloatingActionButton add, removeButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    TableLayout tableLayout;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final NetworkChangeListener networkChangeListener = new NetworkChangeListener();

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
        setContentView(R.layout.activity_soldout);

        soldoutTxt = findViewById(R.id.soldoutTxt);
        add = findViewById(R.id.fab);
        tableLayout = findViewById(R.id.tableLayout);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshMainLayout);
        removeButton = findViewById(R.id.removeButton);
        removeButton.setOnClickListener(v -> removeSelectedRows());

        add.setOnClickListener(v -> addWinThree());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            soldoutTxt.setText("");
            getWinThree();
        });

        getWinThree();
    }

    private void removeSelectedRows() {
        List<String> selectedCombos = new ArrayList<>();
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            CheckBox checkBox = (CheckBox) row.getChildAt(0);
            if (checkBox.isChecked()) {
                TextView comboTextView = (TextView) row.getChildAt(1);
                selectedCombos.add(comboTextView.getText().toString());
                tableLayout.removeView(row);
                i--; // Adjust index after removing row
            }
        }

        updateRemoveButtonVisibility();

        if (!selectedCombos.isEmpty()) {
            deleteFromDatabase(selectedCombos);
        } else {
            Toast.makeText(this, "No items selected", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateRemoveButtonVisibility() {
        boolean anyChecked = false;
        for (int i = 0; i < tableLayout.getChildCount(); i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            CheckBox checkBox = (CheckBox) row.getChildAt(0);
            if (checkBox.isChecked()) {
                anyChecked = true;
                break;
            }
        }
        removeButton.setVisibility(anyChecked ? View.VISIBLE : View.GONE);
    }
    private void deleteFromDatabase(List<String> combos) {
        ConSQL c = new ConSQL();
        try (Connection connection = c.conclass()) {
            if (connection != null) {
                String query = "DELETE FROM WinThreeTB WHERE winthreeRedGroup = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    for (String combo : combos) {
                        pstmt.setString(1, combo);
                        pstmt.executeUpdate();
                    }
                    Toast.makeText(this, "Selected combos removed", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (SQLException e) {
            Log.e("Error:", e.getMessage());
        }
    }
    private void addWinThree() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialoginput_winthree, null);
        builder.setView(dialogView);
        builder.setTitle("Add WinThree");

        TextInputEditText input = dialogView.findViewById(R.id.text_input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String combo = input.getText().toString().trim();
            if (combo.isEmpty()) {
                input.setError("Please enter a valid combo");
                return;
            }

            List<String> permutations = generatePermutations(combo);
            insertWinThreeData(permutations);

            soldoutTxt.setText("");
            getWinThree();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void insertWinThreeData(List<String> lines) {
        ConSQL c = new ConSQL();
        try (Connection connection = c.conclass()) {
            if (connection != null) {
                String query = "INSERT INTO WinThreeTB (winthreeRedGroup) VALUES (?)";
                try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                    for (String line : lines) {
                        pstmt.setString(1, line);
                        pstmt.executeUpdate();
                        Toast.makeText(this, "Successfully added", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (SQLException e) {
            Log.e("Error:", e.getMessage());
        }
    }
    private void getWinThree() {
        executor.execute(() -> {
            List<MyTableList> myTableLists = new ArrayList<>();
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    String query = "SELECT * FROM WinThreeTB";
                    try (Statement smt = connection.createStatement();
                         ResultSet set = smt.executeQuery(query)) {
                        while (set.next()) {
                            String winthree = set.getString("winthreeRedGroup");

                            MyTableList tableList = new MyTableList();
                            tableList.setCombo(winthree);

                            myTableLists.add(tableList);
                        }
                    }
                }
                runOnUiThread(() -> {
                    if (myTableLists.isEmpty()) {
                        soldoutTxt.setText("Empty");
                    } else {
                        tableLayout.removeAllViews();
                        for (MyTableList item : myTableLists) {
                            TableRow tableRow = new TableRow(this);
                            CheckBox checkBox = new CheckBox(this);

                            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> updateRemoveButtonVisibility());

                            tableRow.setGravity(Gravity.CENTER);

                            TextView comboTextView = new TextView(this);
                            comboTextView.setText(item.getCombo());
                            comboTextView.setPadding(16, 16, 16, 16);
                            comboTextView.setTextSize(20);
                            comboTextView.setGravity(Gravity.CENTER);

                            checkBox.setGravity(Gravity.CENTER);

                            tableRow.addView(checkBox);
                            tableRow.addView(comboTextView);

                            tableLayout.addView(tableRow);
                        }
                    }
                });
            } catch (SQLException e) {
                Log.e("Error:", e.getMessage());
            } finally {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
    private List<String> generatePermutations(String input) {
        if (input == null || input.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> uniquePermutations = new HashSet<>();
        permute(input.toCharArray(), 0, uniquePermutations);

        return new ArrayList<>(uniquePermutations);
    }
    private void permute(char[] arr, int index, Set<String> uniquePermutations) {
        if (index == arr.length - 1) {
            uniquePermutations.add(new String(arr));
        } else {
            Set<Character> usedChars = new HashSet<>();
            for (int i = index; i < arr.length; i++) {
                if (usedChars.add(arr[i])) { // Only proceed if the character is not used at this position
                    swap(arr, i, index);
                    permute(arr, index + 1, uniquePermutations);
                    swap(arr, i, index); // Backtrack
                }
            }
        }
    }
    private void swap(char[] arr, int i, int j) {
        char temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
