package com.example.aviatorcrash.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "game_records")
public class GameRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private Date timestamp;
    private double betAmount;
    private double multiplier;
    private double cashoutAmount;
    private boolean isWin;
    private long gameDuration;
    private String username; // Add username field for user-specific history

    public GameRecord(Date timestamp, double betAmount, double multiplier, 
                     double cashoutAmount, boolean isWin, long gameDuration, String username) {
        this.timestamp = timestamp;
        this.betAmount = betAmount;
        this.multiplier = multiplier;
        this.cashoutAmount = cashoutAmount;
        this.isWin = isWin;
        this.gameDuration = gameDuration;
        this.username = username;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public double getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(double betAmount) {
        this.betAmount = betAmount;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getCashoutAmount() {
        return cashoutAmount;
    }

    public void setCashoutAmount(double cashoutAmount) {
        this.cashoutAmount = cashoutAmount;
    }

    public boolean isWin() {
        return isWin;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public long getGameDuration() {
        return gameDuration;
    }

    public void setGameDuration(long gameDuration) {
        this.gameDuration = gameDuration;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
