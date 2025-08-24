package com.example.aviatorcrash;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.aviatorcrash.auth.AuthManager;
import com.example.aviatorcrash.databinding.ActivitySettingsBinding;
import com.example.aviatorcrash.viewmodel.GameViewModel;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private GameViewModel viewModel;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        authManager = new AuthManager(this);
        
        setupUI();
        setupObservers();
        setupClickListeners();
    }

    private void setupUI() {
        // Update account type display
        if (authManager.isAdmin()) {
            binding.accountTypeText.setText("Admin");
        } else {
            binding.accountTypeText.setText("Player");
        }

        // Show/hide change password button based on account type
        if (authManager.canChangePassword()) {
            binding.changePasswordButton.setVisibility(View.VISIBLE);
        } else {
            binding.changePasswordButton.setVisibility(View.GONE);
        }
    }

    private void setupObservers() {
        // No auto cashout observers needed
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        binding.changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
            finish();
        });

        // No auto cashout listeners needed
    }



    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}