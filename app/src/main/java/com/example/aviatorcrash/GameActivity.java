package com.example.aviatorcrash;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.aviatorcrash.adapter.BotLeaderboardAdapter;
import com.example.aviatorcrash.databinding.ActivityGameBinding;
import com.example.aviatorcrash.game.GameEngine;
import com.example.aviatorcrash.viewmodel.GameViewModel;

public class GameActivity extends AppCompatActivity {
    private ActivityGameBinding binding;
    private GameViewModel viewModel;
    private GameEngine gameEngine;
    private BotLeaderboardAdapter botLeaderboardAdapter;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        gameEngine = new GameEngine();
        
        // Ensure ViewModel has correct user data
        viewModel.onUserChanged();


        setupUI();
        setupLeaderboard();
        setupObservers();
        setupClickListeners();
    }

    private void setupUI() {
        // No action bar setup needed as we have custom toolbar
    }

    private void setupLeaderboard() {
        botLeaderboardAdapter = new BotLeaderboardAdapter();
        binding.botLeaderboardRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.botLeaderboardRecycler.setAdapter(botLeaderboardAdapter);
    }

    private void setupObservers() {
        viewModel.getGameState().observe(this, this::updateGameState);
        viewModel.getMultiplier().observe(this, this::updateMultiplier);
        
        // Balance observer removed - will be handled by tuition observer
        
        viewModel.getCurrentBet().observe(this, bet -> 
            binding.currentBetText.setText(getString(R.string.current_bet_format, bet)));
        viewModel.getCashoutAmount().observe(this, amount -> 
            binding.winAmountText.setText(getString(R.string.win_amount_format, amount)));
        viewModel.getCrashPoint().observe(this, crashPoint -> 
            binding.crashPointText.setText(getString(R.string.crash_point_format, crashPoint)));
        viewModel.getBotLeaderboard().observe(this, bots -> {
            if (bots != null) {
                botLeaderboardAdapter.updateBots(bots);
            }
        });
        
        // Educational feature observers
        setupEducationalObservers();
    }
    
    private void setupEducationalObservers() {
        // Only show educational features for user account
        if (!viewModel.getAuthManager().isUser()) {
            // Hide educational cards for admin account
            binding.tuitionMeterCard.setVisibility(android.view.View.GONE);
            binding.educationalIndicators.setVisibility(android.view.View.GONE);
            return;
        }
        
        // Tuition meter - shows current balance as "remaining study money"
        viewModel.getTuitionRemaining().observe(this, tuition -> {
            binding.tuitionAmountText.setText(String.format("%,.0f VND", tuition));
            
            // Get total deposited to calculate percentage remaining
            double totalDeposited = viewModel.getTotalDeposited();
            
            // Calculate progress based on percentage of money remaining
            int progress = 0;
            double percentage = 0;
            
            if (totalDeposited > 0) {
                percentage = (tuition / totalDeposited) * 100;
                progress = (int) Math.min(Math.max(percentage, 0), 100);
            } else if (tuition > 0) {
                // For demo accounts or accounts with initial balance but no deposits
                progress = 100;
                percentage = 100;
            } else {
                progress = 0;
                percentage = 0;
            }
            
            binding.tuitionProgressBar.setProgress(progress);
            
            // Color coding based on percentage remaining (not absolute amount)
            if (percentage <= 0) {
                // No money left (0%)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    binding.tuitionProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
                }
                binding.tuitionAmountText.setTextColor(0xFFFF6B35);
                binding.tuitionWarningText.setVisibility(android.view.View.VISIBLE);
                binding.tuitionWarningText.setText("⚠️ Hết tiền học phí!");
            } else if (percentage < 20) {
                // Very low remaining (< 20%)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    binding.tuitionProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B35));
                }
                binding.tuitionAmountText.setTextColor(0xFFFF6B35);
                binding.tuitionWarningText.setVisibility(android.view.View.VISIBLE);
                binding.tuitionWarningText.setText("⚠️ Tiền học phí sắp hết!");
            } else if (percentage < 50) {
                // Low remaining (< 50%)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    binding.tuitionProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFFFFCC02));
                }
                binding.tuitionAmountText.setTextColor(0xFFFFCC02);
                binding.tuitionWarningText.setVisibility(android.view.View.VISIBLE);
                binding.tuitionWarningText.setText("⚠️ Cẩn thận với tiền học phí!");
            } else {
                // Good remaining (≥ 50%)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    binding.tuitionProgressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(0xFF00FF88));
                }
                binding.tuitionAmountText.setTextColor(0xFF00FF88);
                binding.tuitionWarningText.setVisibility(android.view.View.GONE);
            }
        });
        
        // Total Loss
        viewModel.getTotalLoss().observe(this, totalLoss -> {
            if (totalLoss > 0) {
                binding.totalLossText.setText(String.format("%,.0f VND", totalLoss));
                binding.totalLossText.setTextColor(0xFFFF6B35); // Red color for losses
            } else {
                binding.totalLossText.setText("0 VND");
                binding.totalLossText.setTextColor(0xFF00FF88); // Green color for no loss
            }
        });
        
        // Chasing warning
        viewModel.getChasingWarning().observe(this, isChasing -> {
            if (isChasing) {
                showChasingWarning();
            }
        });
        
        // Regret message
        viewModel.getRegretMessage().observe(this, message -> {
            if (!message.isEmpty()) {
                showRegretMessage(message);
            }
        });
        
        // Motivational messages
        viewModel.getMotivationalMessage().observe(this, message -> {
            if (!message.isEmpty()) {
                showMotivationalMessage(message);
            }
        });
        
        // Probability popup
        viewModel.getShowProbabilityPopup().observe(this, showPopup -> {
            if (showPopup) {
                showProbabilityPopup();
            }
        });
    }

    private void setupClickListeners() {
        try {
        binding.backButton.setOnClickListener(v -> onBackPressed());

            binding.placeBetButton.setOnClickListener(v -> {
                try {
                    placeBet();
                } catch (Exception e) {
                    android.util.Log.e("GameActivity", "Error in placeBet click: " + e.getMessage());
                    Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            });

        binding.cashoutButton.setOnClickListener(v -> {
                try {
            viewModel.cashout();
            showWinEffect();
                } catch (Exception e) {
                    android.util.Log.e("GameActivity", "Error in cashout click: " + e.getMessage());
                }
            });

            binding.nextRoundButton.setOnClickListener(v -> {
                try {
                    viewModel.startNewRound();
                } catch (Exception e) {
                    android.util.Log.e("GameActivity", "Error in nextRound click: " + e.getMessage());
                }
            });
            
            // Quick bet buttons
            binding.bet1mButton.setOnClickListener(v -> {
                try {
                    addToBetAmount(1000000);
                } catch (Exception e) {
                    android.util.Log.e("GameActivity", "Error in bet1m click: " + e.getMessage());
                }
            });
            binding.bet5mButton.setOnClickListener(v -> {
                try {
                    addToBetAmount(5000000);
                } catch (Exception e) {
                    android.util.Log.e("GameActivity", "Error in bet5m click: " + e.getMessage());
                }
            });
            binding.bet10mButton.setOnClickListener(v -> {
                try {
                    addToBetAmount(10000000);
                } catch (Exception e) {
                    android.util.Log.e("GameActivity", "Error in bet10m click: " + e.getMessage());
                }
            });
            binding.betAllinButton.setOnClickListener(v -> {
                try {
                    setAllInBet();
                } catch (Exception e) {
                    android.util.Log.e("GameActivity", "Error in betAllin click: " + e.getMessage());
                }
            });
            
            // Collapse leaderboard button
            binding.collapseLeaderboardButton.setOnClickListener(v -> {
                try {
                    toggleLeaderboard();
                } catch (Exception e) {
                    android.util.Log.e("GameActivity", "Error in toggleLeaderboard click: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error in setupClickListeners: " + e.getMessage());
        }
    }
    
    private boolean isLeaderboardCollapsed = false;
    
    private void toggleLeaderboard() {
        if (isLeaderboardCollapsed) {
            // Expand - restore full size
            binding.leaderboardCard.getLayoutParams().width = (int) (200 * getResources().getDisplayMetrics().density);
            binding.leaderboardCard.getLayoutParams().height = (int) (160 * getResources().getDisplayMetrics().density);
            binding.leaderboardCard.requestLayout();
            binding.collapseLeaderboardButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            isLeaderboardCollapsed = false;
        } else {
            // Collapse - reduce to small size
            binding.leaderboardCard.getLayoutParams().width = (int) (60 * getResources().getDisplayMetrics().density);
            binding.leaderboardCard.getLayoutParams().height = (int) (60 * getResources().getDisplayMetrics().density);
            binding.leaderboardCard.requestLayout();
            binding.collapseLeaderboardButton.setImageResource(android.R.drawable.ic_menu_view);
            isLeaderboardCollapsed = true;
        }
    }
    
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
            
            // Update input directly (already on UI thread)
            binding.betAmountInput.setText(String.valueOf(newTotal));
        } catch (Exception e) {
            // Log error and show user-friendly message
            android.util.Log.e("GameActivity", "Error in addToBetAmount: " + e.getMessage());
            Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void setQuickBetAmount(double amount) {
        try {
            binding.betAmountInput.setText(String.valueOf((long) amount));
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error in setQuickBetAmount: " + e.getMessage());
        }
    }
    
    private void setAllInBet() {
        try {
            Double currentBalance = viewModel.getBalance().getValue();
            if (currentBalance != null && currentBalance > 0) {
                binding.betAmountInput.setText(String.valueOf(currentBalance.longValue()));
            }
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error in setAllInBet: " + e.getMessage());
        }
    }

    private void updateGameState(GameViewModel.GameState state) {
        switch (state) {
            case WAITING:
                binding.gameStatus.setText(R.string.game_status_waiting);
                binding.placeBetButton.setEnabled(true);
                binding.cashoutButton.setEnabled(false);
                binding.nextRoundButton.setEnabled(false);
                
                // Show all HUD elements for waiting state
                showAllHUD();
                
                if (binding.gameView != null) {
                    binding.gameView.reset();
                }
                break;
                
            case FLYING:
                binding.gameStatus.setText(R.string.game_status_flying);
                binding.placeBetButton.setEnabled(false);
                binding.cashoutButton.setEnabled(true);
                binding.nextRoundButton.setEnabled(false);
                
                // Hide HUD and focus on game view, only keep cashout button
                hideHUDForFlying();
                
                if (binding.gameView != null) {
                    binding.gameView.startFlight();
                }
                break;
                
            case CRASHED:
                binding.gameStatus.setText(R.string.game_status_crashed);
                binding.placeBetButton.setEnabled(false);
                binding.cashoutButton.setEnabled(false);
                binding.nextRoundButton.setEnabled(true);
                
                // Show all HUD elements back for crashed state
                showAllHUD();
                
                if (binding.gameView != null) {
                    binding.gameView.crash();
                }
                showCrashEffect();
                break;
                
            case CASHED_OUT:
                binding.gameStatus.setText(R.string.game_status_cashed_out);
                binding.placeBetButton.setEnabled(false);
                binding.cashoutButton.setEnabled(false);
                binding.nextRoundButton.setEnabled(false); // Keep disabled until crash
                
                // Show all HUD elements back for cashout state
                showAllHUD();
                
                // Don't reset gameView here - let the plane continue flying until crash
                break;
        }
    }

    private void updateMultiplier(Double multiplier) {
        if (multiplier != null) {
            binding.multiplierText.setText(String.format("%.2fx", multiplier));
            int color = gameEngine.getMultiplierColor(multiplier);
            binding.multiplierText.setTextColor(color);
            if (binding.gameView != null) {
                binding.gameView.setMultiplier(multiplier);
            }
            
            // Hiển thị thông báo đặc biệt cho jackpot và mega win
            if (gameEngine.isJackpot(multiplier)) {
                binding.multiplierText.setText("🎉 JACKPOT! 100x 🎉");
                showSpecialEffect("JACKPOT! 100x!");
            } else if (gameEngine.isMegaWin(multiplier)) {
                showSpecialEffect("MEGA WIN! " + String.format("%.1fx", multiplier));
            }
        }
    }
    
    private void showSpecialEffect(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void placeBet() {
        try {
            String betText = binding.betAmountInput.getText().toString().trim();
        
        if (TextUtils.isEmpty(betText)) {
                Toast.makeText(this, "Vui lòng nhập số tiền cược", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
                long betAmount = Long.parseLong(betText);
                
                // Basic validation
                if (betAmount < 1000000) {
                    Toast.makeText(this, "Số tiền cược tối thiểu là 1,000,000 VND", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Double currentBalance = viewModel.getBalance().getValue();
                if (currentBalance == null || betAmount > currentBalance) {
                    Toast.makeText(this, "Số dư không đủ", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (gameEngine.isValidBet(betAmount, currentBalance, 1000000.0, 10000000000000.0)) {
                viewModel.placeBet(betAmount);
                binding.betAmountInput.setText("");
            } else {
                    Toast.makeText(this, "Số tiền cược không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                android.util.Log.e("GameActivity", "NumberFormatException in placeBet: " + e.getMessage());
            }
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error in placeBet: " + e.getMessage());
            Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
        }
    }

    private void showCrashEffect() {
        Toast.makeText(this, R.string.game_crashed, Toast.LENGTH_SHORT).show();
        // Thêm hiệu ứng rung khi crash
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.os.VibrationEffect effect = android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE);
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(effect);
            }
        }
    }

    private void showWinEffect() {
        Toast.makeText(this, R.string.cashout_successful, Toast.LENGTH_SHORT).show();
        // Thêm hiệu ứng rung khi thắng
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.os.VibrationEffect effect = android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE);
            android.os.Vibrator vibrator = (android.os.Vibrator) getSystemService(android.content.Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(effect);
            }
        }
    }
    
    // Educational feature methods
    private void showChasingWarning() {
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Cảnh báo hành vi nguy hiểm")
            .setMessage("Bạn đang 'gỡ' - tăng tiền cược sau khi thua!\n\nĐây là hành vi phổ biến dẫn đến mất tiền nhiều hơn. Nhà cái luôn có lợi thế về lâu dài.")
            .setPositiveButton("Tôi hiểu", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
    
    private void showRegretMessage(String message) {
        new AlertDialog.Builder(this)
            .setTitle("💡 Kiến thức tâm lý")
            .setMessage(message)
            .setPositiveButton("Hiểu rồi", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show();
    }
    
    private void showMotivationalMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    private void showProbabilityPopup() {
        double currentBalance = viewModel.getBalance().getValue() != null ? viewModel.getBalance().getValue() : 0.0;
        double deficit = 2870000.0 - currentBalance;
        
        if (deficit > 0) {
            String message = String.format("💹 Để quay về đủ học phí (%.0f VND), bạn cần:\n\n" +
                "• Thắng liên tiếp nhiều ván với tỷ lệ cao\n" +
                "• Xác suất thành công < 5%%\n" +
                "• Càng cố gắng 'gỡ', càng dễ mất sạch\n\n" +
                "🎯 Lời khuyên: Hãy dừng lại và bảo vệ số tiền còn lại!", deficit);
                
            new AlertDialog.Builder(this)
                .setTitle("📊 Phân tích xác suất")
                .setMessage(message)
                .setPositiveButton("Tôi sẽ cân nhắc", null)
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
        }
    }
    

    
    /**
     * Hide HUD elements during FLYING state to focus on game view
     * Keep multiplier, leaderboard, and cashout button visible
     */
    private void hideHUDForFlying() {
        try {
            // Hide top HUD (back button, title)
            binding.topHud.setVisibility(android.view.View.GONE);
            
            // Hide educational cards
            binding.tuitionMeterCard.setVisibility(android.view.View.GONE);
            
            // Keep leaderboard visible for live player updates
            binding.leaderboardCard.setVisibility(android.view.View.VISIBLE);
            
            // Hide bottom HUD except cashout button
            binding.educationalIndicators.setVisibility(android.view.View.GONE);
            binding.bettingControls.setVisibility(android.view.View.GONE);
            binding.nextRoundButton.setVisibility(android.view.View.GONE);
            
            // Keep cashout button visible
            binding.cashoutButton.setVisibility(android.view.View.VISIBLE);
            
            // Keep game status and multiplier text visible for real-time updates
            binding.statusCard.setVisibility(android.view.View.VISIBLE);
            binding.crashPointText.setVisibility(android.view.View.GONE);
            binding.betInfoContainer.setVisibility(android.view.View.GONE);
            
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error in hideHUDForFlying: " + e.getMessage());
        }
    }
    
    /**
     * Show all HUD elements back for WAITING, CRASHED, CASHED_OUT states
     */
    private void showAllHUD() {
        try {
            // Show top HUD
            binding.topHud.setVisibility(android.view.View.VISIBLE);
            
            // Show educational cards (only for user account)
            if (viewModel.getAuthManager().isUser()) {
                binding.tuitionMeterCard.setVisibility(android.view.View.VISIBLE);
            }
            
            // Show leaderboard
            binding.leaderboardCard.setVisibility(android.view.View.VISIBLE);
            
            // Show bottom HUD
            binding.educationalIndicators.setVisibility(android.view.View.VISIBLE);
            binding.bettingControls.setVisibility(android.view.View.VISIBLE);
            binding.nextRoundButton.setVisibility(android.view.View.VISIBLE);
            
            // Show cashout button
            binding.cashoutButton.setVisibility(android.view.View.VISIBLE);
            
            // Show game status and multiplier text
            binding.statusCard.setVisibility(android.view.View.VISIBLE);
            binding.crashPointText.setVisibility(android.view.View.GONE); // Keep hidden by default
            binding.betInfoContainer.setVisibility(android.view.View.GONE); // Keep hidden by default
            
        } catch (Exception e) {
            android.util.Log.e("GameActivity", "Error in showAllHUD: " + e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        if (viewModel.getGameState().getValue() == GameViewModel.GameState.FLYING) {
            new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_exit)
                .setMessage(R.string.confirm_exit_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                .setNegativeButton(R.string.no, null)
                .show();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Save balance when activity is destroyed
        if (viewModel != null) {
            viewModel.saveBalance();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // Save balance when activity is paused
        if (viewModel != null) {
            viewModel.saveBalance();
        }
    }
}