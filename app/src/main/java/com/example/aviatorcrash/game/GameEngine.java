package com.example.aviatorcrash.game;

import java.util.Random;

public class GameEngine {
    private static final double MIN_MULTIPLIER = 1.0;
    private static final double MAX_MULTIPLIER = 100.0; // Giới hạn tối đa 100x
    private static final double HOUSE_EDGE = 0.04; // 4% house edge để cân bằng hơn
    
    private Random random;
    
    public GameEngine() {
        this.random = new Random();
    }
    
    public double generateCrashPoint() {
        // Thuật toán hybrid với phân phối xác suất thực tế và randomness tốt
        double randomValue = random.nextDouble();
        
        // Sử dụng kết hợp giữa phân phối mũ và phân phối đều để tạo ra trải nghiệm cân bằng
        double crashPoint;
        
        if (randomValue < 0.001) {
            // 0.1% xác suất Jackpot 100x
            crashPoint = MAX_MULTIPLIER;
        } else if (randomValue < 0.006) {
            // 0.5% xác suất Mega Win 50x-100x
            crashPoint = 50.0 + random.nextDouble() * 50.0;
        } else if (randomValue < 0.05) {
            // 4.4% xác suất 20x-100x
            crashPoint = 20.0 + random.nextDouble() * 80.0;
        } else if (randomValue < 0.15) {
            // 10% xác suất 5x-20x
            crashPoint = 5.0 + random.nextDouble() * 15.0;
        } else if (randomValue < 0.30) {
            // 15% xác suất 2x-5x
            crashPoint = 2.0 + random.nextDouble() * 3.0;
        } else {
            // 70% xác suất 1x-2x - sử dụng phân phối mũ để tạo ra nhiều giá trị gần 1x
            double exponentialValue = random.nextDouble();
            crashPoint = 1.0 + Math.pow(exponentialValue, 2.0); // Bias về phía 1x
        }
        
        // Thêm randomness để tránh pattern dễ đoán
        double noise = (random.nextDouble() - 0.5) * 0.1; // ±5% noise
        crashPoint = crashPoint * (1.0 + noise);
        
        // Áp dụng house edge
        crashPoint = crashPoint / (1.0 - HOUSE_EDGE);
        
        // Đảm bảo crash point nằm trong giới hạn hợp lý
        crashPoint = Math.max(1.0, Math.min(crashPoint, MAX_MULTIPLIER));
        
        return crashPoint;
    }
    
    public double calculateMultiplier(long elapsedTimeMs) {
        // Convert milliseconds to seconds
        double elapsedTimeSeconds = elapsedTimeMs / 1000.0;
        
        // Cải thiện công thức tính multiplier để tăng chậm hơn
        // Sử dụng hàm mũ với hệ số nhỏ hơn để tăng chậm hơn
        return Math.exp(elapsedTimeSeconds * 0.08);
    }
    
    public boolean shouldCrash(double currentMultiplier, double crashPoint) {
        return currentMultiplier >= crashPoint;
    }
    
    public double calculateWinAmount(double betAmount, double multiplier) {
        return betAmount * multiplier;
    }
    
    public boolean isValidBet(double betAmount, double balance, double minBet, double maxBet) {
        return betAmount >= minBet && betAmount <= maxBet && betAmount <= balance;
    }
    
    public int getMultiplierColor(double multiplier) {
        if (multiplier < 2.0) {
            return 0xFF4CAF50; // Green
        } else if (multiplier < 5.0) {
            return 0xFFFF9800; // Orange
        } else if (multiplier < 10.0) {
            return 0xFFFFEB3B; // Yellow
        } else if (multiplier < 20.0) {
            return 0xFFE91E63; // Pink
        } else if (multiplier < 50.0) {
            return 0xFF9C27B0; // Purple
        } else if (multiplier >= 100.0) {
            return 0xFFFFD700; // Gold (Jackpot)
        } else {
            return 0xFFF44336; // Red (50x+)
        }
    }
    
    public boolean isJackpot(double multiplier) {
        return multiplier >= 100.0;
    }
    
    public boolean isMegaWin(double multiplier) {
        return multiplier >= 50.0;
    }
    
    // Hàm test để kiểm tra phân phối xác suất
    public void testProbabilityDistribution(int iterations) {
        int[] ranges = new int[6]; // 6 khoảng xác suất
        double totalMultiplier = 0.0;
        double maxMultiplier = 0.0;
        double minMultiplier = Double.MAX_VALUE;
        
        for (int i = 0; i < iterations; i++) {
            double crashPoint = generateCrashPoint();
            totalMultiplier += crashPoint;
            maxMultiplier = Math.max(maxMultiplier, crashPoint);
            minMultiplier = Math.min(minMultiplier, crashPoint);
            
            if (crashPoint >= 100.0) {
                ranges[0]++; // Jackpot 100x
            } else if (crashPoint >= 50.0) {
                ranges[1]++; // Mega Win 50x-100x
            } else if (crashPoint >= 20.0) {
                ranges[2]++; // 20x-100x
            } else if (crashPoint >= 5.0) {
                ranges[3]++; // 5x-20x
            } else if (crashPoint >= 2.0) {
                ranges[4]++; // 2x-5x
            } else {
                ranges[5]++; // 1x-2x
            }
        }
        
        System.out.println("=== PHÂN PHỐI XÁC SUẤT THỰC TẾ ===");
        System.out.println("Tổng số lần test: " + iterations);
        System.out.println("Jackpot 100x: " + ranges[0] + " (" + String.format("%.3f", (double)ranges[0]/iterations*100) + "%)");
        System.out.println("Mega Win 50x-100x: " + ranges[1] + " (" + String.format("%.3f", (double)ranges[1]/iterations*100) + "%)");
        System.out.println("20x-100x: " + ranges[2] + " (" + String.format("%.3f", (double)ranges[2]/iterations*100) + "%)");
        System.out.println("5x-20x: " + ranges[3] + " (" + String.format("%.3f", (double)ranges[3]/iterations*100) + "%)");
        System.out.println("2x-5x: " + ranges[4] + " (" + String.format("%.3f", (double)ranges[4]/iterations*100) + "%)");
        System.out.println("1x-2x: " + ranges[5] + " (" + String.format("%.3f", (double)ranges[5]/iterations*100) + "%)");
        System.out.println("Multiplier trung bình: " + String.format("%.2f", totalMultiplier/iterations));
        System.out.println("Multiplier cao nhất: " + String.format("%.2f", maxMultiplier));
        System.out.println("Multiplier thấp nhất: " + String.format("%.2f", minMultiplier));
        System.out.println("================================");
    }
}
