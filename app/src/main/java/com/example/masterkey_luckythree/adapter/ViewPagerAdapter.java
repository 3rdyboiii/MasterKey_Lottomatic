package com.example.masterkey_luckythree.adapter;

import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.masterkey_luckythree.fragment.ListFragment;
import com.example.masterkey_luckythree.helper.OnDateChangedListener;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragments = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();
    private ListFragment parentFragment;

    // Constructor with 2 parameters (for backward compatibility)
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager,
                            @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    // Constructor with 3 parameters (includes parentFragment)
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager,
                            @NonNull Lifecycle lifecycle,
                            ListFragment parentFragment) {
        super(fragmentManager, lifecycle);
        this.parentFragment = parentFragment;
    }

    public void addFragment(Fragment fragment, String title) {
        fragments.add(fragment);
        titles.add(title);

        // Register fragment as date listener if it implements the interface
        if (fragment instanceof OnDateChangedListener) {
            parentFragment.registerDateListener((OnDateChangedListener) fragment);
        }
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}