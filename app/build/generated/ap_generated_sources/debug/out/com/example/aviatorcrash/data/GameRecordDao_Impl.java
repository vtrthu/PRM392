package com.example.aviatorcrash.data;

import android.database.Cursor;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class GameRecordDao_Impl implements GameRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GameRecord> __insertionAdapterOfGameRecord;

  private final EntityDeletionOrUpdateAdapter<GameRecord> __deletionAdapterOfGameRecord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllGameRecords;

  public GameRecordDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGameRecord = new EntityInsertionAdapter<GameRecord>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `game_records` (`id`,`timestamp`,`betAmount`,`multiplier`,`cashoutAmount`,`isWin`,`gameDuration`,`username`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, GameRecord value) {
        stmt.bindLong(1, value.getId());
        final Long _tmp = AppDatabase.Converters.dateToTimestamp(value.getTimestamp());
        if (_tmp == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindLong(2, _tmp);
        }
        stmt.bindDouble(3, value.getBetAmount());
        stmt.bindDouble(4, value.getMultiplier());
        stmt.bindDouble(5, value.getCashoutAmount());
        final int _tmp_1 = value.isWin() ? 1 : 0;
        stmt.bindLong(6, _tmp_1);
        stmt.bindLong(7, value.getGameDuration());
        if (value.getUsername() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getUsername());
        }
      }
    };
    this.__deletionAdapterOfGameRecord = new EntityDeletionOrUpdateAdapter<GameRecord>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `game_records` WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, GameRecord value) {
        stmt.bindLong(1, value.getId());
      }
    };
    this.__preparedStmtOfDeleteAllGameRecords = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM game_records";
        return _query;
      }
    };
  }

  @Override
  public void insertGameRecord(final GameRecord gameRecord) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfGameRecord.insert(gameRecord);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteGameRecord(final GameRecord gameRecord) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfGameRecord.handle(gameRecord);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAllGameRecords() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllGameRecords.acquire();
    __db.beginTransaction();
    try {
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDeleteAllGameRecords.release(_stmt);
    }
  }

  @Override
  public LiveData<List<GameRecord>> getAllGameRecords() {
    final String _sql = "SELECT * FROM game_records ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<List<GameRecord>>() {
      @Override
      public List<GameRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfBetAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "betAmount");
          final int _cursorIndexOfMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "multiplier");
          final int _cursorIndexOfCashoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cashoutAmount");
          final int _cursorIndexOfIsWin = CursorUtil.getColumnIndexOrThrow(_cursor, "isWin");
          final int _cursorIndexOfGameDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "gameDuration");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final List<GameRecord> _result = new ArrayList<GameRecord>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final GameRecord _item;
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            _tmpTimestamp = AppDatabase.Converters.fromTimestamp(_tmp);
            final double _tmpBetAmount;
            _tmpBetAmount = _cursor.getDouble(_cursorIndexOfBetAmount);
            final double _tmpMultiplier;
            _tmpMultiplier = _cursor.getDouble(_cursorIndexOfMultiplier);
            final double _tmpCashoutAmount;
            _tmpCashoutAmount = _cursor.getDouble(_cursorIndexOfCashoutAmount);
            final boolean _tmpIsWin;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWin);
            _tmpIsWin = _tmp_1 != 0;
            final long _tmpGameDuration;
            _tmpGameDuration = _cursor.getLong(_cursorIndexOfGameDuration);
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            _item = new GameRecord(_tmpTimestamp,_tmpBetAmount,_tmpMultiplier,_tmpCashoutAmount,_tmpIsWin,_tmpGameDuration,_tmpUsername);
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<GameRecord> getAllGameRecordsDirect() {
    final String _sql = "SELECT * FROM game_records ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfBetAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "betAmount");
      final int _cursorIndexOfMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "multiplier");
      final int _cursorIndexOfCashoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cashoutAmount");
      final int _cursorIndexOfIsWin = CursorUtil.getColumnIndexOrThrow(_cursor, "isWin");
      final int _cursorIndexOfGameDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "gameDuration");
      final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
      final List<GameRecord> _result = new ArrayList<GameRecord>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final GameRecord _item;
        final Date _tmpTimestamp;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfTimestamp)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
        }
        _tmpTimestamp = AppDatabase.Converters.fromTimestamp(_tmp);
        final double _tmpBetAmount;
        _tmpBetAmount = _cursor.getDouble(_cursorIndexOfBetAmount);
        final double _tmpMultiplier;
        _tmpMultiplier = _cursor.getDouble(_cursorIndexOfMultiplier);
        final double _tmpCashoutAmount;
        _tmpCashoutAmount = _cursor.getDouble(_cursorIndexOfCashoutAmount);
        final boolean _tmpIsWin;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsWin);
        _tmpIsWin = _tmp_1 != 0;
        final long _tmpGameDuration;
        _tmpGameDuration = _cursor.getLong(_cursorIndexOfGameDuration);
        final String _tmpUsername;
        if (_cursor.isNull(_cursorIndexOfUsername)) {
          _tmpUsername = null;
        } else {
          _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
        }
        _item = new GameRecord(_tmpTimestamp,_tmpBetAmount,_tmpMultiplier,_tmpCashoutAmount,_tmpIsWin,_tmpGameDuration,_tmpUsername);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<GameRecord>> getRecentGameRecords() {
    final String _sql = "SELECT * FROM game_records ORDER BY timestamp DESC LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<List<GameRecord>>() {
      @Override
      public List<GameRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfBetAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "betAmount");
          final int _cursorIndexOfMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "multiplier");
          final int _cursorIndexOfCashoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cashoutAmount");
          final int _cursorIndexOfIsWin = CursorUtil.getColumnIndexOrThrow(_cursor, "isWin");
          final int _cursorIndexOfGameDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "gameDuration");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final List<GameRecord> _result = new ArrayList<GameRecord>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final GameRecord _item;
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            _tmpTimestamp = AppDatabase.Converters.fromTimestamp(_tmp);
            final double _tmpBetAmount;
            _tmpBetAmount = _cursor.getDouble(_cursorIndexOfBetAmount);
            final double _tmpMultiplier;
            _tmpMultiplier = _cursor.getDouble(_cursorIndexOfMultiplier);
            final double _tmpCashoutAmount;
            _tmpCashoutAmount = _cursor.getDouble(_cursorIndexOfCashoutAmount);
            final boolean _tmpIsWin;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWin);
            _tmpIsWin = _tmp_1 != 0;
            final long _tmpGameDuration;
            _tmpGameDuration = _cursor.getLong(_cursorIndexOfGameDuration);
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            _item = new GameRecord(_tmpTimestamp,_tmpBetAmount,_tmpMultiplier,_tmpCashoutAmount,_tmpIsWin,_tmpGameDuration,_tmpUsername);
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<GameRecord>> getGameRecordsByUser(final String username) {
    final String _sql = "SELECT * FROM game_records WHERE username = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (username == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, username);
    }
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<List<GameRecord>>() {
      @Override
      public List<GameRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfBetAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "betAmount");
          final int _cursorIndexOfMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "multiplier");
          final int _cursorIndexOfCashoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cashoutAmount");
          final int _cursorIndexOfIsWin = CursorUtil.getColumnIndexOrThrow(_cursor, "isWin");
          final int _cursorIndexOfGameDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "gameDuration");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final List<GameRecord> _result = new ArrayList<GameRecord>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final GameRecord _item;
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            _tmpTimestamp = AppDatabase.Converters.fromTimestamp(_tmp);
            final double _tmpBetAmount;
            _tmpBetAmount = _cursor.getDouble(_cursorIndexOfBetAmount);
            final double _tmpMultiplier;
            _tmpMultiplier = _cursor.getDouble(_cursorIndexOfMultiplier);
            final double _tmpCashoutAmount;
            _tmpCashoutAmount = _cursor.getDouble(_cursorIndexOfCashoutAmount);
            final boolean _tmpIsWin;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWin);
            _tmpIsWin = _tmp_1 != 0;
            final long _tmpGameDuration;
            _tmpGameDuration = _cursor.getLong(_cursorIndexOfGameDuration);
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            _item = new GameRecord(_tmpTimestamp,_tmpBetAmount,_tmpMultiplier,_tmpCashoutAmount,_tmpIsWin,_tmpGameDuration,_tmpUsername);
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<GameRecord> getGameRecordsByUserDirect(final String username) {
    final String _sql = "SELECT * FROM game_records WHERE username = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (username == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, username);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfBetAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "betAmount");
      final int _cursorIndexOfMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "multiplier");
      final int _cursorIndexOfCashoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cashoutAmount");
      final int _cursorIndexOfIsWin = CursorUtil.getColumnIndexOrThrow(_cursor, "isWin");
      final int _cursorIndexOfGameDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "gameDuration");
      final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
      final List<GameRecord> _result = new ArrayList<GameRecord>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final GameRecord _item;
        final Date _tmpTimestamp;
        final Long _tmp;
        if (_cursor.isNull(_cursorIndexOfTimestamp)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
        }
        _tmpTimestamp = AppDatabase.Converters.fromTimestamp(_tmp);
        final double _tmpBetAmount;
        _tmpBetAmount = _cursor.getDouble(_cursorIndexOfBetAmount);
        final double _tmpMultiplier;
        _tmpMultiplier = _cursor.getDouble(_cursorIndexOfMultiplier);
        final double _tmpCashoutAmount;
        _tmpCashoutAmount = _cursor.getDouble(_cursorIndexOfCashoutAmount);
        final boolean _tmpIsWin;
        final int _tmp_1;
        _tmp_1 = _cursor.getInt(_cursorIndexOfIsWin);
        _tmpIsWin = _tmp_1 != 0;
        final long _tmpGameDuration;
        _tmpGameDuration = _cursor.getLong(_cursorIndexOfGameDuration);
        final String _tmpUsername;
        if (_cursor.isNull(_cursorIndexOfUsername)) {
          _tmpUsername = null;
        } else {
          _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
        }
        _item = new GameRecord(_tmpTimestamp,_tmpBetAmount,_tmpMultiplier,_tmpCashoutAmount,_tmpIsWin,_tmpGameDuration,_tmpUsername);
        final int _tmpId;
        _tmpId = _cursor.getInt(_cursorIndexOfId);
        _item.setId(_tmpId);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<List<GameRecord>> getRecentGameRecordsByUser(final String username) {
    final String _sql = "SELECT * FROM game_records WHERE username = ? ORDER BY timestamp DESC LIMIT 50";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (username == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, username);
    }
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<List<GameRecord>>() {
      @Override
      public List<GameRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfBetAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "betAmount");
          final int _cursorIndexOfMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "multiplier");
          final int _cursorIndexOfCashoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cashoutAmount");
          final int _cursorIndexOfIsWin = CursorUtil.getColumnIndexOrThrow(_cursor, "isWin");
          final int _cursorIndexOfGameDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "gameDuration");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final List<GameRecord> _result = new ArrayList<GameRecord>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final GameRecord _item;
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            _tmpTimestamp = AppDatabase.Converters.fromTimestamp(_tmp);
            final double _tmpBetAmount;
            _tmpBetAmount = _cursor.getDouble(_cursorIndexOfBetAmount);
            final double _tmpMultiplier;
            _tmpMultiplier = _cursor.getDouble(_cursorIndexOfMultiplier);
            final double _tmpCashoutAmount;
            _tmpCashoutAmount = _cursor.getDouble(_cursorIndexOfCashoutAmount);
            final boolean _tmpIsWin;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWin);
            _tmpIsWin = _tmp_1 != 0;
            final long _tmpGameDuration;
            _tmpGameDuration = _cursor.getLong(_cursorIndexOfGameDuration);
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            _item = new GameRecord(_tmpTimestamp,_tmpBetAmount,_tmpMultiplier,_tmpCashoutAmount,_tmpIsWin,_tmpGameDuration,_tmpUsername);
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<List<GameRecord>> getWinningGames() {
    final String _sql = "SELECT * FROM game_records WHERE isWin = 1 ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<List<GameRecord>>() {
      @Override
      public List<GameRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfBetAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "betAmount");
          final int _cursorIndexOfMultiplier = CursorUtil.getColumnIndexOrThrow(_cursor, "multiplier");
          final int _cursorIndexOfCashoutAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "cashoutAmount");
          final int _cursorIndexOfIsWin = CursorUtil.getColumnIndexOrThrow(_cursor, "isWin");
          final int _cursorIndexOfGameDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "gameDuration");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final List<GameRecord> _result = new ArrayList<GameRecord>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final GameRecord _item;
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            _tmpTimestamp = AppDatabase.Converters.fromTimestamp(_tmp);
            final double _tmpBetAmount;
            _tmpBetAmount = _cursor.getDouble(_cursorIndexOfBetAmount);
            final double _tmpMultiplier;
            _tmpMultiplier = _cursor.getDouble(_cursorIndexOfMultiplier);
            final double _tmpCashoutAmount;
            _tmpCashoutAmount = _cursor.getDouble(_cursorIndexOfCashoutAmount);
            final boolean _tmpIsWin;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsWin);
            _tmpIsWin = _tmp_1 != 0;
            final long _tmpGameDuration;
            _tmpGameDuration = _cursor.getLong(_cursorIndexOfGameDuration);
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            _item = new GameRecord(_tmpTimestamp,_tmpBetAmount,_tmpMultiplier,_tmpCashoutAmount,_tmpIsWin,_tmpGameDuration,_tmpUsername);
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            _item.setId(_tmpId);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Double> getAverageMultiplier() {
    final String _sql = "SELECT AVG(multiplier) FROM game_records";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Double> getHighestMultiplier() {
    final String _sql = "SELECT MAX(multiplier) FROM game_records";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<Double>() {
      @Override
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if(_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Integer> getWinCount() {
    final String _sql = "SELECT COUNT(*) FROM game_records WHERE isWin = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if(_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Integer> getTotalGames() {
    final String _sql = "SELECT COUNT(*) FROM game_records";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if(_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Integer> getWinCountByUser(final String username) {
    final String _sql = "SELECT COUNT(*) FROM game_records WHERE username = ? AND isWin = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (username == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, username);
    }
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if(_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public LiveData<Integer> getTotalGamesByUser(final String username) {
    final String _sql = "SELECT COUNT(*) FROM game_records WHERE username = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (username == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, username);
    }
    return __db.getInvalidationTracker().createLiveData(new String[]{"game_records"}, false, new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if(_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
