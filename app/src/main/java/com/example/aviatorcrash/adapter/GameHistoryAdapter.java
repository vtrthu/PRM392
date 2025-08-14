package com.example.aviatorcrash.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aviatorcrash.R;
import com.example.aviatorcrash.data.GameRecord;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class GameHistoryAdapter extends ListAdapter<GameRecord, GameHistoryAdapter.GameHistoryViewHolder> {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public GameHistoryAdapter() {
        super(new DiffUtil.ItemCallback<GameRecord>() {
            @Override
            public boolean areItemsTheSame(@NonNull GameRecord oldItem, @NonNull GameRecord newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull GameRecord oldItem, @NonNull GameRecord newItem) {
                return oldItem.getTimestamp().equals(newItem.getTimestamp()) &&
                       oldItem.getBetAmount() == newItem.getBetAmount() &&
                       oldItem.getMultiplier() == newItem.getMultiplier() &&
                       oldItem.getCashoutAmount() == newItem.getCashoutAmount() &&
                       oldItem.isWin() == newItem.isWin();
            }
        });
    }

    @NonNull
    @Override
    public GameHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_record, parent, false);
        return new GameHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameHistoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class GameHistoryViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView resultText;
        private final TextView multiplierText;
        private final TextView betAmountText;
        private final TextView timestampText;
        private final TextView resultLabel;

        public GameHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            resultText = itemView.findViewById(R.id.result_text);
            multiplierText = itemView.findViewById(R.id.multiplier_text);
            betAmountText = itemView.findViewById(R.id.bet_amount_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            resultLabel = itemView.findViewById(R.id.result_label);
        }

        public void bind(GameRecord gameRecord) {
            if (gameRecord.isWin()) {
                resultText.setText(String.format("+%.2f", gameRecord.getCashoutAmount()));
                resultText.setTextColor(itemView.getContext().getColor(R.color.success));
                resultLabel.setText("Thắng");
                resultLabel.setTextColor(itemView.getContext().getColor(R.color.success));
            } else {
                resultText.setText(String.format("-%.2f", gameRecord.getBetAmount()));
                resultText.setTextColor(itemView.getContext().getColor(R.color.error));
                resultLabel.setText("Thua");
                resultLabel.setTextColor(itemView.getContext().getColor(R.color.error));
            }

            multiplierText.setText(String.format("%.2fx", gameRecord.getMultiplier()));
            betAmountText.setText(String.format("Cược: %.2f", gameRecord.getBetAmount()));
            timestampText.setText(dateFormat.format(gameRecord.getTimestamp()));
        }
    }
}
