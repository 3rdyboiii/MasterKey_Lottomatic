package com.example.masterkeylottomatic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.item.combo_item;

import java.util.List;

public class combo_adapter extends RecyclerView.Adapter<combo_adapter.ItemViewHolder> {
    private List<combo_item> comboItemList;

    public combo_adapter(List<combo_item> filteredList) {
        this.comboItemList = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.combo_item, parent, false);
        return new ItemViewHolder(view);
    }

    public int getItemCount() {
        return comboItemList.size();
    }

    @Override
    public void onBindViewHolder(combo_adapter.ItemViewHolder holder, int position) {
        combo_item item = comboItemList.get(position);

        holder.comboTextView.setText(item.getCombo());
        holder.totalTextView.setText(item.getTotal());
    }

    public void addItem(combo_item item) {
        comboItemList.add(item);
        notifyItemInserted(comboItemList.size() - 1);

    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView comboTextView;
        public TextView totalTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            comboTextView = itemView.findViewById(R.id.comboTxt);
            totalTextView = itemView.findViewById(R.id.totalTxt);
        }
    }
}
