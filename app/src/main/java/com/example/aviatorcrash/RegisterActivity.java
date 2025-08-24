package com.example.aviatorcrash;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aviatorcrash.auth.AuthManager;
import com.example.aviatorcrash.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new AuthManager(this);

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.registerButton.setOnClickListener(v -> {
            try {
                register();
            } catch (Exception e) {
                Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });

        binding.backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void register() {
        String username = binding.usernameInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(username)) {
            binding.usernameInput.setError("Vui lòng nhập tên đăng nhập");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordInput.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            binding.passwordInput.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordInput.setError("Mật khẩu xác nhận không khớp");
            return;
        }

        // Check if username already exists
        if (authManager.isUsernameExists(username)) {
            binding.usernameInput.setError("Tên đăng nhập đã tồn tại");
            return;
        }

        // Register new user
        if (authManager.register(username, password)) {
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập", Toast.LENGTH_LONG).show();
            
            // Go back to login
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại, vui lòng thử lại", Toast.LENGTH_SHORT).show();
        }
    }
}
