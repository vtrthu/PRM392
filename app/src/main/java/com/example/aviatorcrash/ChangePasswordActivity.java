package com.example.aviatorcrash;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.aviatorcrash.auth.AuthManager;
import com.example.aviatorcrash.databinding.ActivityChangePasswordBinding;

public class ChangePasswordActivity extends AppCompatActivity {
    private ActivityChangePasswordBinding binding;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = new AuthManager(this);
        setupUI();
        setupClickListeners();
        setupTextWatchers();
    }

    private void setupUI() {
        // Initially disable the change password button
        binding.changePasswordButton.setEnabled(false);
        binding.changePasswordButton.setAlpha(0.5f);
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            finish();
        });

        binding.cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            finish();
        });

        binding.changePasswordButton.setOnClickListener(v -> {
            attemptPasswordChange();
        });
    }

    private void setupTextWatchers() {
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateInputs();
            }
        };

        binding.currentPasswordInput.addTextChangedListener(validationWatcher);
        binding.newPasswordInput.addTextChangedListener(validationWatcher);
        binding.confirmPasswordInput.addTextChangedListener(validationWatcher);
    }

    private void validateInputs() {
        String currentPassword = binding.currentPasswordInput.getText().toString().trim();
        String newPassword = binding.newPasswordInput.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordInput.getText().toString().trim();

        // Update requirement indicators
        updateRequirementIndicator(binding.requirementLength, newPassword.length() >= 6);
        updateRequirementIndicator(binding.requirementDifferent, 
            !TextUtils.isEmpty(newPassword) && !newPassword.equals(currentPassword));
        updateRequirementIndicator(binding.requirementMatch, 
            !TextUtils.isEmpty(confirmPassword) && newPassword.equals(confirmPassword));

        // Enable/disable button based on all requirements
        boolean allValid = !TextUtils.isEmpty(currentPassword) &&
                          newPassword.length() >= 6 &&
                          !newPassword.equals(currentPassword) &&
                          newPassword.equals(confirmPassword);

        binding.changePasswordButton.setEnabled(allValid);
        binding.changePasswordButton.setAlpha(allValid ? 1.0f : 0.5f);
    }

    private void updateRequirementIndicator(android.widget.TextView textView, boolean isValid) {
        if (isValid) {
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
            textView.setText(textView.getText().toString().replace("•", "✓"));
        } else {
            textView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            String text = textView.getText().toString().replace("✓", "•");
            textView.setText(text);
        }
    }

    private void attemptPasswordChange() {
        String currentPassword = binding.currentPasswordInput.getText().toString().trim();
        String newPassword = binding.newPasswordInput.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordInput.getText().toString().trim();

        // Final validation
        if (TextUtils.isEmpty(currentPassword)) {
            showError("Vui lòng nhập mật khẩu hiện tại");
            return;
        }

        if (TextUtils.isEmpty(newPassword)) {
            showError("Vui lòng nhập mật khẩu mới");
            return;
        }

        if (newPassword.length() < 6) {
            showError("Mật khẩu mới phải có ít nhất 6 ký tự");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Mật khẩu xác nhận không khớp");
            return;
        }

        if (newPassword.equals(currentPassword)) {
            showError("Mật khẩu mới phải khác mật khẩu hiện tại");
            return;
        }

        // Disable button during processing
        binding.changePasswordButton.setEnabled(false);
        binding.changePasswordButton.setText("Đang xử lý...");

        // Attempt to change password
        if (authManager.changePassword(currentPassword, newPassword)) {
            showSuccess("✅ Đổi mật khẩu thành công!");
            
            // Navigate back to settings after delay
            binding.getRoot().postDelayed(() -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }, 1500);
        } else {
            showError("❌ Đổi mật khẩu thất bại. Kiểm tra lại mật khẩu hiện tại.");
            binding.changePasswordButton.setEnabled(true);
            binding.changePasswordButton.setText("Đổi mật khẩu");
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }
}
