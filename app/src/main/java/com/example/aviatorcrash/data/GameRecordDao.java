package com.example.aviatorcrash.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GameRecordDao {
    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    LiveData<List<GameRecord>> getAllGameRecords();

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC")
    List<GameRecord> getAllGameRecordsDirect();

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC LIMIT 50")
    LiveData<List<GameRecord>> getRecentGameRecords();

    // User-specific queries
    @Query("SELECT * FROM game_records WHERE username = :username ORDER BY timestamp DESC")
    LiveData<List<GameRecord>> getGameRecordsByUser(String username);

    @Query("SELECT * FROM game_records WHERE username = :username ORDER BY timestamp DESC")
    List<GameRecord> getGameRecordsByUserDirect(String username);

    @Query("SELECT * FROM game_records WHERE username = :username ORDER BY timestamp DESC LIMIT 50")
    LiveData<List<GameRecord>> getRecentGameRecordsByUser(String username);

    @Query("SELECT * FROM game_records WHERE isWin = 1 ORDER BY timestamp DESC")
    LiveData<List<GameRecord>> getWinningGames();

    @Query("SELECT AVG(multiplier) FROM game_records")
    LiveData<Double> getAverageMultiplier();

    @Query("SELECT MAX(multiplier) FROM game_records")
    LiveData<Double> getHighestMultiplier();

    @Query("SELECT COUNT(*) FROM game_records WHERE isWin = 1")
    LiveData<Integer> getWinCount();

    @Query("SELECT COUNT(*) FROM game_records")
    LiveData<Integer> getTotalGames();

    // User-specific statistics
    @Query("SELECT COUNT(*) FROM game_records WHERE username = :username AND isWin = 1")
    LiveData<Integer> getWinCountByUser(String username);

    @Query("SELECT COUNT(*) FROM game_records WHERE username = :username")
    LiveData<Integer> getTotalGamesByUser(String username);

    @Insert
    void insertGameRecord(GameRecord gameRecord);

    @Delete
    void deleteGameRecord(GameRecord gameRecord);

    @Query("DELETE FROM game_records")
    void deleteAllGameRecords();
}
