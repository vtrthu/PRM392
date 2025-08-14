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

    @Insert
    void insertGameRecord(GameRecord gameRecord);

    @Delete
    void deleteGameRecord(GameRecord gameRecord);

    @Query("DELETE FROM game_records")
    void deleteAllGameRecords();
}
