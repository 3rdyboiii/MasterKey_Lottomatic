package com.example.masterkey_luckythree.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.item.combo_item;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LimitFragment extends Fragment {

    private TextView currentSoldoutTxt;
    private TextInputEditText newSoldoutTxt;
    private Button addBtn;
    private Button removeBtn;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private String currentSoldout = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_soldout, container, false);
        getActivity().setTitle("Soldout");

        currentSoldoutTxt = view.findViewById(R.id.currentSoldoutTxt);
        newSoldoutTxt = view.findViewById(R.id.newSoldoutTxt);
        addBtn = view.findViewById(R.id.addBtn);

        addBtn.setOnClickListener(v -> {
            String newDigit = newSoldoutTxt.getText().toString().trim();
            if (validateNewDigit(newDigit)) {
                showConfirmationDialog(newDigit);
            }
        });

        removeBtn = view.findViewById(R.id.removeBtn);
        removeBtn.setOnClickListener(v -> showRemoveDialog());

        fetchSoldout();
        return view;
    }

    private void showRemoveDialog() {
        if (currentSoldout.isEmpty()) {
            Toast.makeText(getActivity(), "No soldout digits to remove", Toast.LENGTH_SHORT).show();
            return;
        }

        // Split current soldout digits
        String[] digits = currentSoldout.split(",");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select digit to remove");

        builder.setItems(digits, (dialog, which) -> {
            String digitToRemove = digits[which];
            showRemoveConfirmationDialog(digitToRemove);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showRemoveConfirmationDialog(String digitToRemove) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_confirmdialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        Button positiveButton = dialog.findViewById(R.id.positiveButton);
        Button negativeButton = dialog.findViewById(R.id.negativeButton);
        TextView title = dialog.findViewById(R.id.dialogTitle);
        TextView description = dialog.findViewById(R.id.dialogDescription);

        title.setText("Confirm Removal");
        description.setText("Remove " + digitToRemove + " from soldout list?");

        positiveButton.setOnClickListener(v -> {
            dialog.dismiss();
            removeSoldoutDigit(digitToRemove);
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void removeSoldoutDigit(String digitToRemove) {
        // Create a final variable for use in lambda
        final String finalDigitToRemove = digitToRemove;

        executor.execute(() -> {
            // Create a new variable for the updated soldout that we'll modify
            String newSoldout = currentSoldout.replace(finalDigitToRemove, "")
                    .replace(",,", ",")
                    .replaceAll("^,|,$", "");

            // Determine if we should set to NULL or keep the updated value
            final String finalUpdatedSoldout = newSoldout.isEmpty() ? null : newSoldout;

            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    String query;
                    if (finalUpdatedSoldout == null) {
                        query = "DELETE FROM SoldoutTB WHERE [group] = 'cotabato'";
                    } else {
                        query = "UPDATE SoldoutTB SET soldout = ? WHERE [group] = 'cotabato'";
                    }

                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        if (finalUpdatedSoldout != null) {
                            preparedStatement.setString(1, finalUpdatedSoldout);
                        }
                        int rowsAffected = preparedStatement.executeUpdate();

                        requireActivity().runOnUiThread(() -> {
                            if (rowsAffected > 0) {
                                currentSoldout = finalUpdatedSoldout == null ? "" : finalUpdatedSoldout;
                                currentSoldoutTxt.setText(currentSoldout.isEmpty() ?
                                        "No soldout digits currently" :
                                        "Soldout digits: " + currentSoldout);
                                Toast.makeText(getActivity(), "Digit removed successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Failed to remove digit", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (SQLException e) {
                Log.e("Error:", e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Error removing digit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean validateNewDigit(String digit) {
        if (digit.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter a digit", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (currentSoldout.contains(digit)) {
            Toast.makeText(getActivity(), "This digit is already soldout", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void fetchSoldout() {
        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    String query = "SELECT soldout FROM SoldoutTB WHERE [group] = 'cotabato'";

                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {
                            if (resultSet.next()) {
                                currentSoldout = resultSet.getString("soldout");
                                if (currentSoldout == null) {
                                    currentSoldout = "";
                                }
                            } else {
                                currentSoldout = "";
                            }

                            requireActivity().runOnUiThread(() -> {
                                currentSoldoutTxt.setText(currentSoldout.isEmpty() ?
                                        "No soldout digits currently" :
                                        "Soldout digits: " + currentSoldout);
                            });
                        }
                    }
                }
            } catch (SQLException e) {
                Log.e("Error:", e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Error fetching soldout digits", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showConfirmationDialog(String newDigit) {
        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_confirmdialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        Button positiveButton = dialog.findViewById(R.id.positiveButton);
        Button negativeButton = dialog.findViewById(R.id.negativeButton);
        TextView title = dialog.findViewById(R.id.dialogTitle);
        TextView description = dialog.findViewById(R.id.dialogDescription);

        title.setText("Confirm Addition");
        description.setText("Add " + newDigit + " to soldout list?");

        positiveButton.setOnClickListener(v -> {
            dialog.dismiss();
            addSoldoutDigit(newDigit);
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void addSoldoutDigit(String newDigit) {
        executor.execute(() -> {
            String updatedSoldout = currentSoldout.isEmpty() ? newDigit : currentSoldout + "," + newDigit;

            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    // First check if a record exists
                    boolean recordExists = false;
                    String checkQuery = "SELECT COUNT(*) FROM SoldoutTB WHERE [group] = 'cotabato'";
                    try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery);
                         ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            recordExists = rs.getInt(1) > 0;
                        }
                    }

                    // Either UPDATE or INSERT based on existence
                    String query;
                    if (recordExists) {
                        query = "UPDATE SoldoutTB SET soldout = ? WHERE [group] = 'cotabato'";
                    } else {
                        query = "INSERT INTO SoldoutTB (soldout, [group]) VALUES (?, 'cotabato')";
                    }

                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setString(1, updatedSoldout);
                        int rowsAffected = preparedStatement.executeUpdate();

                        requireActivity().runOnUiThread(() -> {
                            if (rowsAffected > 0) {
                                currentSoldout = updatedSoldout;
                                currentSoldoutTxt.setText("Soldout digits: " + currentSoldout);
                                newSoldoutTxt.setText("");
                                Toast.makeText(getActivity(), "Soldout digit added successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Failed to update soldout digits", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            } catch (SQLException e) {
                Log.e("Error:", e.getMessage());
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "Error updating soldout digits: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}