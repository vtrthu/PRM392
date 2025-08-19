package com.example.aviatorcrash.bot;

import android.os.Handler;
import android.os.Looper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class BotManager {
    // Configuration constants
    private static final int BOT_COUNT = 40;
    private static final double BOT_WIN_RATE = 0.80; // 80% win rate for bots
    private static final double BET_MIN = 5000.0;
    private static final double BET_MAX = 200000.0;
    private static final int TICK_MS = 100;
    
    // Cashout distribution weights (multiplier ranges)
    private static final double[] CASHOUT_RANGES = {1.6, 2.0, 2.5, 3.0, 5.0, 10.0};
    private static final double[] CASHOUT_WEIGHTS = {0.35, 0.25, 0.20, 0.10, 0.07, 0.03};

    private List<BotEntry> allBots;
    private BotEntry playerEntry;
    private Random random;
    private Handler handler;
    private boolean roundActive;
    private double currentCrashPoint;
    private long roundStartTime;

    // Bot name pools
    private static final String[] BOT_NAMES = {
        "Alex", "Sam", "Jordan", "Casey", "Riley", "Taylor", "Morgan", "Quinn",
        "Blake", "Avery", "Cameron", "Drew", "Emery", "Finley", "Harper", "Jaden",
        "Kai", "Lane", "Micah", "Noah", "Parker", "Reese", "Sage", "Tate",
        "Val", "Wesley", "Zion", "Adrian", "Beau", "Cole", "Dean", "Eli",
        "Felix", "Gray", "Hunter", "Ivan", "Jack", "Kyle", "Leo", "Max",
        "Nick", "Oscar", "Paul", "Quinn", "Ryan", "Sean", "Troy", "Uma",
        "Victor", "Will", "Xander", "Yale", "Zack", "Ace", "Ben", "Cal"
    };

    public interface BotUpdateListener {
        void onBotsUpdated(List<BotEntry> leaderboard);
    }

    private BotUpdateListener listener;

    public BotManager() {
        this.allBots = new ArrayList<>();
        this.random = new Random();
        this.handler = new Handler(Looper.getMainLooper());
        this.roundActive = false;
    }

    public void setBotUpdateListener(BotUpdateListener listener) {
        this.listener = listener;
    }

    /**
     * Start a new round with the given crash point
     */
    public void startRound(double crashPoint) {
        this.currentCrashPoint = crashPoint;
        this.roundStartTime = System.currentTimeMillis();
        this.roundActive = true;
        
        generateBots();
        scheduleInitialBets();
        notifyListener();
    }

    /**
     * Update multiplier during the round
     */
    public void onTick(double currentMultiplier) {
        if (!roundActive) return;
        
        // Check for bot cashouts
        processBotCashouts(currentMultiplier);
        notifyListener();
    }

    /**
     * End the round when crash occurs
     */
    public void endRound(double crashMultiplier) {
        if (!roundActive) return;
        
        this.roundActive = false;
        
        // Crash all remaining bots
        for (BotEntry bot : allBots) {
            if (bot.getState() == BotEntry.BotState.BET_PLACED) {
                bot.crash();
            }
        }
        
        notifyListener();
    }

    /**
     * Update player entry when they place a bet
     */
    public void setPlayerBet(String playerName, double betAmount) {
        if (playerEntry == null) {
            playerEntry = new BotEntry(playerName, betAmount, true);
        } else {
            playerEntry.setName(playerName);
            playerEntry.setBetAmount(betAmount);
            playerEntry.setState(BotEntry.BotState.PENDING);
        }
        playerEntry.placeBet();
        
        // Add or update player in allBots list
        boolean playerFound = false;
        for (int i = 0; i < allBots.size(); i++) {
            if (allBots.get(i).isPlayer()) {
                allBots.set(i, playerEntry);
                playerFound = true;
                break;
            }
        }
        if (!playerFound) {
            allBots.add(playerEntry);
        }
        
        notifyListener();
    }

    /**
     * Update player entry when they cash out
     */
    public void setPlayerCashout(double multiplier, double winAmount) {
        if (playerEntry != null) {
            playerEntry.cashout(multiplier);
            playerEntry.setWinAmount(winAmount);
            notifyListener();
        }
    }

    /**
     * Update player entry when they crash
     */
    public void setPlayerCrash() {
        if (playerEntry != null) {
            playerEntry.crash();
            notifyListener();
        }
    }

    /**
     * Reset player for new round
     */
    public void resetPlayer() {
        if (playerEntry != null) {
            playerEntry.setState(BotEntry.BotState.PENDING);
            playerEntry.setActualCashout(0.0);
            playerEntry.setWinAmount(0.0);
        }
    }

    /**
     * Get current leaderboard (top 20, sorted by win amount)
     */
    public List<BotEntry> getLeaderboard() {
        List<BotEntry> leaderboard = new ArrayList<>(allBots);
        
        // Sort by win amount (descending), then by name for stability
        Collections.sort(leaderboard, new Comparator<BotEntry>() {
            @Override
            public int compare(BotEntry a, BotEntry b) {
                int winCompare = Double.compare(b.getWinAmount(), a.getWinAmount());
                if (winCompare != 0) return winCompare;
                return a.getName().compareTo(b.getName());
            }
        });
        
        // Return top 20
        return leaderboard.size() > 20 ? leaderboard.subList(0, 20) : leaderboard;
    }

    private void generateBots() {
        allBots.clear();
        
        // Add player if exists
        if (playerEntry != null) {
            resetPlayer();
            allBots.add(playerEntry);
        }
        
        // Generate bots
        for (int i = 0; i < BOT_COUNT; i++) {
            String name = BOT_NAMES[random.nextInt(BOT_NAMES.length)] + 
                         String.format("%02d", random.nextInt(100));
            
            double betAmount = generateBetAmount();
            double targetCashout = generateTargetCashout();
            
            BotEntry bot = new BotEntry(name, betAmount, targetCashout);
            allBots.add(bot);
        }
    }

    private double generateBetAmount() {
        // Weighted random: more small bets, fewer large bets
        double weight = random.nextDouble();
        if (weight < 0.6) {
            // 60% small bets (5k-50k)
            return BET_MIN + random.nextDouble() * (50000 - BET_MIN);
        } else if (weight < 0.9) {
            // 30% medium bets (50k-100k)
            return 50000 + random.nextDouble() * 50000;
        } else {
            // 10% large bets (100k-200k)
            return 100000 + random.nextDouble() * (BET_MAX - 100000);
        }
    }

    private double generateTargetCashout() {
        // Apply bot win rate bias
        double adjustedCrashPoint = currentCrashPoint;
        if (random.nextDouble() < BOT_WIN_RATE) {
            // Bot should win - target cashout below crash point
            adjustedCrashPoint = Math.max(1.1, currentCrashPoint * 0.85);
        }
        
        // Generate target based on weighted distribution
        double weight = random.nextDouble();
        double cumulative = 0.0;
        
        for (int i = 0; i < CASHOUT_WEIGHTS.length; i++) {
            cumulative += CASHOUT_WEIGHTS[i];
            if (weight <= cumulative) {
                double rangeStart = i == 0 ? 1.1 : CASHOUT_RANGES[i - 1];
                double rangeEnd = CASHOUT_RANGES[i];
                double target = rangeStart + random.nextDouble() * (rangeEnd - rangeStart);
                
                // Ensure target is achievable if bot should win
                if (random.nextDouble() < BOT_WIN_RATE) {
                    target = Math.min(target, adjustedCrashPoint);
                }
                
                return Math.max(1.1, target);
            }
        }
        
        // Fallback
        return 1.6 + random.nextDouble() * 0.9; // 1.6x - 2.5x
    }

    private void scheduleInitialBets() {
        // Schedule bots to place bets with slight delays
        for (int i = 0; i < allBots.size(); i++) {
            final BotEntry bot = allBots.get(i);
            if (!bot.isPlayer()) {
                int delay = random.nextInt(500); // 0-500ms delay
                handler.postDelayed(() -> {
                    if (roundActive) {
                        bot.placeBet();
                        notifyListener();
                    }
                }, delay);
            }
        }
    }

    private void processBotCashouts(double currentMultiplier) {
        for (BotEntry bot : allBots) {
            if (!bot.isPlayer() && 
                bot.getState() == BotEntry.BotState.BET_PLACED && 
                currentMultiplier >= bot.getTargetCashout()) {
                
                // Add some randomness to avoid all bots cashing out at exact same time
                double variation = 0.95 + random.nextDouble() * 0.1; // ±5% variation
                if (currentMultiplier >= bot.getTargetCashout() * variation) {
                    bot.cashout(currentMultiplier);
                }
            }
        }
    }

    private void notifyListener() {
        if (listener != null) {
            handler.post(() -> listener.onBotsUpdated(getLeaderboard()));
        }
    }

    // Getters for configuration (can be used for settings)
    public static int getBotCount() { return BOT_COUNT; }
    public static double getBotWinRate() { return BOT_WIN_RATE; }
    public static double getBetMin() { return BET_MIN; }
    public static double getBetMax() { return BET_MAX; }
}
