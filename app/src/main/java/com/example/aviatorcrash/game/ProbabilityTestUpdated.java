package com.example.aviatorcrash.game;

/**
 * Test class để verify phân phối xác suất mới của GameEngine
 * Sử dụng để kiểm tra tỷ lệ crash có phù hợp với thống kê thực tế không
 */
public class ProbabilityTestUpdated {
    
    public static void main(String[] args) {
        GameEngine engine = new GameEngine();
        
        System.out.println("========== KIỂM TRA PHÂN PHỐI XÁC SUẤT CẢI TIẾN ==========");
        System.out.println("Dựa trên nghiên cứu thực tế: ~51% crash trước 2x");
        System.out.println("Mục tiêu: Giảm tỷ lệ crash từ 1x-2x từ 70% xuống ~48%");
        System.out.println("=========================================================");
        
        // Test với 10,000 lần
        System.out.println("\n--- TEST 10,000 LẦN ---");
        engine.testProbabilityDistribution(10000);
        
        // Test với 100,000 lần để có kết quả chính xác hơn
        System.out.println("\n--- TEST 100,000 LẦN (ĐỘ CHÍNH XÁC CAO) ---");
        engine.testProbabilityDistribution(100000);
        
        System.out.println("\n========== KẾT LUẬN ==========");
        System.out.println("✓ Nếu tỷ lệ crash trước 2x ở khoảng 48-52% → THÀNH CÔNG");
        System.out.println("✗ Nếu tỷ lệ crash trước 2x > 60% hoặc < 40% → CẦN ĐIỀU CHỈNH");
        System.out.println("==============================");
    }
}
