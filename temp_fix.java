    private void addToBetAmount(double amount) {
        try {
            String currentText = binding.betAmountInput.getText().toString().trim();
            long currentAmount = 0;
            
            if (!currentText.isEmpty()) {
                try {
                    currentAmount = Long.parseLong(currentText);
                } catch (NumberFormatException e) {
                    currentAmount = 0;
                }
            }
            
            // Add new amount
            long newTotal = currentAmount + (long) amount;
            
            // Check if total exceeds balance
            Double currentBalance = viewModel.getBalance().getValue();
            if (currentBalance != null && newTotal > currentBalance) {
                // Show warning and set to max possible
                newTotal = currentBalance.longValue();
                Toast.makeText(this, "⚠️ Tổng tiền cược không được vượt quá số dư!", Toast.LENGTH_SHORT).show();
            }
            
            // Update input - create final variable for lambda
            final long finalTotal = newTotal;
            runOnUiThread(() -> {
                binding.betAmountInput.setText(String.valueOf(finalTotal));
            });
        } catch (Exception e) {
            // Log error and show user-friendly message
            android.util.Log.e("GameActivity", "Error in addToBetAmount: " + e.getMessage());
            Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setQuickBetAmount(double amount) {
        try {
            final long finalAmount = (long) amount;
            runOnUiThread(() -> {
                binding.betAmountInput.setText(String.valueOf(finalAmount));
            });
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error in setQuickBetAmount: " + e.getMessage());
        }
    }
    
    private void setAllInBet() {
        try {
            Double currentBalance = viewModel.getBalance().getValue();
            if (currentBalance != null && currentBalance > 0) {
                final long finalBalance = currentBalance.longValue();
                runOnUiThread(() -> {
                    binding.betAmountInput.setText(String.valueOf(finalBalance));
                });
            }
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error in setAllInBet: " + e.getMessage());
        }
    }
