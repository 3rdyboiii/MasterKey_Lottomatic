package com.example.masterkey_luckythree.fragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.adapter.ViewPagerAdapter;
import com.example.masterkey_luckythree.adapter.entry_adapter;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.helper.OnDateChangedListener;
import com.example.masterkey_luckythree.item.combo_item;
import com.example.masterkey_luckythree.item.entry_item;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListFragment extends Fragment implements OnDateChangedListener {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView selectedDateText;
    private View datePickerButton;
    private String currentSelectedDate;
    private List<OnDateChangedListener> dateListeners = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_pager, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        selectedDateText = view.findViewById(R.id.selectedDateText);
        datePickerButton = view.findViewById(R.id.datePickerButton);

        // Set current date as default
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        currentSelectedDate = sdf.format(new Date());
        updateDateText(currentSelectedDate);

        // Setup date picker button
        datePickerButton.setOnClickListener(v -> showDatePicker());

        // Delay the ViewPager setup to ensure proper initialization
        view.post(() -> setupViewPager());

        return view;
    }

    private void setupViewPager() {
        // Use the 3-parameter constructor
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager(),
                getLifecycle(),
                this); // Add 'this' as third parameter

        // Create fragments
        EntryListFragment entryFragment = new EntryListFragment();
        ComboTotalFragment comboFragment = new ComboTotalFragment();
        HitsFragment hitsFragment = new HitsFragment();

        // Add fragments to adapter
        adapter.addFragment(entryFragment, "Entries");
        adapter.addFragment(comboFragment, "Total Per Combo");
        adapter.addFragment(hitsFragment, "Hits");

        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(adapter.getPageTitle(position))
        ).attach();

        viewPager.setCurrentItem(0, false);
    }

    private void showDatePicker() {
        // Parse current date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date currentDate;
        try {
            currentDate = sdf.parse(currentSelectedDate);
        } catch (ParseException e) {
            currentDate = new Date();
        }

        Calendar calendar = Calendar.getInstance();
        if (currentDate != null) {
            calendar.setTime(currentDate);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the selected date
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay);
                    String newDate = sdf.format(selectedCalendar.getTime());

                    // Update date and notify listeners
                    currentSelectedDate = newDate;
                    updateDateText(newDate);
                    notifyDateChanged(newDate);
                },
                year, month, day
        );

        // Optional: Set date range limits if needed
        // datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void updateDateText(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parsedDate = sdf.parse(date);
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            String displayDate = displayFormat.format(parsedDate);

            // Check if today
            SimpleDateFormat todayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String today = todayFormat.format(new Date());
            if (date.equals(today)) {
                selectedDateText.setText("Today - " + displayDate);
            } else {
                selectedDateText.setText(displayDate);
            }
        } catch (ParseException e) {
            selectedDateText.setText(date);
        }
    }

    public void registerDateListener(OnDateChangedListener listener) {
        dateListeners.add(listener);
        // Notify the new listener of the current date
        listener.onDateChanged(currentSelectedDate);
    }

    public void unregisterDateListener(OnDateChangedListener listener) {
        dateListeners.remove(listener);
    }

    private void notifyDateChanged(String newDate) {
        for (OnDateChangedListener listener : dateListeners) {
            listener.onDateChanged(newDate);
        }
    }

    @Override
    public void onDateChanged(String selectedDate) {
        // Handle date change if needed in the fragment itself
    }
}