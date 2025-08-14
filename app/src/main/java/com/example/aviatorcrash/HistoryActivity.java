package com.example.aviatorcrash;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aviatorcrash.adapter.GameHistoryAdapter;
import com.example.aviatorcrash.databinding.ActivityHistoryBinding;
import com.example.aviatorcrash.viewmodel.GameViewModel;

public class HistoryActivity extends AppCompatActivity {
    private ActivityHistoryBinding binding;
    private GameViewModel viewModel;
    private GameHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        setupUI();
        setupRecyclerView();
        setupObservers();
        setupClickListeners();
    }

    private void setupUI() {
        // No action bar setup needed as we have custom toolbar
    }

    private void setupRecyclerView() {
        adapter = new GameHistoryAdapter();
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyRecyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getGameHistory().observe(this, gameRecords -> {
            if (gameRecords != null && !gameRecords.isEmpty()) {
                adapter.submitList(gameRecords);
                binding.historyRecyclerView.setVisibility(android.view.View.VISIBLE);
                binding.emptyState.setVisibility(android.view.View.GONE);
            } else {
                binding.historyRecyclerView.setVisibility(android.view.View.GONE);
                binding.emptyState.setVisibility(android.view.View.VISIBLE);
            }
        });

        viewModel.getWinRate().observe(this, winRate -> {
            if (winRate != null) {
                binding.winRateText.setText(String.format("%.1f%%", winRate));
            }
        });

        viewModel.getTotalGames().observe(this, totalGames -> {
            if (totalGames != null) {
                binding.totalGamesText.setText(String.valueOf(totalGames));
            }
        });
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> onBackPressed());

        binding.clearHistoryButton.setOnClickListener(v -> showClearHistoryDialog());
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.clear_history)
            .setMessage(R.string.clear_history_confirm)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                viewModel.clearHistory();
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }
}
