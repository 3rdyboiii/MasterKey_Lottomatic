package com.example.masterkeylottomatic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.item.combo_item;

import java.util.List;

public class ComboColumnAdapter extends RecyclerView.Adapter<ComboColumnAdapter.ComboViewHolder> {
    private List<combo_item> items;

    public ComboColumnAdapter(List<combo_item> items) {
        this.items = items;
    }

    @Override
    public ComboViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.combo_item, parent, false);
        return new ComboViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ComboViewHolder holder, int position) {
        combo_item item = items.get(position);
        holder.comboText.setText(item.getCombo());
        holder.totalText.setText(item.getTotal());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ComboViewHolder extends RecyclerView.ViewHolder {
        TextView comboText;
        TextView totalText;

        ComboViewHolder(View itemView) {
            super(itemView);
            comboText = itemView.findViewById(R.id.comboTxt);
            totalText = itemView.findViewById(R.id.totalTxt);
        }
    }
}
