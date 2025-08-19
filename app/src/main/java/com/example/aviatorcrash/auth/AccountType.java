package com.example.aviatorcrash.auth;

/**
 * Account types for educational gambling demo
 * Purpose: Show the reality of online gambling
 */
public enum AccountType {
    ADMIN("Admin (Nhà Cái)", "admin", "Tài khoản nhà cái - tỷ lệ thắng rất cao"),
    USER("Player (Người Chơi)", "player", "Tài khoản người chơi - tỷ lệ thắng giảm dần theo thời gian");

    private final String displayName;
    private final String username;
    private final String description;

    AccountType(String displayName, String username, String description) {
        this.displayName = displayName;
        this.username = username;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUsername() {
        return username;
    }

    public String getDescription() {
        return description;
    }

    public static AccountType fromUsername(String username) {
        for (AccountType type : values()) {
            if (type.getUsername().equals(username)) {
                return type;
            }
        }
        return USER; // Default to user account
    }
}
