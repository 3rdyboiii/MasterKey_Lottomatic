package com.example.masterkey_luckythree.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.item.hits_Item;

import java.util.List;

public class hits_adapter extends RecyclerView.Adapter<hits_adapter.ItemViewHolder> {

    private final List<hits_Item> hitsList;

    public hits_adapter(List<hits_Item> hitsList) {
        this.hitsList = hitsList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hits_item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        hits_Item item = hitsList.get(position);

        holder.idTextView.setText("ID: " + item.getId());
        holder.agentTextView.setText("Agent: " + item.getAgent());
        holder.comboTextView.setText(item.getCombo());
        holder.betsTextView.setText("₱" + item.getBets().toString());
        holder.prizeTextView.setText("₱" + item.getPrize().toString());
        holder.transTextView.setText(item.getTranscode());
        holder.gameTextView.setText("Game: " + item.getGame());
        holder.drawTextView.setText("Draw: " + item.getDraw());
    }

    @Override
    public int getItemCount() {
        return hitsList.size();
    }

    public void addItem(hits_Item item) {
        hitsList.add(item);
        notifyItemInserted(hitsList.size() - 1);

    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView idTextView;
        public TextView agentTextView;
        public TextView comboTextView;
        public TextView betsTextView;
        public TextView prizeTextView;
        public TextView transTextView;
        public TextView gameTextView;
        public TextView drawTextView;
        public LinearLayout cardView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.idTxt);
            agentTextView = itemView.findViewById(R.id.agentTxt);
            comboTextView = itemView.findViewById(R.id.comboTxt);
            betsTextView = itemView.findViewById(R.id.betTxt);
            prizeTextView = itemView.findViewById(R.id.prizeTxt);
            transTextView = itemView.findViewById(R.id.transcodeTxt);
            gameTextView = itemView.findViewById(R.id.gameTxt);
            drawTextView = itemView.findViewById(R.id.drawTxt);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
