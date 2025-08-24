package com.example.aviatorcrash;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aviatorcrash.auth.AuthManager;
import com.example.aviatorcrash.auth.AccountType;
import com.example.aviatorcrash.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new AuthManager(this);
        
        // Check if user is logged in
        if (!authManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            startActivity(intent);
        });

        binding.historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        });

        binding.settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        binding.logoutButton.setOnClickListener(v -> {
            authManager.logout();
            navigateToLogin();
        });

        binding.exitButton.setOnClickListener(v -> {
            finish();
        });

        binding.depositButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DepositActivity.class);
            startActivity(intent);
        });

        binding.withdrawButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, WithdrawActivity.class);
            startActivity(intent);
        });
    }


    
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    


    @Override
    public void onBackPressed() {
        Toast.makeText(this, R.string.exit, Toast.LENGTH_SHORT).show();
    }
}
