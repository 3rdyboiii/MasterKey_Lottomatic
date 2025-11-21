package com.example.masterkey_luckythree.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.item.combo_item;

import java.util.List;

public class ComboPagerAdapter extends RecyclerView.Adapter<ComboPagerAdapter.ColumnViewHolder> {
    private List<List<combo_item>> groupedItems;

    public ComboPagerAdapter(List<List<combo_item>> groupedItems) {
        this.groupedItems = groupedItems;
    }

    @Override
    public ColumnViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_combo_column, parent, false);
        return new ColumnViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ColumnViewHolder holder, int position) {
        List<combo_item> columnItems = groupedItems.get(position);
        ComboColumnAdapter adapter = new ComboColumnAdapter(columnItems);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerView.setAdapter(adapter);

        // Set header for this column (e.g., "0-1", "1-2", etc.)
        String header = position + "-" + (position + 1);
        // You might want to add a header view to your column layout
    }

    @Override
    public int getItemCount() {
        return groupedItems.size();
    }

    static class ColumnViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;

        ColumnViewHolder(View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recyclerView);
        }
    }
}
