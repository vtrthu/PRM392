package com.example.aviatorcrash.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aviatorcrash.data.AppDatabase;
import com.example.aviatorcrash.data.GameRecord;
import com.example.aviatorcrash.data.GameRecordDao;
import com.example.aviatorcrash.game.GameEngine;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameViewModel extends AndroidViewModel {
    public enum GameState {
        WAITING, FLYING, CRASHED, CASHED_OUT
    }

    private GameEngine gameEngine;
    private GameRecordDao gameRecordDao;
    private ExecutorService executorService;
    private Handler mainHandler;
    private SharedPreferences sharedPreferences;

    // LiveData for UI updates
    private MutableLiveData<GameState> gameState;
    private MutableLiveData<Double> multiplier;
    private MutableLiveData<Double> balance;
    private MutableLiveData<Double> betAmount;
    private MutableLiveData<Double> currentBet;
    private MutableLiveData<Double> cashoutAmount;
    private MutableLiveData<Double> crashPoint;
    private MutableLiveData<Long> gameDuration;
    private MutableLiveData<Boolean> autoCashoutEnabled;
    private MutableLiveData<Double> autoCashoutMultiplier;
    private MutableLiveData<List<GameRecord>> gameHistory;
    private MutableLiveData<Double> winRate;
    private MutableLiveData<Integer> totalGames;

    public GameViewModel(Application application) {
        super(application);
        
        gameEngine = new GameEngine();
        gameRecordDao = AppDatabase.getDatabase(application).gameRecordDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);

        // Initialize LiveData
        gameState = new MutableLiveData<>(GameState.WAITING);
        multiplier = new MutableLiveData<>(1.0);
        balance = new MutableLiveData<>(1000.0);
        betAmount = new MutableLiveData<>(10.0);
        currentBet = new MutableLiveData<>(0.0);
        cashoutAmount = new MutableLiveData<>(0.0);
        crashPoint = new MutableLiveData<>(0.0);
        gameDuration = new MutableLiveData<>(0L);
        autoCashoutEnabled = new MutableLiveData<>(false);
        autoCashoutMultiplier = new MutableLiveData<>(2.0);
        gameHistory = new MutableLiveData<>();
        winRate = new MutableLiveData<>(0.0);
        totalGames = new MutableLiveData<>(0);

        loadSettings();
        loadGameHistory();
    }

    public void placeBet(double amount) {
        if (gameState.getValue() == GameState.WAITING && 
            gameEngine.isValidBet(amount, balance.getValue(), 1.0, 10000.0)) {
            
            currentBet.setValue(amount);
            balance.setValue(balance.getValue() - amount);
            gameState.setValue(GameState.FLYING);
            startGameTimer();
        }
    }

    public void cashout() {
        if (gameState.getValue() == GameState.FLYING) {
            double currentMultiplier = multiplier.getValue();
            double betAmount = currentBet.getValue();
            double winAmount = gameEngine.calculateWinAmount(betAmount, currentMultiplier);
            
            cashoutAmount.setValue(winAmount);
            balance.setValue(balance.getValue() + winAmount);
            gameState.setValue(GameState.CASHED_OUT);
            
            saveGameRecord(betAmount, currentMultiplier, winAmount, true);
        }
    }

    public void startNewRound() {
        gameState.setValue(GameState.WAITING);
        multiplier.setValue(1.0);
        currentBet.setValue(0.0);
        cashoutAmount.setValue(0.0);
        gameDuration.setValue(0L);
        crashPoint.setValue(0.0);
    }

    private void startGameTimer() {
        crashPoint.setValue(gameEngine.generateCrashPoint());
        
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (gameState.getValue() == GameState.FLYING) {
                    long currentDuration = gameDuration.getValue() + 100;
                    gameDuration.setValue(currentDuration);
                    
                    double currentMultiplier = gameEngine.calculateMultiplier(currentDuration);
                    multiplier.setValue(currentMultiplier);
                    
                    // Check for auto-cashout
                    if (autoCashoutEnabled.getValue() && 
                        currentMultiplier >= autoCashoutMultiplier.getValue()) {
                        cashout();
                        return;
                    }
                    
                    // Check if should crash
                    if (gameEngine.shouldCrash(currentMultiplier, crashPoint.getValue())) {
                        crash();
                        return;
                    }
                    
                    mainHandler.postDelayed(this, 100);
                }
            }
        };
        
        mainHandler.post(timerRunnable);
    }

    private void crash() {
        gameState.setValue(GameState.CRASHED);
        double betAmount = currentBet.getValue();
        saveGameRecord(betAmount, multiplier.getValue(), 0.0, false);
    }

    private void saveGameRecord(double betAmount, double multiplier, double cashoutAmount, boolean isWin) {
        executorService.execute(() -> {
            GameRecord record = new GameRecord(
                new Date(),
                betAmount,
                multiplier,
                cashoutAmount,
                isWin,
                gameDuration.getValue()
            );
            gameRecordDao.insertGameRecord(record);
            
            mainHandler.post(() -> loadGameHistory());
        });
    }

    private void loadGameHistory() {
        executorService.execute(() -> {
            List<GameRecord> records = gameRecordDao.getAllGameRecords().getValue();
            if (records != null) {
                mainHandler.post(() -> {
                    gameHistory.setValue(records);
                    calculateStatistics(records);
                });
            }
        });
    }

    private void calculateStatistics(List<GameRecord> records) {
        int total = records.size();
        int wins = 0;
        
        for (GameRecord record : records) {
            if (record.isWin()) {
                wins++;
            }
        }
        
        double winRateValue = total > 0 ? (double) wins / total * 100 : 0.0;
        winRate.setValue(winRateValue);
        totalGames.setValue(total);
    }

    public void clearHistory() {
        executorService.execute(() -> {
            gameRecordDao.deleteAllGameRecords();
            mainHandler.post(() -> {
                gameHistory.setValue(null);
                winRate.setValue(0.0);
                totalGames.setValue(0);
            });
        });
    }

    public void resetBalance() {
        balance.setValue(1000.0);
        saveSettings();
    }

    public void setAutoCashout(boolean enabled, double multiplier) {
        autoCashoutEnabled.setValue(enabled);
        autoCashoutMultiplier.setValue(multiplier);
        saveSettings();
    }

    private void loadSettings() {
        double savedBalance = sharedPreferences.getFloat("balance", 1000.0f);
        boolean autoCashout = sharedPreferences.getBoolean("auto_cashout_enabled", false);
        double autoCashoutMultiplierValue = sharedPreferences.getFloat("auto_cashout_multiplier", 2.0f);
        
        balance.setValue((double) savedBalance);
        autoCashoutEnabled.setValue(autoCashout);
        autoCashoutMultiplier.setValue(autoCashoutMultiplierValue);
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("balance", balance.getValue().floatValue());
        editor.putBoolean("auto_cashout_enabled", autoCashoutEnabled.getValue());
        editor.putFloat("auto_cashout_multiplier", autoCashoutMultiplier.getValue().floatValue());
        editor.apply();
    }

    // Getters for LiveData
    public LiveData<GameState> getGameState() { return gameState; }
    public LiveData<Double> getMultiplier() { return multiplier; }
    public LiveData<Double> getBalance() { return balance; }
    public LiveData<Double> getBetAmount() { return betAmount; }
    public LiveData<Double> getCurrentBet() { return currentBet; }
    public LiveData<Double> getCashoutAmount() { return cashoutAmount; }
    public LiveData<Double> getCrashPoint() { return crashPoint; }
    public LiveData<Long> getGameDuration() { return gameDuration; }
    public LiveData<Boolean> getAutoCashoutEnabled() { return autoCashoutEnabled; }
    public LiveData<Double> getAutoCashoutMultiplier() { return autoCashoutMultiplier; }
    public LiveData<List<GameRecord>> getGameHistory() { return gameHistory; }
    public LiveData<Double> getWinRate() { return winRate; }
    public LiveData<Integer> getTotalGames() { return totalGames; }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
