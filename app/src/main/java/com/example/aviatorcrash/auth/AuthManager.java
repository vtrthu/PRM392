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
    private static final String KEY_CURRENT_USERNAME = "current_username";
    private static final String KEY_TOTAL_GAMES_USER = "total_games_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_REGISTERED_USERS = "registered_users";
    
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
            
            // If this is a newly registered user, mark them as established after first login
            if (isNewlyRegisteredUser(username)) {
                markAsEstablishedUser(username);
            }
            
            prefs.edit()
                    .putString(KEY_ACCOUNT_TYPE, accountType.getUsername())
                    .putString(KEY_CURRENT_USERNAME, username) // Store actual username
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
        } else if (isRegisteredUser(username, password)) {
            return AccountType.USER; // Registered users get USER account type
        }
        return null; // Invalid credentials
    }

    public void logout() {
        currentAccountType = null;
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_CURRENT_USERNAME) // Clear actual username
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
     * Get the actual username (not the AccountType username)
     */
    public String getCurrentUsername() {
        return prefs.getString(KEY_CURRENT_USERNAME, null);
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
            } else if (gamesPlayed <= 8) {
                return 0.60; // 60% as house edge starts
            } else if (gamesPlayed <= 10) {
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

    /**
     * Register new user account
     */
    public boolean register(String username, String password) {
        try {
            // Get existing registered users
            String existingUsers = prefs.getString(KEY_REGISTERED_USERS, "");
            
            // Check if username already exists
            if (existingUsers.contains(username + ":")) {
                return false; // Username already exists
            }
            
            // Add new user (format: username:password,username:password,...)
            String newUser = username + ":" + password;
            String updatedUsers = existingUsers.isEmpty() ? newUser : existingUsers + "," + newUser;
            
            prefs.edit()
                    .putString(KEY_REGISTERED_USERS, updatedUsers)
                    .apply();
            
            // Mark this user as newly registered (for balance reset)
            markAsNewUser(username);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Mark user as newly registered for balance reset
     */
    private void markAsNewUser(String username) {
        String newUsers = prefs.getString("new_users", "");
        if (!newUsers.contains(username)) {
            String updatedNewUsers = newUsers.isEmpty() ? username : newUsers + "," + username;
            prefs.edit()
                    .putString("new_users", updatedNewUsers)
                    .apply();
            
            // Debug log
            android.util.Log.d("AuthManager", "Marked user as new: " + username + ", List: " + updatedNewUsers);
        }
    }

    /**
     * Check if user is newly registered (for balance reset)
     */
    public boolean isNewlyRegisteredUser(String username) {
        if (username == null) return false;
        
        String newUsers = prefs.getString("new_users", "");
        boolean isNew = newUsers.contains(username);
        
        // Debug log
        android.util.Log.d("AuthManager", "Checking if user is new: " + username + ", List: " + newUsers + ", Result: " + isNew);
        
        return isNew;
    }

    /**
     * Remove user from newly registered list after first login
     */
    public void markAsEstablishedUser(String username) {
        String newUsers = prefs.getString("new_users", "");
        if (newUsers.contains(username)) {
            String updatedNewUsers = newUsers.replace(username + ",", "").replace("," + username, "").replace(username, "");
            prefs.edit()
                    .putString("new_users", updatedNewUsers)
                    .apply();
        }
    }

    /**
     * Change password for current user
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        if (currentAccountType == null) {
            return false;
        }

        String username = currentAccountType.getUsername();
        
        // For built-in accounts (admin, toiyeufpt), don't allow password change
        if (username.equals(ADMIN_USERNAME) || username.equals(PLAYER_USERNAME)) {
            return false; // Built-in accounts cannot change password
        }

        // Verify current password
        if (validateCredentials(username, currentPassword) == null) {
            return false;
        }

        // Update password for registered user
        String registeredUsers = prefs.getString(KEY_REGISTERED_USERS, "");
        if (registeredUsers.contains(username + ":")) {
            // Replace old password with new password
            String oldEntry = username + ":" + currentPassword;
            String newEntry = username + ":" + newPassword;
            String updatedUsers = registeredUsers.replace(oldEntry, newEntry);
            
            prefs.edit()
                    .putString(KEY_REGISTERED_USERS, updatedUsers)
                    .apply();
            
            return true;
        }

        return false;
    }

    /**
     * Check if current user can change password
     */
    public boolean canChangePassword() {
        if (currentAccountType == null) {
            return false;
        }

        String username = currentAccountType.getUsername();
        // Built-in accounts cannot change password
        return !username.equals(ADMIN_USERNAME) && !username.equals(PLAYER_USERNAME);
    }

    /**
     * Check if username already exists
     */
    public boolean isUsernameExists(String username) {
        String existingUsers = prefs.getString(KEY_REGISTERED_USERS, "");
        return existingUsers.contains(username + ":");
    }

    /**
     * Check if user is a registered user (not admin or demo player)
     */
    private boolean isRegisteredUser(String username, String password) {
        String existingUsers = prefs.getString(KEY_REGISTERED_USERS, "");
        String userToCheck = username + ":" + password;
        return existingUsers.contains(userToCheck);
    }
}