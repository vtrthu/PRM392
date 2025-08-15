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
        // Thuật toán cải tiến dựa trên thống kê thực tế từ các game crash phổ biến
        double randomValue = random.nextDouble();
        
        // Phân phối xác suất dựa trên nghiên cứu thực tế:
        // ~51% crash trước 2x, 3% crash 1.1x-1.3x, nhiều người chơi rút tiền 1.5x-2.5x
        double crashPoint;
        
        if (randomValue < 0.001) {
            // 0.1% xác suất Jackpot 100x
            crashPoint = MAX_MULTIPLIER;
        } else if (randomValue < 0.006) {
            // 0.5% xác suất Mega Win 50x-100x
            crashPoint = 50.0 + random.nextDouble() * 50.0;
        } else if (randomValue < 0.04) {
            // 3.4% xác suất 10x-50x
            crashPoint = 10.0 + random.nextDouble() * 40.0;
        } else if (randomValue < 0.12) {
            // 8% xác suất 5x-10x
            crashPoint = 5.0 + random.nextDouble() * 5.0;
        } else if (randomValue < 0.25) {
            // 13% xác suất 2.5x-5x
            crashPoint = 2.5 + random.nextDouble() * 2.5;
        } else if (randomValue < 0.49) {
            // 24% xác suất 2x-2.5x (sweet spot cho người chơi)
            crashPoint = 2.0 + random.nextDouble() * 0.5;
        } else if (randomValue < 0.76) {
            // 27% xác suất 1.5x-2x (nhiều người chọn rút ở đây)
            crashPoint = 1.5 + random.nextDouble() * 0.5;
        } else if (randomValue < 0.97) {
            // 21% xác suất 1.2x-1.5x (vẫn có lợi nhuận nhưng ít rủi ro)
            crashPoint = 1.2 + random.nextDouble() * 0.3;
        } else {
            // 3% xác suất 1.01x-1.2x (crash sớm như thực tế)
            crashPoint = 1.01 + random.nextDouble() * 0.19;
        }
        
        // Giảm noise để ổn định hơn
        double noise = (random.nextDouble() - 0.5) * 0.05; // ±2.5% noise
        crashPoint = crashPoint * (1.0 + noise);
        
        // Áp dụng house edge nhẹ hơn
        crashPoint = crashPoint / (1.0 - HOUSE_EDGE * 0.8); // Giảm house edge impact
        
        // Đảm bảo crash point nằm trong giới hạn hợp lý
        crashPoint = Math.max(1.01, Math.min(crashPoint, MAX_MULTIPLIER));
        
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
    
    // Hàm test để kiểm tra phân phối xác suất mới
    public void testProbabilityDistribution(int iterations) {
        int[] ranges = new int[8]; // 8 khoảng xác suất chi tiết hơn
        double totalMultiplier = 0.0;
        double maxMultiplier = 0.0;
        double minMultiplier = Double.MAX_VALUE;
        int crashBefore2x = 0; // Đếm số lần crash trước 2x
        
        for (int i = 0; i < iterations; i++) {
            double crashPoint = generateCrashPoint();
            totalMultiplier += crashPoint;
            maxMultiplier = Math.max(maxMultiplier, crashPoint);
            minMultiplier = Math.min(minMultiplier, crashPoint);
            
            if (crashPoint < 2.0) {
                crashBefore2x++;
            }
            
            if (crashPoint >= 100.0) {
                ranges[0]++; // Jackpot 100x
            } else if (crashPoint >= 50.0) {
                ranges[1]++; // Mega Win 50x-100x
            } else if (crashPoint >= 10.0) {
                ranges[2]++; // 10x-50x
            } else if (crashPoint >= 5.0) {
                ranges[3]++; // 5x-10x
            } else if (crashPoint >= 2.5) {
                ranges[4]++; // 2.5x-5x
            } else if (crashPoint >= 2.0) {
                ranges[5]++; // 2x-2.5x
            } else if (crashPoint >= 1.5) {
                ranges[6]++; // 1.5x-2x
            } else {
                ranges[7]++; // 1.01x-1.5x
            }
        }
        
        System.out.println("=== PHÂN PHỐI XÁC SUẤT CẢI TIẾN (DỰA TRÊN THỐNG KÊ THỰC TẾ) ===");
        System.out.println("Tổng số lần test: " + iterations);
        System.out.println("Jackpot 100x: " + ranges[0] + " (" + String.format("%.3f", (double)ranges[0]/iterations*100) + "%)");
        System.out.println("Mega Win 50x-100x: " + ranges[1] + " (" + String.format("%.3f", (double)ranges[1]/iterations*100) + "%)");
        System.out.println("10x-50x: " + ranges[2] + " (" + String.format("%.3f", (double)ranges[2]/iterations*100) + "%)");
        System.out.println("5x-10x: " + ranges[3] + " (" + String.format("%.3f", (double)ranges[3]/iterations*100) + "%)");
        System.out.println("2.5x-5x: " + ranges[4] + " (" + String.format("%.3f", (double)ranges[4]/iterations*100) + "%)");
        System.out.println("2x-2.5x: " + ranges[5] + " (" + String.format("%.3f", (double)ranges[5]/iterations*100) + "%)");
        System.out.println("1.5x-2x: " + ranges[6] + " (" + String.format("%.3f", (double)ranges[6]/iterations*100) + "%)");
        System.out.println("1.01x-1.5x: " + ranges[7] + " (" + String.format("%.3f", (double)ranges[7]/iterations*100) + "%)");
        System.out.println("--- THỐNG KÊ QUAN TRỌNG ---");
        System.out.println("Crash trước 2x: " + crashBefore2x + " (" + String.format("%.1f", (double)crashBefore2x/iterations*100) + "%) - Mục tiêu: ~51%");
        System.out.println("Multiplier trung bình: " + String.format("%.2f", totalMultiplier/iterations));
        System.out.println("Multiplier cao nhất: " + String.format("%.2f", maxMultiplier));
        System.out.println("Multiplier thấp nhất: " + String.format("%.2f", minMultiplier));
        System.out.println("================================================================");
    }
}
