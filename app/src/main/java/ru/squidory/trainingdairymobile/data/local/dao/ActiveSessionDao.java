package ru.squidory.trainingdairymobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import ru.squidory.trainingdairymobile.data.local.entity.ActiveSessionEntity;

/**
 * DAO для кэширования активной сессии.
 * Используется для локального хранения данных сессии во время выполнения тренировки.
 */
@Dao
public interface ActiveSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(ActiveSessionEntity session);

    @Update
    void update(ActiveSessionEntity session);

    @Query("SELECT * FROM active_session_cache WHERE sessionId = :sessionId")
    ActiveSessionEntity getSessionById(long sessionId);

    @Query("SELECT * FROM active_session_cache WHERE status = 'IN_PROGRESS' LIMIT 1")
    ActiveSessionEntity getActiveSession();

    @Query("DELETE FROM active_session_cache WHERE sessionId = :sessionId")
    void deleteSession(long sessionId);

    @Query("DELETE FROM active_session_cache")
    void deleteAll();

    @Query("UPDATE active_session_cache SET exercisesJson = :exercisesJson, lastUpdatedAt = :lastUpdatedAt WHERE sessionId = :sessionId")
    void updateExercises(long sessionId, String exercisesJson, long lastUpdatedAt);

    @Query("UPDATE active_session_cache SET removedExerciseIdsJson = :removedIdsJson, lastUpdatedAt = :lastUpdatedAt WHERE sessionId = :sessionId")
    void updateRemovedExerciseIds(long sessionId, String removedIdsJson, long lastUpdatedAt);

    @Query("UPDATE active_session_cache SET isSynced = :isSynced WHERE sessionId = :sessionId")
    void updateSyncStatus(long sessionId, boolean isSynced);

    @Query("UPDATE active_session_cache SET status = :status, isSynced = :isSynced WHERE sessionId = :sessionId")
    void updateStatus(long sessionId, String status, boolean isSynced);
}
