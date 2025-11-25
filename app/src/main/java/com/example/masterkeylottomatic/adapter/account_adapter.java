package com.example.masterkeylottomatic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.item.account_item;

import java.util.List;

public class account_adapter extends RecyclerView.Adapter<account_adapter.ItemViewHolder> {
    private List<account_item> accountItemList;
    private Context context;

    public account_adapter(Context context, List<account_item> accountItemList) {
        this.context = context;
        this.accountItemList = accountItemList;
    }

    public void setFilteredList(List<account_item> filteredList){
        this.accountItemList = filteredList;
        notifyDataSetChanged();

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        account_item item = accountItemList.get(position);
        holder.usernameTextView.setText(item.getUsername());
        holder.nameTextView.setText(item.getName());
        holder.codeTextView.setText(item.getCode());
        holder.versionTextView.setText(item.getVersion());
        holder.groupTextView.setText(item.getGroup());
    }

    public void addItem(account_item item) {
        accountItemList.add(item);
        notifyItemInserted(accountItemList.size() - 1);

    }

    @Override
    public int getItemCount() {
        return accountItemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView;
        public TextView nameTextView;
        public TextView codeTextView;
        public TextView versionTextView;
        public TextView groupTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTxt);
            nameTextView = itemView.findViewById(R.id.nameTxt);
            codeTextView = itemView.findViewById(R.id.codeTxt);
            versionTextView = itemView.findViewById(R.id.versionTxt);
            groupTextView = itemView.findViewById(R.id.groupTxt);
        }
    }
}