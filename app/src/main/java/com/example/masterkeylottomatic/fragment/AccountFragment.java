package com.example.masterkeylottomatic.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.adapter.account_adapter;
import com.example.masterkeylottomatic.helper.Account;
import com.example.masterkeylottomatic.helper.ConSQL;
import com.example.masterkeylottomatic.item.account_item;
import com.example.masterkeylottomatic.modal.NewAccountBottomSheet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountFragment extends Fragment {


    RecyclerView DisplayList;
    private account_adapter adapter;
    private List<account_item> itemList;

    private SearchView searchView;

    private View addaccount;
    private View loading;

    Connection connection;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onStart() {
        fetchAccount();
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        DisplayList = view.findViewById(R.id.displayList);
        itemList = new ArrayList<>();
        adapter = new account_adapter(getContext(), itemList);
        DisplayList.setLayoutManager(new LinearLayoutManager(getActivity()));
        DisplayList.setAdapter(adapter);
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

        loading = view.findViewById(R.id.loadingAnimation);
        addaccount = view.findViewById(R.id.addBtn);
        addaccount.setOnClickListener(v -> {
            NewAccountBottomSheet bottomSheet = new NewAccountBottomSheet();
            bottomSheet.show(getParentFragmentManager(), "NewAccountBottomSheet");
        });

        getActivity().setTitle("Accounts");

        return view;
    }

    private void filterList(String text) {
        List<account_item> filteredList = new ArrayList<>();
        for(account_item item : itemList){
            if(item.getUsername().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            } else if(item.getGroup().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            }
        }
        if(filteredList.isEmpty()) {
            Toast.makeText(getActivity(),"No data found", Toast.LENGTH_SHORT).show();
        } else {
            adapter.setFilteredList(filteredList);
        }
    }

    public void fetchAccount() {
        Dialog loadingDialog = new Dialog(requireContext(), R.style.DialogTheme);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.fetching_bets);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.setCancelable(false);

        // Show the loading dialog
        requireActivity().runOnUiThread(loadingDialog::show);

        itemList.clear();

        executor.execute(() -> {
            ConSQL c = new ConSQL();
            try (Connection connection = c.conclass()) {
                if (connection != null) {
                    Account account = Account.getInstance(requireContext());
                    boolean isSuperUser = account.isSuperUser();
                    String userGroup = account.getGroup();

                    String query;
                    if (isSuperUser) {
                        query = "SELECT username, name, code, version, [group] FROM UserTB WHERE [group] = ? ORDER BY code DESC";
                    } else {
                        query = "SELECT username, name, code, version, [group] FROM UserTB WHERE [group] = ? ORDER BY code DESC";
                    }
                    try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        if (!isSuperUser) {
                            preparedStatement.setString(1, userGroup);
                        }
                        try (ResultSet resultSet = preparedStatement.executeQuery()) {

                            while (resultSet.next()) {
                                String username = resultSet.getString("username");
                                String name = resultSet.getString("name");
                                String code = resultSet.getString("code");
                                String version = resultSet.getString("version");
                                String group = resultSet.getString("group");

                                // Include totalBets in the account_item object
                                account_item item = new account_item(username, name, code, version, group);
                                itemList.add(item);
                            }

                            if (getActivity() == null || !isAdded()) return;

                            requireActivity().runOnUiThread(() -> {
                                loadingDialog.dismiss();
                                adapter.notifyDataSetChanged();

                                if (itemList.isEmpty()) {
                                    DisplayList.setVisibility(View.GONE);
                                    loading.setVisibility(View.VISIBLE);
                                } else {
                                    DisplayList.setVisibility(View.VISIBLE);
                                    loading.setVisibility(View.GONE);
                                }
                            });

                        }
                    }

                }
            } catch (SQLException e) {
                Log.e("Error:", e.getMessage());
                try {
                    if (connection != null) {
                        connection.rollback(); // Rollback transaction in case of an error
                    }
                } catch (SQLException rollbackEx) {
                    Log.e("Rollback Error:", rollbackEx.getMessage());
                }
            }
        });
    }
}