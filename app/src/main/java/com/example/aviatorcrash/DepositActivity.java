package com.example.aviatorcrash;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aviatorcrash.databinding.ActivityDepositBinding;
import com.example.aviatorcrash.viewmodel.GameViewModel;

public class DepositActivity extends AppCompatActivity {
    private ActivityDepositBinding binding;
    private GameViewModel gameViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDepositBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        gameViewModel = new GameViewModel(getApplication());

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.depositButton.setOnClickListener(v -> {
            try {
                processDeposit();
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

    private void processDeposit() {
        // Get input values
        String cardNumber = binding.cardNumberInput.getText().toString().trim();
        String cardHolder = binding.cardHolderInput.getText().toString().trim();
        String expiryDate = binding.expiryDateInput.getText().toString().trim();
        String cvv = binding.cvvInput.getText().toString().trim();
        String amountStr = binding.amountInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(cardNumber)) {
            binding.cardNumberInput.setError("Vui lòng nhập số thẻ");
            binding.cardNumberInput.requestFocus();
            return;
        }

        if (cardNumber.length() < 16) {
            binding.cardNumberInput.setError("Số thẻ phải có ít nhất 16 số");
            binding.cardNumberInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(cardHolder)) {
            binding.cardHolderInput.setError("Vui lòng nhập tên chủ thẻ");
            binding.cardHolderInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(expiryDate)) {
            binding.expiryDateInput.setError("Vui lòng nhập ngày hết hạn");
            binding.expiryDateInput.requestFocus();
            return;
        }

        if (!expiryDate.matches("\\d{2}/\\d{2}")) {
            binding.expiryDateInput.setError("Định dạng: MM/YY");
            binding.expiryDateInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(cvv)) {
            binding.cvvInput.setError("Vui lòng nhập CVV");
            binding.cvvInput.requestFocus();
            return;
        }

        if (cvv.length() < 3 || cvv.length() > 4) {
            binding.cvvInput.setError("CVV phải có 3-4 số");
            binding.cvvInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            binding.amountInput.setError("Vui lòng nhập số tiền");
            binding.amountInput.requestFocus();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                binding.amountInput.setError("Số tiền phải lớn hơn 0");
                binding.amountInput.requestFocus();
                return;
            }

            if (amount < 100000) {
                binding.amountInput.setError("Số tiền tối thiểu: 100,000 coin");
                binding.amountInput.requestFocus();
                return;
            }

            // Process deposit
            gameViewModel.depositMoney(amount);
            
            // Show success message
            Toast.makeText(this, "✅ Nạp tiền thành công! Số dư: " + 
                    String.format("%,.0f", gameViewModel.getBalance().getValue()) + " coin", 
                    Toast.LENGTH_LONG).show();
            
            // Go back to main menu
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            
        } catch (NumberFormatException e) {
            binding.amountInput.setError("Số tiền không hợp lệ");
            binding.amountInput.requestFocus();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
