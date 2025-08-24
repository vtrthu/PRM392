package com.example.aviatorcrash;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aviatorcrash.databinding.ActivityWithdrawBinding;
import com.example.aviatorcrash.viewmodel.GameViewModel;

public class WithdrawActivity extends AppCompatActivity {
    private ActivityWithdrawBinding binding;
    private GameViewModel gameViewModel;
    // Educational purpose: ALL withdrawals will be blocked (no limit needed)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWithdrawBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gameViewModel = new GameViewModel(getApplication());

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.withdrawButton.setOnClickListener(v -> {
            try {
                processWithdraw();
            } catch (Exception e) {
                Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
            }
        });

        binding.backToMainButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void processWithdraw() {
        // Get input values
        String amountStr = binding.withdrawAmountInput.getText().toString().trim();
        String bankAccount = binding.bankAccountInput.getText().toString().trim();
        String bankName = binding.bankNameInput.getText().toString().trim();
        String accountHolder = binding.accountHolderInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(amountStr)) {
            binding.withdrawAmountInput.setError("Vui lòng nhập số tiền");
            binding.withdrawAmountInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(bankAccount)) {
            binding.bankAccountInput.setError("Vui lòng nhập số tài khoản");
            binding.bankAccountInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(bankName)) {
            binding.bankNameInput.setError("Vui lòng nhập tên ngân hàng");
            binding.bankNameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(accountHolder)) {
            binding.accountHolderInput.setError("Vui lòng nhập tên chủ tài khoản");
            binding.accountHolderInput.requestFocus();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                binding.withdrawAmountInput.setError("Số tiền phải lớn hơn 0");
                binding.withdrawAmountInput.requestFocus();
                return;
            }
             if (amount < 100000) {
                binding.withdrawAmountInput.setError("Số tiền rút tối thiểu là 100,000 coin");
                binding.withdrawAmountInput.requestFocus();
                return;
            }

            double currentBalance = gameViewModel.getBalance().getValue() != null ? 
                    gameViewModel.getBalance().getValue() : 0;

            if (amount > currentBalance) {
                binding.withdrawAmountInput.setError("Số tiền rút không được vượt quá số dư");
                binding.withdrawAmountInput.requestFocus();
                return;
            }

            // Educational purpose: ALL withdrawals are blocked to show gambling app scam nature
            // No matter the amount, user will be "account suspended"
            showAccountSuspendedDialog(amount);
            
        } catch (NumberFormatException e) {
            binding.withdrawAmountInput.setError("Số tiền không hợp lệ");
            binding.withdrawAmountInput.requestFocus();
        }
    }

    private void showAccountSuspendedDialog(double amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("⚠️ TÀI KHOẢN BỊ VÔ HIỆU HÓA")
                .setMessage("Tài khoản của bạn đã bị vô hiệu hóa do vi phạm quy định của hệ thống.\n\n" +
                        "Số tiền yêu cầu rút: " + String.format("%,.0f", amount) + " coin\n" +
                        "Lý do: Hệ thống phát hiện hoạt động bất thường")
                .setPositiveButton("Xem thêm", (dialog, which) -> {
                    showEducationalDialog(amount);
                })
                .setNegativeButton("Hủy", null)
                .setCancelable(false)
                .show();
    }

    private void showEducationalDialog(double amount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null);
        
        // Create custom layout for checkbox and message
        View customView = LayoutInflater.from(this).inflate(android.R.layout.simple_list_item_1, null);
        
        builder.setTitle("🚨 CẢNH BÁO")
                .setMessage("Những app cá độ lừa đảo sẽ có thể khóa tài khoản của bạn bất cứ lúc nào, " +
                        "bất kể bạn đã thắng bao nhiêu tiền!\n\n" +
                        "Đây là một trong những chiêu trò lừa đảo phổ biến của các app cờ bạc online.")
                .setView(createEducationalView())
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Process withdrawal and go back to menu directly
                    gameViewModel.withdrawMoney(amount);
                    Toast.makeText(this, "Số dư đã được cập nhật. Quay về menu chính...", Toast.LENGTH_SHORT).show();
                    
                    // Go back to main menu
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .setCancelable(false);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Find checkbox by ID
        CheckBox checkBox = dialog.findViewById(12345);
        
        // Hide positive button initially
        if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(android.view.View.GONE);
            android.util.Log.d("WithdrawActivity", "Button hidden initially");
        }
        
        // Show button when checkbox is checked
        if (checkBox != null) {
            android.util.Log.d("WithdrawActivity", "CheckBox found!");
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                android.util.Log.d("WithdrawActivity", "Checkbox changed: " + isChecked);
                if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) {
                    if (isChecked) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(android.view.View.VISIBLE);
                        android.util.Log.d("WithdrawActivity", "Button shown");
                    } else {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(android.view.View.GONE);
                        android.util.Log.d("WithdrawActivity", "Button hidden");
                    }
                }
            });
        } else {
            android.util.Log.e("WithdrawActivity", "CheckBox not found by ID!");
        }
    }

    private View createEducationalView() {
        // Create a simple layout with checkbox and message
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        CheckBox checkBox = new CheckBox(this);
        checkBox.setId(12345); // Set specific ID for easy finding
        checkBox.setText("Tôi đã hiểu và đồng ý với quy định");
        checkBox.setTextColor(android.graphics.Color.parseColor("#333333"));
        checkBox.setTextSize(16);

        layout.addView(checkBox);
        return layout;
    }

    private void showSuccessDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    // Show confirmation that balance has been deducted
                    Toast.makeText(this, "Số dư đã được cập nhật. Quay về menu chính...", Toast.LENGTH_SHORT).show();
                    
                    // Go back to main menu after successful withdrawal
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
