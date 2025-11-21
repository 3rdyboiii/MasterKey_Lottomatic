package com.example.masterkey_luckythree.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkey_luckythree.R;
import com.example.masterkey_luckythree.item.game_item;

import java.util.List;

public class game_adapter extends RecyclerView.Adapter<game_adapter.ItemViewHolder> {
    private Context context;
    private List<game_item> gameItemList;

    public game_adapter(List<game_item> gameItemList) {
        this.context = context;
        this.gameItemList = gameItemList;
    }

    public void setFilteredList(List<game_item> filteredList){
        this.gameItemList = filteredList;
        notifyDataSetChanged();

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.total_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        game_item item = gameItemList.get(position);
        holder.gameName.setText(item.getGameName());

        if (item.is4DGame()) {
            // Hide 2pm and 5pm views for 4D game
            holder.firstdraw.setVisibility(View.GONE);
            holder.seconddraw.setVisibility(View.GONE);
            holder.firsthitsdraw.setVisibility(View.GONE);
            holder.secondhitsdraw.setVisibility(View.GONE);
            holder.firsttext.setVisibility(View.GONE);
            holder.secondtext.setVisibility(View.GONE);
            holder.firsthitstext.setVisibility(View.GONE);
            holder.secondhitstext.setVisibility(View.GONE);

            holder.thirddraw.setText("₱" + item.getDraw9pm().toString());
            holder.thirdhitsdraw.setText("₱" + item.getHits9pm().toString());
        } else {
            holder.firstdraw.setVisibility(View.VISIBLE);
            holder.seconddraw.setVisibility(View.VISIBLE);
            holder.firsthitsdraw.setVisibility(View.VISIBLE);
            holder.secondhitsdraw.setVisibility(View.VISIBLE);
            holder.firsttext.setVisibility(View.VISIBLE);
            holder.secondtext.setVisibility(View.VISIBLE);
            holder.firsthitstext.setVisibility(View.VISIBLE);
            holder.secondhitstext.setVisibility(View.VISIBLE);

            holder.firstdraw.setText("₱" + item.getDraw2pm().toString());
            holder.seconddraw.setText("₱" + item.getDraw5pm().toString());
            holder.thirddraw.setText("₱" + item.getDraw9pm().toString());
            holder.firsthitsdraw.setText("₱" + item.getHits2pm().toString());
            holder.secondhitsdraw.setText("₱" + item.getHits5pm().toString());
            holder.thirdhitsdraw.setText("₱" + item.getHits9pm().toString());
        }

        holder.hitsTextView.setText(item.calculateTotalHits());
        holder.amountTextView.setText("₱" + item.getGross().toString());
    }

    public void addItem(game_item item) {
        gameItemList.add(item);
        notifyItemInserted(gameItemList.size() - 1);

    }

    @Override
    public int getItemCount() {
        return gameItemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView firstdraw;
        public TextView seconddraw;
        public TextView thirddraw;
        public TextView firsthitsdraw;
        public TextView secondhitsdraw;
        public TextView thirdhitsdraw;
        public TextView amountTextView;
        public TextView hitsTextView;
        public TextView gameName;
        public TextView firsttext;
        public TextView secondtext;
        public TextView firsthitstext;
        public TextView secondhitstext;

        public ItemViewHolder(View itemView) {
            super(itemView);
            firstdraw = itemView.findViewById(R.id.firstdrawTxt);
            seconddraw = itemView.findViewById(R.id.seconddrawTxt);
            thirddraw = itemView.findViewById(R.id.thirddrawTxt);
            firsthitsdraw = itemView.findViewById(R.id.firsthitsTxt);
            secondhitsdraw = itemView.findViewById(R.id.secondhitsTxt);
            thirdhitsdraw = itemView.findViewById(R.id.thirdhitsTxt);
            amountTextView = itemView.findViewById(R.id.grossTxt);
            hitsTextView = itemView.findViewById(R.id.hitsTxt);
            gameName = itemView.findViewById(R.id.gameTxt);
            firsttext = itemView.findViewById(R.id.firstText);
            secondtext = itemView.findViewById(R.id.secondText);
            firsthitstext = itemView.findViewById(R.id.firsthitsText);
            secondhitstext = itemView.findViewById(R.id.secondhitsText);

        }
    }
}
