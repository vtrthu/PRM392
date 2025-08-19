package com.example.aviatorcrash.bot;

public class BotEntry {
    public enum BotState {
        PENDING,    // Waiting to place bet
        BET_PLACED, // Has placed bet, waiting for cashout or crash
        CASHED_OUT, // Successfully cashed out
        CRASHED     // Crashed before cashout
    }

    private String name;
    private double betAmount;
    private double targetCashout;
    private double actualCashout;
    private double winAmount;
    private BotState state;
    private boolean isPlayer; // true if this represents the real player

    public BotEntry(String name, double betAmount, double targetCashout) {
        this.name = name;
        this.betAmount = betAmount;
        this.targetCashout = targetCashout;
        this.actualCashout = 0.0;
        this.winAmount = 0.0;
        this.state = BotState.PENDING;
        this.isPlayer = false;
    }

    // Constructor for player entry
    public BotEntry(String name, double betAmount, boolean isPlayer) {
        this.name = name;
        this.betAmount = betAmount;
        this.targetCashout = 0.0; // Player controls their own cashout
        this.actualCashout = 0.0;
        this.winAmount = 0.0;
        this.state = BotState.PENDING;
        this.isPlayer = isPlayer;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBetAmount() { return betAmount; }
    public void setBetAmount(double betAmount) { this.betAmount = betAmount; }

    public double getTargetCashout() { return targetCashout; }
    public void setTargetCashout(double targetCashout) { this.targetCashout = targetCashout; }

    public double getActualCashout() { return actualCashout; }
    public void setActualCashout(double actualCashout) { this.actualCashout = actualCashout; }

    public double getWinAmount() { return winAmount; }
    public void setWinAmount(double winAmount) { this.winAmount = winAmount; }

    public BotState getState() { return state; }
    public void setState(BotState state) { this.state = state; }

    public boolean isPlayer() { return isPlayer; }
    public void setPlayer(boolean player) { isPlayer = player; }

    public void cashout(double multiplier) {
        if (state == BotState.BET_PLACED) {
            this.actualCashout = multiplier;
            this.winAmount = betAmount * multiplier;
            this.state = BotState.CASHED_OUT;
        }
    }

    public void crash() {
        if (state == BotState.BET_PLACED) {
            this.actualCashout = 0.0;
            this.winAmount = 0.0;
            this.state = BotState.CRASHED;
        }
    }

    public void placeBet() {
        this.state = BotState.BET_PLACED;
    }

    @Override
    public String toString() {
        return "BotEntry{" +
                "name='" + name + '\'' +
                ", betAmount=" + betAmount +
                ", targetCashout=" + targetCashout +
                ", actualCashout=" + actualCashout +
                ", winAmount=" + winAmount +
                ", state=" + state +
                ", isPlayer=" + isPlayer +
                '}';
    }
}
