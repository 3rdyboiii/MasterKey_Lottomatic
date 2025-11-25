package com.example.masterkeylottomatic.activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.masterkeylottomatic.LoginActivity;
import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.fragment.AccountFragment;
import com.example.masterkeylottomatic.fragment.BetLimitFragment;
import com.example.masterkeylottomatic.fragment.HomeFragment;
import com.example.masterkeylottomatic.fragment.LimitFragment;
import com.example.masterkeylottomatic.fragment.ListFragment;
import com.example.masterkeylottomatic.fragment.ResultFragment;
import com.example.masterkeylottomatic.fragment.agentsTotalFragment;
import com.example.masterkeylottomatic.utility.NetworkChangeListener;
import com.google.android.material.navigation.NavigationView;

import java.sql.Connection;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView agent, amount, total, total3D, total4D, total6D, totalPick3, totalLast2, total2pm, total5pm, total9pm, hits;
    private String totalsum, date, selectedGroup, groupName;
    private Calendar Date;

    private boolean isSuperUser = false;

    Connection connection;
    private ImageView loading;
    private TableLayout tableLayout;
    private ToggleButton toggleBtn;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private Button dateBtn;
    private Spinner groupList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AlertDialog loadingDialog;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        isSuperUser = getIntent().getBooleanExtra("isSuperUser", false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.custom_confirmdialog);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.custom_dialog_bg));
            dialog.setCancelable(false);

            Button positiveButton = dialog.findViewById(R.id.positiveButton);
            Button negativeButton = dialog.findViewById(R.id.negativeButton);
            TextView title = dialog.findViewById(R.id.dialogTitle);
            TextView description = dialog.findViewById(R.id.dialogDescription);

            title.setText("Confirmation to logout:");
            description.setText("Do you want to logout?");

            positiveButton.setOnClickListener(buttonView -> {
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                dialog.dismiss();
            });

            negativeButton.setOnClickListener(buttonView -> {
                dialog.dismiss();
            });
            dialog.show();
        });


        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                // This ensures the icon stays white during animation
                getDrawerArrowDrawable().setColor(ContextCompat.getColor(MainActivity.this, R.color.white)); }
        };
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId(); // Get the selected item's ID

            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();  // Replace with your Home fragment
            } else if (id == R.id.nav_agent) {
                selectedFragment = new agentsTotalFragment();
            } else if (id == R.id.nav_account) {
                selectedFragment = new AccountFragment();
            } else if (id == R.id.nav_list) {
                selectedFragment = new ListFragment();
            } else if (id == R.id.nav_result) {
                selectedFragment = new ResultFragment();
            } else if (id == R.id.nav_limit) {
                selectedFragment = new LimitFragment();
            } else if (id == R.id.nav_betlimit) {
                selectedFragment = new BetLimitFragment();
            } else if (id == R.id.nav_result) {
                selectedFragment = new ResultFragment();
            } else if (id == R.id.nav_limit) {
                selectedFragment = new LimitFragment();
            } else if (id == R.id.nav_betlimit) {
                selectedFragment = new BetLimitFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            drawerLayout.closeDrawers();  // Close drawer after item is clicked
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.custom_confirmdialog);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.custom_dialog_bg));
            dialog.setCancelable(false);

            Button positiveButton = dialog.findViewById(R.id.positiveButton);
            Button negativeButton = dialog.findViewById(R.id.negativeButton);
            TextView title = dialog.findViewById(R.id.dialogTitle);
            TextView description = dialog.findViewById(R.id.dialogDescription);

            title.setText("Confirmation to logout:");
            description.setText("Do you want to logout?");

            positiveButton.setOnClickListener(buttonView -> {
                super.onBackPressed();
                dialog.dismiss();
            });

            negativeButton.setOnClickListener(buttonView -> {
                dialog.dismiss();
            });
            dialog.show();
        }
    }
}