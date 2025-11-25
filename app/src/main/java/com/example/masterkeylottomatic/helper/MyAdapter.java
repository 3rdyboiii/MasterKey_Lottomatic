package com.example.masterkeylottomatic.helper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkeylottomatic.R;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private List<MyTableList> myTableLists;

    public MyAdapter(List<MyTableList> myTableLists) {
        this.myTableLists = myTableLists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyTableList myTableList = myTableLists.get(position);
        holder.name.setText(myTableList.getName().toString());
        holder.total.setText(myTableList.getTotal().toString());
    }

    @Override
    public int getItemCount() {
        return myTableLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, total;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.nameTxt);
        }
    }
}
