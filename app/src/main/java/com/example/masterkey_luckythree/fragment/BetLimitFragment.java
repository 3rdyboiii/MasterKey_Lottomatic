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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.helper.Account;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BetLimitFragment extends Fragment {

    private TextInputEditText S2LimitTxt, L2LimitTxt, TS3LimitTxt, TL3LimitTxt, fourDLimitTxt;
    private Button updateS2Btn, updateL2Btn, updateTS3Btn, updateTL3Btn, update4DBtn;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_limit, container, false);
        getActivity().setTitle("Limits");

        // Initialize views
        S2LimitTxt = view.findViewById(R.id.gameS2Txt);
        L2LimitTxt = view.findViewById(R.id.gameL2Txt);
        TS3LimitTxt = view.findViewById(R.id.gameTL3Txt);
        TL3LimitTxt = view.findViewById(R.id.gameTS3Txt);
        fourDLimitTxt = view.findViewById(R.id.game4DTxt);

        updateS2Btn = view.findViewById(R.id.updateS2Btn);
        updateL2Btn = view.findViewById(R.id.updateL2Btn);
        updateTS3Btn = view.findViewById(R.id.updateTL3Btn);
        updateTL3Btn = view.findViewById(R.id.updateTS3Btn);
        update4DBtn = view.findViewById(R.id.update4DBtn);

        // Set click listeners
        updateS2Btn.setOnClickListener(v -> showConfirmationDialog("S2"));
        updateL2Btn.setOnClickListener(v -> showConfirmationDialog("L2"));
        updateTS3Btn.setOnClickListener(v -> showConfirmationDialog("TS3"));
        updateTL3Btn.setOnClickListener(v -> showConfirmationDialog("TL3"));
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
                    fetchGameLimit(connection, "S2", S2LimitTxt);
                    fetchGameLimit(connection, "L2", L2LimitTxt);
                    fetchGameLimit(connection, "TS3", TS3LimitTxt);
                    fetchGameLimit(connection, "TL3", TL3LimitTxt);
                    fetchGameLimit(connection, "4D", fourDLimitTxt);
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
        Account account = Account.getInstance(requireContext());
        boolean isSuperUser = account.isSuperUser();
        String userGroup = account.getGroup();

        String query = "SELECT LimitAmount FROM BetLimits WHERE GameType = ? AND [group] = 'COTABATO'";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, gameType);
            if (!isSuperUser) {
                ps.setString(2, userGroup);
            }
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

        if ("S2".equals(gameType)) {
            editText = S2LimitTxt;
        } else if ("L2".equals(gameType)) {
            editText = L2LimitTxt;
        } else if ("TS3".equals(gameType)) {
            editText = TS3LimitTxt;
        } else if ("TL3".equals(gameType)) {
            editText = TL3LimitTxt;
        } else if ("4D".equals(gameType)) {
            editText = fourDLimitTxt;
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
                        Account account = Account.getInstance(requireContext());
                        boolean isSuperUser = account.isSuperUser();
                        String userGroup = account.getGroup();

                        String query = "UPDATE BetLimits SET LimitAmount = ? WHERE GameType = ? AND [group] = 'COTABATO'";

                        try (PreparedStatement ps = connection.prepareStatement(query)) {
                            ps.setDouble(1, newLimit);
                            ps.setString(2, gameType);
                            if (!isSuperUser) {
                                ps.setString(3, userGroup);
                            }
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