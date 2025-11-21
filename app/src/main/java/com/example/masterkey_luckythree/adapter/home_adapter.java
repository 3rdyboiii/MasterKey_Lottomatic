package com.example.masterkey_luckythree.adapter;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.helper.ConSQL;
import com.example.masterkey_luckythree.item.entry_item;
import com.example.masterkey_luckythree.item.home_item;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class home_adapter extends RecyclerView.Adapter<home_adapter.ItemViewHolder> {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Context context;
    private List<home_item> homeItemList;

    public home_adapter(Context context, List<home_item> homeItemList) {
        this.context = context;
        this.homeItemList = homeItemList;
    }

    public void setFilteredList(List<home_item> filteredList){
        this.homeItemList = filteredList;
        notifyDataSetChanged();

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        home_item item = homeItemList.get(position);
        holder.firstdraw.setText("₱" + item.getFirstdraw().toString());
        holder.seconddraw.setText("₱" + item.getSeconddraw().toString());
        holder.thirddraw.setText("₱" + item.getThirddraw().toString());
        holder.first3Ddraw.setText("₱" + item.getFirst3Ddraw().toString());
        holder.second3Ddraw.setText("₱" + item.getSecond3Ddraw().toString());
        holder.third3Ddraw.setText("₱" + item.getThird3Ddraw().toString());
        holder.third4Ddraw.setText("₱" + item.getThird4Ddraw().toString());
        holder.hits2pm2Damount.setText("₱" + item.getHits2pm2Damount().toString());
        holder.hits5pm2Damount.setText("₱" + item.getHits5pm2Damount().toString());
        holder.hits9pm2Damount.setText("₱" + item.getHits9pm2Damount().toString());
        holder.hits2pm3Damount.setText("₱" + item.getHits2pm3Damount().toString());
        holder.hits5pm3Damount.setText("₱" + item.getHits5pm3Damount().toString());
        holder.hits9pm3Damount.setText("₱" + item.getHits9pm3Damount().toString());
        holder.hits9pm4Damount.setText("₱" + item.getHits9pm4Damount().toString());
        holder.amountTextView.setText("₱" + item.getAmount().toString());
        holder.playerTextView.setText(item.getAgent());
        holder.groupTextView.setText(item.getGroup());
    }

    public void addItem(home_item item) {
        homeItemList.add(item);
        notifyItemInserted(homeItemList.size() - 1);

    }

    @Override
    public int getItemCount() {
        return homeItemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView firstdraw;
        public TextView seconddraw;
        public TextView thirddraw;
        public TextView first3Ddraw;
        public TextView second3Ddraw;
        public TextView third3Ddraw;
        public TextView third4Ddraw;
        public TextView hits2pm2Damount;
        public TextView hits5pm2Damount;
        public TextView hits9pm2Damount;
        public TextView hits2pm3Damount;
        public TextView hits5pm3Damount;
        public TextView hits9pm3Damount;
        public TextView hits9pm4Damount;
        public TextView amountTextView;
        public TextView playerTextView;
        public TextView groupTextView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            firstdraw = itemView.findViewById(R.id.firstdrawTxt);
            seconddraw = itemView.findViewById(R.id.seconddrawTxt);
            thirddraw = itemView.findViewById(R.id.thirddrawTxt);
            first3Ddraw = itemView.findViewById(R.id.first3ddrawTxt);
            second3Ddraw = itemView.findViewById(R.id.second3ddrawTxt);
            third3Ddraw = itemView.findViewById(R.id.third3ddrawTxt);
            third4Ddraw = itemView.findViewById(R.id.third4ddrawTxt);
            hits2pm2Damount = itemView.findViewById(R.id.hits2pm2DTxt);
            hits5pm2Damount = itemView.findViewById(R.id.hits5pm2DTxt);
            hits9pm2Damount = itemView.findViewById(R.id.hits9pm2DTxt);
            hits2pm3Damount = itemView.findViewById(R.id.hits2pm3DTxt);
            hits5pm3Damount = itemView.findViewById(R.id.hits5pm3DTxt);
            hits9pm3Damount = itemView.findViewById(R.id.hits9pm3DTxt);
            hits9pm4Damount = itemView.findViewById(R.id.hits9pm4DTxt);
            amountTextView = itemView.findViewById(R.id.amountTxt);
            playerTextView = itemView.findViewById(R.id.agentTxt);
            groupTextView = itemView.findViewById(R.id.groupTxt);

        }
    }
}
