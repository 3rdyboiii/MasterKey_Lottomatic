package com.example.masterkeylottomatic.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.helper.Account;
import com.example.masterkeylottomatic.helper.ConSQL;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BetLimitFragment extends Fragment {

    private TextInputEditText Limit2DTxt, Limit3DTxt, Limit4DTxt;
    private Button update2DBtn, update3DBtn, update4DBtn;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_limit, container, false);
        getActivity().setTitle("Limits");

        // Initialize views
        Limit2DTxt = view.findViewById(R.id.game2DTxt);
        Limit3DTxt = view.findViewById(R.id.game3DTxt);
        Limit4DTxt = view.findViewById(R.id.game4DTxt);

        update2DBtn = view.findViewById(R.id.update2DBtn);
        update3DBtn = view.findViewById(R.id.update3DBtn);
        update4DBtn = view.findViewById(R.id.update4DBtn);

        // Set click listeners
        update2DBtn .setOnClickListener(v -> showConfirmationDialog("2D"));
        update3DBtn.setOnClickListener(v -> showConfirmationDialog("3D"));
        update4DBtn.setOnClickListener(v -> showConfirmationDialog("4D"));

        // Fetch current limits
        fetchLimits();

        return view;
    }

    private void fetchLimits() {
        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    fetchGameLimit(connection, "2D", Limit2DTxt);
                    fetchGameLimit(connection, "3D", Limit3DTxt);
                    fetchGameLimit(connection, "4D", Limit4DTxt);
                }
            } catch (SQLException e) {
                Log.e("DatabaseError", "Error fetching limits", e);
                if (getActivity() == null || !isAdded()) return;
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error fetching limits", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchGameLimit(Connection connection, String gameType,
                                TextInputEditText editText) throws SQLException {

        String query = "SELECT LimitAmount FROM BetLimits WHERE GameType = ? AND [group] = 'BICOL'";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, gameType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double limit = rs.getDouble("LimitAmount");
                    if (getActivity() == null || !isAdded()) return;
                    requireActivity().runOnUiThread(() ->
                            editText.setText(String.valueOf(limit)));
                }
            }
        }
    }

    private void showConfirmationDialog(String gameType) {
        if (getActivity() == null || !isAdded()) return;

        Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.custom_confirmdialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.custom_dialog_bg));
        dialog.setCancelable(false);

        Button positiveButton = dialog.findViewById(R.id.positiveButton);
        Button negativeButton = dialog.findViewById(R.id.negativeButton);
        TextView title = dialog.findViewById(R.id.dialogTitle);
        TextView description = dialog.findViewById(R.id.dialogDescription);

        title.setText("Confirm Update");
        description.setText("Update " + gameType + " limit?");

        positiveButton.setOnClickListener(v -> {
            updateLimit(gameType);
            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateLimit(String gameType) {
        String newLimitStr;
        TextInputEditText editText;

        if ("2D".equals(gameType)) {
            editText = Limit2DTxt;
        } else if ("3D".equals(gameType)) {
            editText = Limit3DTxt;
        } else if ("4D".equals(gameType)) {
            editText = Limit4DTxt;
        } else {
            return;
        }

        newLimitStr = editText.getText().toString();

        if (newLimitStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a limit", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double newLimit = Double.parseDouble(newLimitStr);

            executor.execute(() -> {
                ConSQL c = new ConSQL();
                try (Connection connection = c.conclass()) {
                    if (connection != null) {

                        String query = "UPDATE BetLimits SET LimitAmount = ? WHERE GameType = ? AND [group] = 'BICOL'";

                        try (PreparedStatement ps = connection.prepareStatement(query)) {
                            ps.setDouble(1, newLimit);
                            ps.setString(2, gameType);
                            int rowsUpdated = ps.executeUpdate();

                            if (getActivity() == null || !isAdded()) return;

                            requireActivity().runOnUiThread(() -> {
                                if (rowsUpdated > 0) {
                                    Toast.makeText(getContext(),
                                            gameType + " limit updated!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(),
                                            "Failed to update limit",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } catch (SQLException e) {
                    Log.e("DatabaseError", "Error updating limit", e);
                    if (getActivity() == null || !isAdded()) return;
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Database error", Toast.LENGTH_SHORT).show());
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}