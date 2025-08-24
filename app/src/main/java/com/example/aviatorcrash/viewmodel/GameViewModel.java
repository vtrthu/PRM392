package com.example.aviatorcrash.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.content.Context;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.aviatorcrash.data.AppDatabase;
import com.example.aviatorcrash.data.GameRecord;
import com.example.aviatorcrash.data.GameRecordDao;
import com.example.aviatorcrash.game.GameEngine;
import com.example.aviatorcrash.auth.AuthManager;
import com.example.aviatorcrash.bot.BotManager;
import com.example.aviatorcrash.bot.BotEntry;

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
    private AuthManager authManager; // Educational bias manager
    private BotManager botManager; // Bot crowd manager

    // LiveData for UI updates
    private MutableLiveData<GameState> gameState;
    private MutableLiveData<Double> multiplier;
    private MutableLiveData<Double> balance;
    private MutableLiveData<Double> betAmount;
    private MutableLiveData<Double> currentBet;
    private MutableLiveData<Double> cashoutAmount;
    private MutableLiveData<Double> crashPoint;
    private MutableLiveData<Long> gameDuration;

    private MutableLiveData<List<GameRecord>> gameHistory;
    private MutableLiveData<Double> winRate;
    private MutableLiveData<Integer> totalGames;
    private MutableLiveData<List<BotEntry>> botLeaderboard;
    
    // Educational features
    private MutableLiveData<Double> tuitionRemaining;
    private MutableLiveData<Double> additionalExpenses;
    private MutableLiveData<Double> debtAmount;

    private MutableLiveData<Double> totalLoss;
    private MutableLiveData<Boolean> chasingWarning;
    private MutableLiveData<String> regretMessage;
    private MutableLiveData<String> motivationalMessage;
    private MutableLiveData<Boolean> showProbabilityPopup;
    
    // Educational constants - removed fixed amount constraint
    private double[] previousBets = new double[5]; // Track last 5 bets for chasing detection
    private int betHistoryIndex = 0;
    private long sessionStartTime;

    public GameViewModel(Application application) {
        super(application);
        
        gameEngine = new GameEngine();
        authManager = new AuthManager(application);
        gameEngine.setAuthManager(authManager); // Set educational bias
        botManager = new BotManager(); // Initialize bot manager
        gameRecordDao = AppDatabase.getDatabase(application).gameRecordDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize shared preferences for current user
        updateSharedPreferences();

        // Initialize LiveData
        gameState = new MutableLiveData<>(GameState.WAITING);
        multiplier = new MutableLiveData<>(1.0);
        balance = new MutableLiveData<>(loadBalance());
        betAmount = new MutableLiveData<>(10.0);
        currentBet = new MutableLiveData<>(0.0);
        cashoutAmount = new MutableLiveData<>(0.0);
        crashPoint = new MutableLiveData<>(0.0);
        gameDuration = new MutableLiveData<>(0L);

        gameHistory = new MutableLiveData<>();
        winRate = new MutableLiveData<>(0.0);
        totalGames = new MutableLiveData<>(0);
        botLeaderboard = new MutableLiveData<>();
        
        // Initialize educational features
        tuitionRemaining = new MutableLiveData<>(0.0);
        additionalExpenses = new MutableLiveData<>(0.0);
        debtAmount = new MutableLiveData<>(0.0);

        totalLoss = new MutableLiveData<>(0.0);
        chasingWarning = new MutableLiveData<>(false);
        regretMessage = new MutableLiveData<>("");
        motivationalMessage = new MutableLiveData<>("");
        showProbabilityPopup = new MutableLiveData<>(false);
        sessionStartTime = System.currentTimeMillis();

        // Setup bot manager listener
        botManager.setBotUpdateListener(bots -> botLeaderboard.setValue(bots));

        loadSettings();
        loadGameHistory();
        
        // Initialize educational calculations
        updateEducationalMetrics();
    }

    public void placeBet(double amount) {
        if (gameState.getValue() == GameState.WAITING && 
            gameEngine.isValidBet(amount, balance.getValue(), 5000.0, 10000000000000.0)) {
            
            currentBet.setValue(amount);
            balance.setValue(balance.getValue() - amount);
            saveSettings(); // Lưu số dư sau khi đặt cược
            
            // Educational features
            if (authManager.isUser()) {
                detectChasingBehavior(amount);
                updateEducationalMetrics();
            }
            
            // Notify bot manager about player bet
            String playerName = "You";
            botManager.setPlayerBet(playerName, amount);
            
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
            saveSettings(); // Lưu số dư sau khi rút tiền
            gameState.setValue(GameState.CASHED_OUT);
            
            // Educational features
            if (authManager.isUser()) {
                updateEducationalMetrics();
                // Show regret explanation after a short delay
                mainHandler.postDelayed(() -> showRegretExplanation(), 1000);
            }
            
            // Notify bot manager about player cashout
            botManager.setPlayerCashout(currentMultiplier, winAmount);
            
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
        
        // Reset bot manager for new round
        botManager.resetPlayer();
        
        saveSettings(); // Lưu trạng thái game
    }

    private void startGameTimer() {
        // Increment game count for educational tracking
        authManager.incrementUserGameCount();
        
        double generatedCrashPoint = gameEngine.generateCrashPoint();
        crashPoint.setValue(generatedCrashPoint);
        
        // Start bot round with the crash point
        botManager.startRound(generatedCrashPoint);
        
        Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                // Continue timer for both FLYING and CASHED_OUT states
                if (gameState.getValue() == GameState.FLYING || gameState.getValue() == GameState.CASHED_OUT) {
                    long currentDuration = gameDuration.getValue() + 100;
                    gameDuration.setValue(currentDuration);
                    
                    double currentMultiplier = gameEngine.calculateMultiplier(currentDuration);
                    multiplier.setValue(currentMultiplier);
                    
                    // Update bot manager with current multiplier
                    botManager.onTick(currentMultiplier);
                    

                    
                    // Check if should crash
                    if (gameEngine.shouldCrash(currentMultiplier, crashPoint.getValue())) {
                        handleCrash();
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

    private void handleCrash() {
        double crashMultiplier = multiplier.getValue();
        
        // End bot round
        botManager.endRound(crashMultiplier);
        
        if (gameState.getValue() == GameState.FLYING) {
            // Player was still flying when crash happened - they lose
            gameState.setValue(GameState.CRASHED);
            double betAmount = currentBet.getValue();
            
            // Notify bot manager about player crash
            botManager.setPlayerCrash();
            
            saveGameRecord(betAmount, multiplier.getValue(), 0.0, false);
            
                         // Educational features for losses
             if (authManager.isUser()) {
                 updateEducationalMetrics();
                 // Show recovery probability if they lost significant amount
                 if (betAmount > 50000) {
                     showRecoveryProbability();
                 }
             }
        } else if (gameState.getValue() == GameState.CASHED_OUT) {
            // Player already cashed out, just end the round to show crash effect
            // No need to save another game record since they already won
            gameState.setValue(GameState.CRASHED);
            
            // Educational features - update metrics after crash
            if (authManager.isUser()) {
                updateEducationalMetrics();
            }
        }
    }

    private void saveGameRecord(double betAmount, double multiplier, double cashoutAmount, boolean isWin) {
        executorService.execute(() -> {
            String currentUsername = getCurrentUsername();
            GameRecord record = new GameRecord(
                new Date(),
                betAmount,
                multiplier,
                cashoutAmount,
                isWin,
                gameDuration.getValue(),
                currentUsername // Add username to record
            );
            gameRecordDao.insertGameRecord(record);
            
            mainHandler.post(() -> loadGameHistory());
        });
    }

    private void loadGameHistory() {
        executorService.execute(() -> {
            try {
                String currentUsername = getCurrentUsername();
                List<GameRecord> records;
                
                if (currentUsername != null) {
                    // Load user-specific records
                    records = gameRecordDao.getGameRecordsByUserDirect(currentUsername);
                } else {
                    // Fallback to all records
                    records = gameRecordDao.getAllGameRecordsDirect();
                }
                
                mainHandler.post(() -> {
                    gameHistory.setValue(records);
                    if (records != null && !records.isEmpty()) {
                        calculateStatistics(records);
                    } else {
                        winRate.setValue(0.0);
                        totalGames.setValue(0);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback: set empty list
                mainHandler.post(() -> {
                    gameHistory.setValue(null);
                    winRate.setValue(0.0);
                    totalGames.setValue(0);
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
        // Only reset balance for demo accounts, not newly registered users
        String currentUsername = getCurrentUsername();
        
        if (authManager.isAdmin() || 
            (authManager.isUser() && "toiyeufpt".equals(currentUsername))) {
            double initialBalance = authManager.isAdmin() ? 100000.0 : 28700000.0; // Keep demo balance for toiyeufpt
            balance.setValue(initialBalance);
            saveSettings();
        }
        // Newly registered users keep their 0 balance until they deposit
    }
    
    public void saveBalance() {
        // Force save current balance to preferences
        saveSettings();
    }

    private double loadBalance() {
        // Get current username safely
        String currentUsername = getCurrentUsername();
        
        // Debug log
        android.util.Log.d("GameViewModel", "Loading balance for username: " + currentUsername);
        
        // Check if this is a newly registered user
        if (currentUsername != null && authManager.isNewlyRegisteredUser(currentUsername)) {
            android.util.Log.d("GameViewModel", "User is newly registered, returning 0 balance");
            return 0.0;
        }
        
                 // Check if this is a demo account (admin/toiyeufpt)
        if (authManager.isAdmin() || 
            (authManager.isUser() && "toiyeufpt".equals(currentUsername))) {
            // Demo accounts get initial balance
            if (!sharedPreferences.contains("balance")) {
                double initialBalance = authManager.isAdmin() ? 100000.0 : 28700000.0; // Keep demo balance for toiyeufpt
                android.util.Log.d("GameViewModel", "Demo account, returning initial balance: " + initialBalance);
                return initialBalance;
            }
        }
        
        // Load saved balance from preferences
        double savedBalance = sharedPreferences.getFloat("balance", 0.0f);
        android.util.Log.d("GameViewModel", "Loading saved balance: " + savedBalance);
        return savedBalance;
    }
    
    /**
     * Get current username safely
     */
    private String getCurrentUsername() {
        return authManager.getCurrentUsername();
    }
    
    /**
     * Update SharedPreferences to use current user's file
     */
    private void updateSharedPreferences() {
        String currentUsername = getCurrentUsername();
        if (currentUsername != null) {
            // Create user-specific preferences file
            String prefsName = "game_preferences_" + currentUsername;
            sharedPreferences = getApplication().getSharedPreferences(prefsName, Context.MODE_PRIVATE);
            android.util.Log.d("GameViewModel", "Updated SharedPreferences for user: " + currentUsername + " (file: " + prefsName + ")");
        } else {
            // Fallback to general preferences
            sharedPreferences = getApplication().getSharedPreferences("game_preferences", Context.MODE_PRIVATE);
            android.util.Log.d("GameViewModel", "Using fallback SharedPreferences (no current user)");
        }
    }
    
    /**
     * Call this method when user changes (login/logout)
     */
    public void onUserChanged() {
        // Update SharedPreferences for new user
        updateSharedPreferences();
        
        // Reset tracking data to fix legacy calculation issues
        resetTrackingData();
        
        // Reload balance for new user
        balance.setValue(loadBalance());
        
        // Reload settings for new user
        loadSettings();
        
        // Reload game history for new user
        loadGameHistory();
        
        // Reset educational metrics
        updateEducationalMetrics();
        
        android.util.Log.d("GameViewModel", "User changed, reloaded all data");
    }
    
    /**
     * Reset tracking data to fix legacy calculation issues
     */
    private void resetTrackingData() {
        // Reset total_deposited and total_withdrawn for clean state
        double currentBalance = balance.getValue() != null ? balance.getValue() : 0.0;
        
        // Set total_deposited = current balance (assume all current balance came from deposits)
        sharedPreferences.edit()
                .putFloat("total_deposited", (float) currentBalance)
                .putFloat("total_withdrawn", 0.0f)
                .apply();
        
        android.util.Log.d("GameViewModel", "Reset tracking data - deposited: " + currentBalance + ", withdrawn: 0");
    }
    
    /**
     * Debug method to check balance loading logic
     */
    public String debugBalanceLogic() {
        String currentUsername = getCurrentUsername();
        boolean isNewlyRegistered = currentUsername != null && authManager.isNewlyRegisteredUser(currentUsername);
        boolean isAdmin = authManager.isAdmin();
        boolean isDemoUser = "toiyeufpt".equals(currentUsername);
        String prefsFile = sharedPreferences != null ? "game_preferences_" + currentUsername : "null";
        
        return String.format("Username: %s, IsNew: %s, IsAdmin: %s, IsDemo: %s, PrefsFile: %s", 
                           currentUsername, isNewlyRegistered, isAdmin, isDemoUser, prefsFile);
    }



    private void loadSettings() {
        // Balance is already loaded in constructor via loadBalance()
        // No auto cashout settings to load
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("balance", balance.getValue().floatValue());
        // No auto cashout settings to save
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

    public LiveData<List<GameRecord>> getGameHistory() { return gameHistory; }
    public LiveData<Double> getWinRate() { return winRate; }
    public LiveData<Integer> getTotalGames() { return totalGames; }
    public LiveData<List<BotEntry>> getBotLeaderboard() { return botLeaderboard; }
    
    /**
     * Get AuthManager for educational features
     */
    public AuthManager getAuthManager() {
        return authManager;
    }
    
    // Educational feature getters
    public LiveData<Double> getTuitionRemaining() { return tuitionRemaining; }

    public LiveData<Double> getAdditionalExpenses() { return additionalExpenses; }
    public LiveData<Double> getDebtAmount() { return debtAmount; }

    public LiveData<Double> getTotalLoss() { return totalLoss; }
    public LiveData<Boolean> getChasingWarning() { return chasingWarning; }
    public LiveData<String> getRegretMessage() { return regretMessage; }
    public LiveData<String> getMotivationalMessage() { return motivationalMessage; }
    public LiveData<Boolean> getShowProbabilityPopup() { return showProbabilityPopup; }
    
    /**
     * Deposit money into account
     */
    public void depositMoney(double amount) {
        if (amount > 0) {
            double currentBalance = balance.getValue() != null ? balance.getValue() : 0.0;
            balance.setValue(currentBalance + amount);
            
            // Track total deposited for loss calculation
            updateTotalDeposited(amount);
            
            saveBalance();
            saveSettings();
            
            // Update educational metrics after deposit
            updateEducationalMetrics();
        }
    }

    /**
     * Withdraw money from account
     */
    public void withdrawMoney(double amount) {
        double currentBalance = balance.getValue() != null ? balance.getValue() : 0.0;
        if (amount > 0 && amount <= currentBalance) {
            balance.setValue(currentBalance - amount);
            
            // Track total withdrawn for accurate loss calculation
            updateTotalWithdrawn(amount);
            
            saveBalance();
            saveSettings();
            
            // Update educational metrics after withdrawal
            updateEducationalMetrics();
        }
    }

         /**
      * Update educational metrics based on current game state
      */
     private void updateEducationalMetrics() {
         if (!authManager.isUser()) return; // Only for student account
         
         // Update tuition remaining - directly show current balance as "study money left"
         double currentBalance = balance.getValue() != null ? balance.getValue() : 0.0;
         tuitionRemaining.setValue(currentBalance); // Show actual balance, can be 0 or negative
         
         // Calculate debt if balance goes negative
         if (currentBalance < 0) {
             debtAmount.setValue(Math.abs(currentBalance));
         } else {
             debtAmount.setValue(0.0);
         }
         
         // Calculate total loss based on actual deposit history
         updateTotalLossCalculation();
         
         // Add random expenses periodically
         addRandomExpenses();
         
         // Show motivational messages
         updateMotivationalMessages();
     }
    
    private void updateTotalLossCalculation() {
        // Calculate total loss based on actual deposit/withdraw history
        // Formula: Total Loss = Total Deposited - Current Balance - Total Withdrawn
        double currentBalance = balance.getValue() != null ? balance.getValue() : 0.0;
        double totalDeposited = getTotalDeposited();
        double totalWithdrawn = getTotalWithdrawn();
        
        // Real loss = Money put in - Money currently have - Money successfully withdrawn
        double totalLossAmount = Math.max(0, totalDeposited - currentBalance - totalWithdrawn);
        totalLoss.setValue(totalLossAmount);
    }
    
    /**
     * Get total amount deposited by user (from SharedPreferences)
     */
    public double getTotalDeposited() {
        return sharedPreferences.getFloat("total_deposited", 0.0f);
    }
    
    /**
     * Get total amount withdrawn by user (from SharedPreferences)
     */
    public double getTotalWithdrawn() {
        return sharedPreferences.getFloat("total_withdrawn", 0.0f);
    }
    
    /**
     * Update total deposited amount
     */
    private void updateTotalDeposited(double amount) {
        double currentTotal = getTotalDeposited();
        double newTotal = currentTotal + amount;
        sharedPreferences.edit()
                .putFloat("total_deposited", (float) newTotal)
                .apply();
    }
    
    /**
     * Update total withdrawn amount
     */
    private void updateTotalWithdrawn(double amount) {
        double currentTotal = getTotalWithdrawn();
        double newTotal = currentTotal + amount;
        sharedPreferences.edit()
                .putFloat("total_withdrawn", (float) newTotal)
                .apply();
    }
    

    
    private void addRandomExpenses() {
        // Add random expenses every few rounds (books, transportation, etc.)
        int totalGamesPlayed = totalGames.getValue() != null ? totalGames.getValue() : 0;
        if (totalGamesPlayed > 0 && totalGamesPlayed % 5 == 0) {
            double[] expenses = {25000, 50000, 75000, 100000}; // Random expense amounts
            String[] expenseTypes = {"Giáo trình", "Tiền xe", "Tiền ăn", "Phí thực hành"};
            
            java.util.Random random = new java.util.Random();
            double expense = expenses[random.nextInt(expenses.length)];
            additionalExpenses.setValue((additionalExpenses.getValue() != null ? additionalExpenses.getValue() : 0.0) + expense);
        }
    }
    
    private void updateMotivationalMessages() {
        String[] messages = {
            "💝 Cha mẹ tin bạn sẽ dùng tiền đúng mục đích",
            "📚 Đầu tư vào tri thức sinh lời bền vững",
            "🎓 Học vấn là tài sản quý giá nhất",
            "⏰ Thời gian học đại học không quay lại",
            "🏠 Gia đình kỳ vọng vào thành công của bạn",
            "💡 Mỗi lần cược là một cơ hội học tập bị lãng phí"
        };
        
        int totalGamesPlayed = totalGames.getValue() != null ? totalGames.getValue() : 0;
        if (totalGamesPlayed > 0 && totalGamesPlayed % 3 == 0) {
            java.util.Random random = new java.util.Random();
            String message = messages[random.nextInt(messages.length)];
            motivationalMessage.setValue(message);
        }
    }
    
    /**
     * Detect chasing behavior (increasing bets after losses)
     */
    private void detectChasingBehavior(double betAmount) {
        // Add current bet to history
        previousBets[betHistoryIndex] = betAmount;
        betHistoryIndex = (betHistoryIndex + 1) % previousBets.length;
        
        // Check if player is increasing bets (chasing)
        boolean isChasing = false;
        for (int i = 1; i < previousBets.length; i++) {
            if (previousBets[i] > previousBets[i-1] * 1.5) { // 50% increase threshold
                isChasing = true;
                break;
            }
        }
        
        chasingWarning.setValue(isChasing);
    }
    
    /**
     * Show regret message after cashout
     */
    public void showRegretExplanation() {
        regretMessage.setValue("🧠 Hiệu ứng 'tiếc nuối': Não bộ tạo cảm giác tiếc khi thấy có thể thắng nhiều hơn. Đây là tâm lý bình thường nhưng nguy hiểm trong cá cược!");
        
        // Clear message after 5 seconds
        mainHandler.postDelayed(() -> regretMessage.setValue(""), 5000);
    }
    
    /**
     * Show probability popup for recovery calculation
     */
    public void showRecoveryProbability() {
        double currentBalance = balance.getValue() != null ? balance.getValue() : 0.0;
        double totalDeposited = getTotalDeposited();
        
        if (totalDeposited > 0 && currentBalance < totalDeposited) {
            // Calculate probability of recovery based on total deposited vs current balance
            double deficit = totalDeposited - currentBalance;
            
            if (currentBalance > 0) {
                double requiredMultiplier = deficit / currentBalance + 1.0;
                double probability = Math.pow(0.5, Math.log(requiredMultiplier) / Math.log(2)) * 100;
                
                showProbabilityPopup.setValue(true);
                
                // Clear popup after showing
                mainHandler.postDelayed(() -> showProbabilityPopup.setValue(false), 3000);
            }
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
