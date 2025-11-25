package com.example.masterkeylottomatic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.item.combo_item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComboExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> groupTitles; // e.g., ["000-099", "100-199", ...]
    private Map<String, List<combo_item>> childItems; // Map of group title to its items

    public ComboExpandableListAdapter(Context context, List<combo_item> items) {
        this.context = context;
        organizeData(items);
    }

    private void organizeData(List<combo_item> items) {
        groupTitles = new ArrayList<>();
        childItems = new HashMap<>();

        // Group items by their first digit (for 000-099, 100-199, etc.)
        for (combo_item item : items) {
            String firstDigit = item.getCombo().substring(0, 1);
            String groupKey = firstDigit + "00-" + firstDigit + "99";

            if (!childItems.containsKey(groupKey)) {
                groupTitles.add(groupKey);
                childItems.put(groupKey, new ArrayList<>());
            }
            childItems.get(groupKey).add(item);
        }

        // Sort groups
        Collections.sort(groupTitles);
    }

    @Override
    public int getGroupCount() {
        return groupTitles.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childItems.get(groupTitles.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupTitles.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childItems.get(groupTitles.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String groupTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_item, null);
        }

        TextView titleText = convertView.findViewById(R.id.groupTitle);
        titleText.setText(groupTitle);

        convertView.setMinimumHeight(dpToPx(48));

        return convertView;
    }

    private int dpToPx(int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        combo_item childItem = (combo_item) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.combo_item, null);
        }

        TextView comboText = convertView.findViewById(R.id.comboTxt);
        TextView totalText = convertView.findViewById(R.id.totalTxt);

        comboText.setText(childItem.getCombo());
        totalText.setText(childItem.getTotal());

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void updateData(List<combo_item> newItems) {
        // Reorganize the data with the new filtered items
        organizeData(newItems);
        notifyDataSetChanged();
    }
}