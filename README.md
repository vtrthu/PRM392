# Aviator Crash Game - Android

## Mô tả
Aviator Crash là một game giải trí mô phỏng trò chơi crash game với giao diện đẹp và thuật toán random thông minh.

## Tính năng chính

### 🎮 Gameplay
- **Multiplier tối đa**: 100x (thay vì 1000x như trước)
- **Thuật toán random cải tiến**: Sử dụng phân phối mũ với bias để tạo trải nghiệm thực tế hơn
- **Jackpot system**: 0.1% xác suất đạt 100x
- **Mega Win**: 0.5% xác suất đạt 50x-100x
- **House edge**: 4% (giảm từ 5% để cân bằng hơn)

### 🎨 Giao diện
- **Material Design**: Giao diện hiện đại với dark theme
- **Color coding**: Màu sắc thay đổi theo multiplier
  - Xanh lá: < 2x
  - Cam: 2x - 5x
  - Vàng: 5x - 10x
  - Hồng: 10x - 20x
  - Tím: 20x - 50x
  - Đỏ: 50x - 100x
  - Vàng (Gold): 100x (Jackpot)

### 📊 Thống kê
- Lịch sử game chi tiết
- Tỷ lệ thắng
- Tổng số ván đã chơi
- Auto-cashout feature

### ⚙️ Cài đặt
- Tự động rút tiền với multiplier tùy chỉnh
- Reset balance
- Cài đặt game preferences

## Cải tiến thuật toán

### Thuật toán Random mới
```java
// Thuật toán hybrid với phân phối xác suất rõ ràng
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
    // 70% xác suất 1x-2x - sử dụng phân phối mũ để bias về 1x
    double exponentialValue = random.nextDouble();
    crashPoint = 1.0 + Math.pow(exponentialValue, 2.0);
}
```

### Phân phối xác suất (Cải tiến dựa trên thống kê thực tế)
- **48%**: Crash ở 1.01x - 2.0x (giảm từ 70% để hợp lý hơn)
- **24%**: Crash ở 2.0x - 2.5x (sweet spot cho người chơi)  
- **13%**: Crash ở 2.5x - 5.0x
- **8%**: Crash ở 5.0x - 10.0x
- **3.4%**: Crash ở 10.0x - 50.0x
- **0.5%**: Mega Win 50x-100x
- **0.1%**: Jackpot 100x

**Thống kê quan trọng**: ~51% crash trước 2x (phù hợp với game thực tế)

### Hiệu ứng đặc biệt
- **Vibration**: Rung khi thắng/thua
- **Toast messages**: Thông báo đặc biệt cho Jackpot/Mega Win
- **Color effects**: Màu sắc động theo multiplier

## Cài đặt và chạy

### Yêu cầu hệ thống
- Android API level 26+ (Android 8.0+)
- Android Studio Arctic Fox trở lên

### Build và chạy
```bash
# Clone project
git clone <repository-url>
cd PRM_BL5

# Build project
./gradlew clean build

# Chạy trên device/emulator
./gradlew installDebug
```

### Mở trong Android Studio
1. Mở Android Studio
2. Chọn "Open an existing project"
3. Chọn thư mục `PRM_BL5`
4. Đợi Gradle sync hoàn tất
5. Chạy app trên device/emulator

## Cấu trúc project

```
app/src/main/java/com/example/aviatorcrash/
├── MainActivity.java          # Màn hình chính
├── GameActivity.java          # Màn hình game
├── HistoryActivity.java       # Lịch sử game
├── SettingsActivity.java      # Cài đặt
├── adapter/
│   └── GameHistoryAdapter.java # Adapter cho RecyclerView
├── data/
│   ├── AppDatabase.java       # Room database
│   ├── GameRecord.java        # Entity
│   └── GameRecordDao.java     # DAO
├── game/
│   └── GameEngine.java        # Logic game chính
└── viewmodel/
    └── GameViewModel.java     # ViewModel cho MVVM
```

## Công nghệ sử dụng

- **Android Architecture Components**: ViewModel, LiveData, Room
- **Material Design Components**: Buttons, Cards, TextInputLayout
- **ViewBinding**: Thay thế findViewById
- **MVVM Pattern**: Architecture pattern
- **Room Database**: Local storage
- **RecyclerView**: Hiển thị danh sách
- **ConstraintLayout**: Layout system

## Tác giả
Android Fundamentals Team

## Phiên bản
1.0 - Phiên bản cải tiến với thuật toán random mới và giới hạn 100x
