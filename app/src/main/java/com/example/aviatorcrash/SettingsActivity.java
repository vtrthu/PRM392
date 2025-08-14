package com.example.aviatorcrash;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.aviatorcrash.databinding.ActivitySettingsBinding;
import com.example.aviatorcrash.viewmodel.GameViewModel;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private GameViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        setupUI();
        setupObservers();
        setupClickListeners();
    }

    private void setupUI() {
        // No action bar setup needed as we have custom toolbar
    }

    private void setupObservers() {
        viewModel.getBalance().observe(this, balance -> {
            if (balance != null) {
                binding.currentBalanceText.setText(String.format("%.2f", balance));
            }
        });

        viewModel.getAutoCashoutEnabled().observe(this, enabled -> {
            if (enabled != null) {
                binding.autoCashoutSwitch.setChecked(enabled);
            }
        });

        viewModel.getAutoCashoutMultiplier().observe(this, multiplier -> {
            if (multiplier != null) {
                binding.autoCashoutSlider.setValue(multiplier.floatValue());
                binding.autoCashoutValue.setText(String.format("%.1fx", multiplier));
            }
        });
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> onBackPressed());

        binding.autoCashoutSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setAutoCashout(isChecked, binding.autoCashoutSlider.getValue());
        });

        binding.autoCashoutSlider.addOnChangeListener((slider, value, fromUser) -> {
            binding.autoCashoutValue.setText(String.format("%.1fx", value));
            if (fromUser) {
                viewModel.setAutoCashout(binding.autoCashoutSwitch.isChecked(), value);
            }
        });

        binding.resetBalanceButton.setOnClickListener(v -> showResetBalanceDialog());
    }

    private void showResetBalanceDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.reset_balance)
            .setMessage(R.string.reset_balance_confirm)
            .setPositiveButton(R.string.yes, (dialog, which) -> {
                viewModel.resetBalance();
            })
            .setNegativeButton(R.string.no, null)
            .show();
    }
}
