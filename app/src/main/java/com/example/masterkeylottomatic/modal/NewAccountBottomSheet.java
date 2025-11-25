package com.example.masterkeylottomatic.modal;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.fragment.AccountFragment;
import com.example.masterkeylottomatic.helper.ConSQL;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewAccountBottomSheet extends BottomSheetDialogFragment {

    private EditText userText, passText, nameText, codeText, versionText, groupText;
    private Button submit;
    private Connection connection;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_new_account, container, false);

        // Initialize views
        userText = view.findViewById(R.id.usernameText);
        passText = view.findViewById(R.id.passwordText);
        nameText = view.findViewById(R.id.nameText);
        codeText = view.findViewById(R.id.codeText);
        groupText = view.findViewById(R.id.groupText);
        versionText = view.findViewById(R.id.versionText);

        submit = view.findViewById(R.id.submitButton);
        submit.setOnClickListener(v -> submitForm());

        return view;
    }

    private void submitForm() {
        if (checkForm()) {
            String user = userText.getText().toString();
            String pass = passText.getText().toString();
            String name = nameText.getText().toString();
            String code = codeText.getText().toString();
            String group = groupText.getText().toString();
            String version = versionText.getText().toString();

            executor.execute(() -> {
                ConSQL c = new ConSQL();
                connection = c.conclass();
                if (connection != null) {
                    try {
                        // Check if username already exists
                        String checkQuery = "SELECT COUNT(*) FROM UserTB WHERE username = ? and code = ?";
                        PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                        checkStatement.setString(1, user);
                        checkStatement.setString(2, code);
                        ResultSet resultSet = checkStatement.executeQuery();

                        if (resultSet.next() && resultSet.getInt(1) > 0) {
                            requireActivity().runOnUiThread(() -> {
                                // Show error message
                            });
                            resultSet.close();
                            checkStatement.close();
                            return;
                        }

                        resultSet.close();
                        checkStatement.close();

                        // Insert the new account
                        String query = "INSERT INTO UserTB (username, password, name, code, version, [group]) VALUES (?, ?, ?, ?, ?, ?)";
                        PreparedStatement preparedStatement = connection.prepareStatement(query);
                        preparedStatement.setString(1, user);
                        preparedStatement.setString(2, pass);
                        preparedStatement.setString(3, name);
                        preparedStatement.setString(4, code);
                        preparedStatement.setString(5, version);
                        preparedStatement.setString(6, group);
                        preparedStatement.executeUpdate();
                        preparedStatement.close();

                        requireActivity().runOnUiThread(() -> {
                            popupmessage();
                            // Refresh the account list in the fragment
                            if (getParentFragment() instanceof AccountFragment) {
                                ((AccountFragment) getParentFragment()).fetchAccount();
                            }
                        });
                    } catch (Exception e) {
                        requireActivity().runOnUiThread(() -> {
                            // Show error message
                        });
                    }
                }
            });
        }
    }

    private boolean checkForm() {
        // Same validation logic as before
        boolean isValid = true;

        if (userText.getText().toString().isEmpty()) {
            userText.setError("Username is required");
            isValid = false;
        }
        // Add other validations...

        return isValid;
    }

    private void popupmessage() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);

        TextView detailTxt = dialog.findViewById(R.id.detailsTxt);
        detailTxt.setText("Account Created!");

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            dismiss(); // Dismiss the bottom sheet
        }, 2000);

        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
