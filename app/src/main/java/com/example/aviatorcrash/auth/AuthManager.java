package com.example.aviatorcrash.auth;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Authentication Manager with realistic login credentials
 * For educational demo purposes - hides true intent during classroom presentation
 */
public class AuthManager {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_ACCOUNT_TYPE = "account_type";
    private static final String KEY_TOTAL_GAMES_USER = "total_games_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    
    // Real login credentials for demo
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "house2024";
    private static final String PLAYER_USERNAME = "toiyeufpt";
    private static final String PLAYER_PASSWORD = "demo";

    private final SharedPreferences prefs;
    private AccountType currentAccountType;

    public AuthManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadCurrentAccount();
    }

    /**
     * Authenticate user with username and password
     * Returns true if login successful, false otherwise
     */
    public boolean login(String username, String password) {
        AccountType accountType = validateCredentials(username, password);
        if (accountType != null) {
            currentAccountType = accountType;
            
            // Reset game count for fresh experience each login (educational demo)
            // This ensures every login starts with "honeymoon period" -> "house edge" pattern
            if (accountType == AccountType.USER) {
                resetUserGameCount();
            }
            
            prefs.edit()
                    .putString(KEY_ACCOUNT_TYPE, accountType.getUsername())
                    .putBoolean(KEY_IS_LOGGED_IN, true)
                    .apply();
            return true;
        }
        return false;
    }
    
    /**
     * Validate login credentials and return corresponding account type
     */
    private AccountType validateCredentials(String username, String password) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            return AccountType.ADMIN;
        } else if (PLAYER_USERNAME.equals(username) && PLAYER_PASSWORD.equals(password)) {
            return AccountType.USER;
        }
        return null; // Invalid credentials
    }

    public void logout() {
        currentAccountType = null;
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && currentAccountType != null;
    }

    public AccountType getCurrentAccountType() {
        return currentAccountType;
    }

    public boolean isAdmin() {
        return currentAccountType == AccountType.ADMIN;
    }

    public boolean isUser() {
        return currentAccountType == AccountType.USER;
    }

    /**
     * Get total games played by user account (for educational bias calculation)
     */
    public int getUserTotalGames() {
        return prefs.getInt(KEY_TOTAL_GAMES_USER, 0);
    }

    /**
     * Increment user game count (only for user account)
     */
    public void incrementUserGameCount() {
        if (isUser()) {
            int currentCount = getUserTotalGames();
            prefs.edit()
                    .putInt(KEY_TOTAL_GAMES_USER, currentCount + 1)
                    .apply();
        }
    }

    /**
     * Reset user game count (for fresh demo experience)
     */
    public void resetUserGameCount() {
        prefs.edit()
                .putInt(KEY_TOTAL_GAMES_USER, 0)
                .apply();
    }

    /**
     * Calculate expected win rate for educational purposes
     * Admin: Always high (95%+)
     * User: Decreases over time to demonstrate house edge
     */
    public double getExpectedWinRate() {
        if (isAdmin()) {
            return 0.95; // 95% win rate for house
        } else {
            int gamesPlayed = getUserTotalGames();
            
            // Educational bias: User win rate decreases over time
            if (gamesPlayed <= 5) {
                return 0.80; // 80% in honeymoon period
            } else if (gamesPlayed <= 15) {
                return 0.60; // 60% as house edge starts
            } else if (gamesPlayed <= 30) {
                return 0.40; // 40% house edge increases
            } else {
                return 0.20; // 20% long-term house edge dominates
            }
        }
    }

    /**
     * Get account status message (hidden educational purpose)
     */
    public String getAccountMessage() {
        if (isAdmin()) {
            return "🎯 VIP Account - Premium Features Enabled";
        } else {
            int gamesPlayed = getUserTotalGames();
            
            if (gamesPlayed <= 5) {
                return "🆕 New Player - Beginner's Luck Active";
            } else if (gamesPlayed <= 15) {
                return "⭐ Regular Player - Game Experience Building";
            } else if (gamesPlayed <= 30) {
                return "🎮 Experienced Player - Advanced Mode";
            } else {
                return "👑 Veteran Player - Master Level Unlocked";
            }
        }
    }
    
    /**
     * Get true educational message (for internal use only)
     */
    public String getEducationalMessage() {
        if (isAdmin()) {
            return "🏦 Nhà Cái luôn có lợi thế! Hầu hết các ván đều thắng.";
        } else {
            int gamesPlayed = getUserTotalGames();
            
            if (gamesPlayed <= 5) {
                return "🎯 Giai đoạn 'mật ngọt': Bạn đang thắng nhiều để bị cuốn hút!";
            } else if (gamesPlayed <= 15) {
                return "⚠️ Tỷ lệ thắng bắt đầu giảm... Nhà cái đang lấy lại lợi thế!";
            } else if (gamesPlayed <= 30) {
                return "📉 House Edge đang hoạt động! Tỷ lệ thắng của bạn giảm mạnh.";
            } else {
                return "🚨 THỰC TẾ: Về lâu dài, nhà cái LUÔN THẮNG! Hãy ngừng cá cược!";
            }
        }
    }

    private void loadCurrentAccount() {
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            String username = prefs.getString(KEY_ACCOUNT_TYPE, AccountType.USER.getUsername());
            currentAccountType = AccountType.fromUsername(username);
        }
    }
}