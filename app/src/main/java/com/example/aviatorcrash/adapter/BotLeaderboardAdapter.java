package com.example.aviatorcrash.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aviatorcrash.R;
import com.example.aviatorcrash.bot.BotEntry;
import java.util.ArrayList;
import java.util.List;

public class BotLeaderboardAdapter extends RecyclerView.Adapter<BotLeaderboardAdapter.ViewHolder> {
    
    private List<BotEntry> botEntries;

    public BotLeaderboardAdapter() {
        this.botEntries = new ArrayList<>();
    }

    public void updateBots(List<BotEntry> newBots) {
        this.botEntries.clear();
        this.botEntries.addAll(newBots);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bot_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BotEntry bot = botEntries.get(position);
        
        // Rank
        holder.playerRank.setText(String.valueOf(position + 1));
        
        // Name with special formatting for real player
        if (bot.isPlayer()) {
            holder.playerName.setText("👤 " + bot.getName());
            holder.playerName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
        } else {
            holder.playerName.setText(bot.getName());
            holder.playerName.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
        }
        
        // Bet amount (format as K for thousands)
        String betText = formatAmount(bot.getBetAmount());
        holder.playerBet.setText(betText);
        
        // Status (cashout multiplier or crashed)
        String statusText;
        int statusColor;
        
        switch (bot.getState()) {
            case PENDING:
                statusText = "—";
                statusColor = R.color.text_secondary;
                break;
            case BET_PLACED:
                statusText = "✈️";
                statusColor = R.color.primary;
                break;
            case CASHED_OUT:
                statusText = String.format("%.2fx", bot.getActualCashout());
                statusColor = R.color.success;
                break;
            case CRASHED:
                statusText = "💥";
                statusColor = R.color.error;
                break;
            default:
                statusText = "—";
                statusColor = R.color.text_secondary;
                break;
        }
        
        holder.playerStatus.setText(statusText);
        holder.playerStatus.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), statusColor));
    }

    @Override
    public int getItemCount() {
        return botEntries.size();
    }

    private String formatAmount(double amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000);
        } else if (amount >= 1000) {
            return String.format("%.0fK", amount / 1000);
        } else {
            return String.format("%.0f", amount);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView playerRank;
        TextView playerName;
        TextView playerBet;
        TextView playerStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            playerRank = itemView.findViewById(R.id.player_rank);
            playerName = itemView.findViewById(R.id.player_name);
            playerBet = itemView.findViewById(R.id.player_bet);
            playerStatus = itemView.findViewById(R.id.player_status);
        }
    }
}
