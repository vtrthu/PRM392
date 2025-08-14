package com.example.aviatorcrash;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.aviatorcrash.databinding.ActivityGameBinding;
import com.example.aviatorcrash.game.GameEngine;
import com.example.aviatorcrash.viewmodel.GameViewModel;

public class GameActivity extends AppCompatActivity {
    private ActivityGameBinding binding;
    private GameViewModel viewModel;
    private GameEngine gameEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        gameEngine = new GameEngine();

        setupUI();
        setupObservers();
        setupClickListeners();
    }

    private void setupUI() {
        // No action bar setup needed as we have custom toolbar
    }

    private void setupObservers() {
        viewModel.getGameState().observe(this, this::updateGameState);
        viewModel.getMultiplier().observe(this, this::updateMultiplier);
        viewModel.getBalance().observe(this, balance -> 
            binding.balanceText.setText(getString(R.string.balance_format, balance)));
        viewModel.getCurrentBet().observe(this, bet -> 
            binding.currentBetText.setText(getString(R.string.current_bet_format, bet)));
        viewModel.getCashoutAmount().observe(this, amount -> 
            binding.winAmountText.setText(getString(R.string.win_amount_format, amount)));
        viewModel.getCrashPoint().observe(this, crashPoint -> 
            binding.crashPointText.setText(getString(R.string.crash_point_format, crashPoint)));
    }

    private void setupClickListeners() {
        binding.backButton.setOnClickListener(v -> onBackPressed());

        binding.placeBetButton.setOnClickListener(v -> placeBet());

        binding.cashoutButton.setOnClickListener(v -> {
            viewModel.cashout();
            showWinEffect();
        });

        binding.nextRoundButton.setOnClickListener(v -> viewModel.startNewRound());
    }

    private void updateGameState(GameViewModel.GameState state) {
        switch (state) {
            case WAITING:
                binding.gameStatus.setText(R.string.game_status_waiting);
                binding.placeBetButton.setEnabled(true);
                binding.cashoutButton.setEnabled(false);
                binding.nextRoundButton.setEnabled(false);
                break;
            case FLYING:
                binding.gameStatus.setText(R.string.game_status_flying);
                binding.placeBetButton.setEnabled(false);
                binding.cashoutButton.setEnabled(true);
                binding.nextRoundButton.setEnabled(false);
                break;
            case CRASHED:
                binding.gameStatus.setText(R.string.game_status_crashed);
                binding.placeBetButton.setEnabled(false);
                binding.cashoutButton.setEnabled(false);
                binding.nextRoundButton.setEnabled(true);
                showCrashEffect();
                break;
            case CASHED_OUT:
                binding.gameStatus.setText(R.string.game_status_cashed_out);
                binding.placeBetButton.setEnabled(false);
                binding.cashoutButton.setEnabled(false);
                binding.nextRoundButton.setEnabled(true);
                break;
        }
    }

    private void updateMultiplier(Double multiplier) {
        if (multiplier != null) {
            binding.multiplierText.setText(String.format("%.2fx", multiplier));
            int color = gameEngine.getMultiplierColor(multiplier);
            binding.multiplierText.setTextColor(color);
            
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
        String betText = binding.betAmountInput.getText().toString();
        
        if (TextUtils.isEmpty(betText)) {
            binding.betAmountLayout.setError(getString(R.string.error_enter_bet_amount));
            return;
        }

        try {
            double betAmount = Double.parseDouble(betText);
            if (gameEngine.isValidBet(betAmount, viewModel.getBalance().getValue(), 1.0, 10000.0)) {
                viewModel.placeBet(betAmount);
                binding.betAmountInput.setText("");
                binding.betAmountLayout.setError(null);
            } else {
                binding.betAmountLayout.setError(getString(R.string.error_invalid_bet));
            }
        } catch (NumberFormatException e) {
            binding.betAmountLayout.setError(getString(R.string.error_invalid_number));
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
}
