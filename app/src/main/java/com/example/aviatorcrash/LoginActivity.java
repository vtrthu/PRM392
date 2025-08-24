package com.example.aviatorcrash;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aviatorcrash.auth.AuthManager;
import com.example.aviatorcrash.databinding.ActivityLoginBinding;

/**
 * Login Activity with realistic username/password authentication
 * Hides educational intent during classroom demo
 */
public class LoginActivity extends AppCompatActivity {
    
    private ActivityLoginBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new AuthManager(this);
        
        // FORCE LOGOUT for demo purposes - always show login
        authManager.logout();
        android.util.Log.d("LoginActivity", "Forced logout - showing login screen");

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.loginButton.setOnClickListener(v -> {
            attemptLogin();
        });
        
        binding.registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
        
        // Allow login on Enter key press
        binding.passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            attemptLogin();
            return true;
        });
    }

    private void attemptLogin() {
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();
        
        // Validate input
        if (TextUtils.isEmpty(username)) {
            binding.usernameInput.setError("Username required");
            binding.usernameInput.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            binding.passwordInput.setError("Password required");
            binding.passwordInput.requestFocus();
            return;
        }
        
        // Clear previous errors
        binding.errorMessage.setVisibility(android.view.View.GONE);
        binding.usernameInput.setError(null);
        binding.passwordInput.setError(null);
        
        // Attempt authentication
        boolean loginSuccessful = authManager.login(username, password);
        
        if (loginSuccessful) {
            // Neutral success message - no account type revealed
            Toast.makeText(this, "✅ Login successful! Welcome to Aviator Crash", 
                    Toast.LENGTH_SHORT).show();
            
            navigateToMain();
        } else {
            // Show error
            binding.errorMessage.setVisibility(android.view.View.VISIBLE);
            
            // Animate error
            binding.errorMessage.setAlpha(0f);
            binding.errorMessage.animate()
                    .alpha(1f)
                    .setDuration(300);
            
            // Clear password field
            binding.passwordInput.setText("");
            binding.passwordInput.requestFocus();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent back navigation from login screen
        Toast.makeText(this, "Please login to continue", Toast.LENGTH_SHORT).show();
    }
}
