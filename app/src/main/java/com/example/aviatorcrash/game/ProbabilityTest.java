package com.example.aviatorcrash.game;

public class ProbabilityTest {
    public static void main(String[] args) {
        GameEngine gameEngine = new GameEngine();
        
        // Test với 100,000 lần để có kết quả chính xác
        System.out.println("Bắt đầu test phân phối xác suất...");
        gameEngine.testProbabilityDistribution(100000);
        
        // Test thêm với 1,000,000 lần để xác nhận
        System.out.println("\nTest với số lượng lớn hơn...");
        gameEngine.testProbabilityDistribution(1000000);
    }
}
