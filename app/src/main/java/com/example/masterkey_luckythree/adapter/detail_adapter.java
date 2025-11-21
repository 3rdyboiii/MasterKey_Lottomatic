package com.example.masterkey_luckythree.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.item.detail_Item;

import java.util.List;

public class detail_adapter extends RecyclerView.Adapter<detail_adapter.ItemViewHolder> {
    private List<detail_Item> detailItemList;

    public detail_adapter(List<detail_Item> detailItemList) {
        this.detailItemList = detailItemList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.detail_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        detail_Item item = detailItemList.get(position);
        holder.usernameTextView.setText(item.getUsername());
        holder.nameTextView.setText(item.getName());
        holder.codeTextView.setText(item.getCode());
    }

    public void addItem(detail_Item item) {
        detailItemList.add(item);
        notifyItemInserted(detailItemList.size() - 1);

    }

    @Override
    public int getItemCount() {
        return detailItemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView;
        public TextView nameTextView;
        public TextView codeTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTxt);
            nameTextView = itemView.findViewById(R.id.nameTxt);
            codeTextView = itemView.findViewById(R.id.codeTxt);
        }
    }
}
