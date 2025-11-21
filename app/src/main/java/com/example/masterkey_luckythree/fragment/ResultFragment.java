package com.example.masterkey_luckythree.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
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
import com.example.masterkey_luckythree.helper.TextInputFilter;
import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResultFragment extends Fragment {

    private TextInputEditText draw2pm3DTxt, draw5pm3DTxt, draw9pm3DTxt, draw9pm4DTxt;
    private TextInputEditText draw2pm3DGroup2Txt, draw5pm3DGroup2Txt, draw9pm3DGroup2Txt;
    private TextInputEditText draw2pm2DTxt, draw5pm2DTxt, draw9pm2DTxt;
    private TextView result3D, result2D, result4D, lanaogroup, cotabatogroup;
    private Button update3DBtn, updateP131Btn, update4DBtn;
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        getActivity().setTitle("Results");

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        result2D = view.findViewById(R.id.result2D);
        result3D = view.findViewById(R.id.result3D);
        result4D = view.findViewById(R.id.result4D);

        lanaogroup = view.findViewById(R.id.Group1Txt);
        cotabatogroup = view.findViewById(R.id.Group2Txt);


        result2D.setText((dayOfWeek == Calendar.SUNDAY) ? "S2 Results" : "L2 Results");
        result3D.setText((dayOfWeek == Calendar.SUNDAY) ? "TS3 Results" : "TL3 Results");

        // Initialize 3D views
        draw2pm3DTxt = view.findViewById(R.id.draw2pmTxt);
        draw5pm3DTxt = view.findViewById(R.id.draw5pmTxt);
        draw9pm3DTxt = view.findViewById(R.id.draw9pmTxt);
        draw2pm3DGroup2Txt = view.findViewById(R.id.draw2pmGroup2Txt);
        draw5pm3DGroup2Txt = view.findViewById(R.id.draw5pmGroup2Txt);
        draw9pm3DGroup2Txt = view.findViewById(R.id.draw9pmGroup2Txt);
        update3DBtn = view.findViewById(R.id.updateBtn);

        // Initialize 2D views
        draw2pm2DTxt = view.findViewById(R.id.draw2pm2DTxt);
        draw5pm2DTxt = view.findViewById(R.id.draw5pm2DTxt);
        draw9pm2DTxt = view.findViewById(R.id.draw9pm2DTxt);

        // Initialize 4D views
        draw9pm4DTxt = view.findViewById(R.id.draw9pm4DTxt);
        update4DBtn = view.findViewById(R.id.update4DBtn);
        updateP131Btn = view.findViewById(R.id.updateP131Btn);

        // Set click listeners
        update3DBtn.setOnClickListener(v -> showConfirmationDialog((dayOfWeek == Calendar.SUNDAY) ? "TS3" : "TL3"));
        updateP131Btn.setOnClickListener(v -> showConfirmationDialog((dayOfWeek == Calendar.SUNDAY) ? "S2" : "L2"));
        update4DBtn.setOnClickListener(v -> showConfirmationDialog("4D"));

        // Fetch current results
        fetchResults();

        return view;
    }

    private void fetchResults() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    // Fetch 3D Lanao results
                    fetchGameResults(connection, currentDate, (dayOfWeek == Calendar.SUNDAY) ? "TS3" : "TL3",
                            draw2pm3DTxt, draw5pm3DTxt, draw9pm3DTxt);

                    // Fetch 3D Cotabato results
                    fetchGameResults(connection, currentDate, (dayOfWeek == Calendar.SUNDAY) ? "TS3" : "TL3",
                            draw2pm3DGroup2Txt, draw5pm3DGroup2Txt, draw9pm3DGroup2Txt);

                    // Fetch LAST2 results
                    fetchGameResults(connection, currentDate, (dayOfWeek == Calendar.SUNDAY) ? "S2" : "L2",
                            draw2pm2DTxt, draw5pm2DTxt, draw9pm2DTxt);

                    fetchGameResults(connection, currentDate, "4D",
                            null, null, draw9pm4DTxt);
                }
            } catch (SQLException e) {
                Log.e("DatabaseError", "Error fetching results", e);
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Error fetching results", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void fetchGameResults(Connection connection, String date, String game,
                                  TextInputEditText... editTexts) throws SQLException {

        String[] draws = {"2PM", "5PM", "9PM"};
        for (int i = 0; i < draws.length; i++) {

            String query = "SELECT result FROM ResultTB WHERE game = ? AND draw = ? AND CAST([date] as date) = ? and [group] in ('lanao', 'cotabato', 'cotabato-2')";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setString(1, game);
                ps.setString(2, draws[i]);
                ps.setString(3, date);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String result = rs.getString("result");
                        int finalI = i;

                        if (getActivity() == null || !isAdded()) return;


                        requireActivity().runOnUiThread(() ->
                                editTexts[finalI].setText(result));
                    }
                }
            }
        }
    }

    private void showConfirmationDialog(String gameType) {

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
        description.setText("Update " + gameType + " results?");

        positiveButton.setOnClickListener(v -> {
            updateResults(gameType);
            dialog.dismiss();
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void updateResults(String gameType) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    connection.setAutoCommit(false); // Start transaction

                    try {
                        if ("L2".equals(gameType) || "S2".equals(gameType)) {
                            updateGameResults(connection, currentDate, (dayOfWeek == Calendar.SUNDAY) ? "S2" : "L2", "lanao",
                                    draw2pm2DTxt.getText().toString(),
                                    draw5pm2DTxt.getText().toString(),
                                    draw9pm2DTxt.getText().toString());
                        } else if ("TL3".equals(gameType) || "TS3".equals(gameType)) {
                            updateGameResults(connection, currentDate, (dayOfWeek == Calendar.SUNDAY) ? "TS3" : "TL3", "lanao",
                                    draw2pm3DTxt.getText().toString(),
                                    draw5pm3DTxt.getText().toString(),
                                    draw9pm3DTxt.getText().toString());
                            updateGameResults(connection, currentDate, (dayOfWeek == Calendar.SUNDAY) ? "TS3" : "TL3", "cotabato",
                                    draw2pm3DGroup2Txt.getText().toString(),
                                    draw5pm3DGroup2Txt.getText().toString(),
                                    draw9pm3DGroup2Txt.getText().toString());
                        } else if ("4D".equals(gameType)) {
                            updateGameResults(connection, currentDate, "4D", null, null, "lanao",
                                    draw9pm4DTxt.getText().toString());
                        }

                        connection.commit();

                        if (getActivity() == null || !isAdded()) return;

                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), gameType + " results updated!", Toast.LENGTH_SHORT).show());
                    } catch (SQLException e) {
                        connection.rollback();
                        Log.e("DatabaseError", "Error updating " + gameType + " results", e);
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Failed to update " + gameType + " results", Toast.LENGTH_SHORT).show());
                    } finally {
                        connection.setAutoCommit(true);
                    }
                }
            } catch (SQLException e) {
                Log.e("DatabaseError", "Connection error", e);
            }
        });
    }

    private void updateGameResults(Connection connection, String date, String game, String group,
                                   String result2pm, String result5pm, String result9pm) throws SQLException {
        // Update the result in the ResultTB table
        String updateQuery = "UPDATE ResultTB SET result = ? WHERE game = ? AND draw = ? AND CAST([date] AS DATE) = ? and [group] = ?";

        updateDrawResult(connection, updateQuery, game, "2PM", date, result2pm, group);
        updateDrawResult(connection, updateQuery, game, "5PM", date, result5pm, group);
        updateDrawResult(connection, updateQuery, game, "9PM", date, result9pm, group);
    }

    private void updateDrawResult(Connection connection, String query, String game,
                                  String draw, String date, String result, String group) throws SQLException {

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, result);
            ps.setString(2, game);
            ps.setString(3, draw);
            ps.setString(4, date);
            ps.setString(5, group);
            int rowsUpdated = ps.executeUpdate();
            Log.d("UpdateResult", game + " " + draw + ": " + rowsUpdated + " rows updated");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}