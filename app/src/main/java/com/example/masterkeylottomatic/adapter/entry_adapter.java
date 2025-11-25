package com.example.masterkeylottomatic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.masterkeylottomatic.R;
import com.example.masterkeylottomatic.item.entry_item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class entry_adapter extends RecyclerView.Adapter<entry_adapter.ItemViewHolder> {
    private List<entry_item> entryItemList;
    private Set<Integer> selectedPositions = new HashSet<>();

    public entry_adapter(List<entry_item> entryItemList) {
        this.entryItemList = entryItemList;
    }

    public void setFilteredList(List<entry_item> filteredList){
        this.entryItemList = filteredList;
        notifyDataSetChanged();

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.entry_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        entry_item item = entryItemList.get(position);

        holder.idTextView.setText("ID: " + item.getId());
        holder.agentTextView.setText("Agent: " + item.getAgent());
        holder.comboTextView.setText(item.getCombo());
        holder.betsTextView.setText("₱" + item.getBets().toString());
        holder.prizeTextView.setText("₱" + item.getPrize().toString());
        holder.transTextView.setText(item.getTranscode());
        holder.gameTextView.setText("Game: " + item.getGame());
        holder.drawTextView.setText("Draw: " + item.getDraw());

        holder.cardView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.anim_four));

        if (selectedPositions.contains(position)) {
            holder.cardView.setBackgroundResource(R.drawable.custom_button);
            holder.cardView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red)); // Set the default color
        } else {
            holder.cardView.setBackgroundResource(R.drawable.custom_button_bg);
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedPositions.contains(position)) {
                selectedPositions.remove(position); // Remove from selection
            } else {
                selectedPositions.add(position); // Add to selection
            }

            // Notify the adapter to update the background color
            notifyItemChanged(position);

            // Notify the listener about the selection change
            if (onSelectionChangedListener != null) {
                onSelectionChangedListener.onSelectionChanged(!selectedPositions.isEmpty());
            }
        });
    }

    public List<entry_item> getSelectedItems() {
        List<entry_item> selectedItems = new ArrayList<>();
        for (int position : selectedPositions) {
            if (position >= 0 && position < entryItemList.size()) {
                selectedItems.add(entryItemList.get(position));
            }
        }
        return selectedItems;
    }

    public void addItem(entry_item item) {
        entryItemList.add(item);
        notifyItemInserted(entryItemList.size() - 1);

    }

    @Override
    public int getItemCount() {
        return entryItemList.size();
    }

    public void clearSelectedItems() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean hasSelectedItems);
    }

    private OnSelectionChangedListener onSelectionChangedListener;

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.onSelectionChangedListener = listener;
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
