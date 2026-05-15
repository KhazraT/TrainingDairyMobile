package ru.squidory.trainingdairymobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import ru.squidory.trainingdairymobile.data.local.entity.AntiGSessionEntity;

/**
 * DAO for anti-G breathing training sessions.
 */
@Dao
public interface AntiGSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AntiGSessionEntity session);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AntiGSessionEntity> sessions);

    @Delete
    void delete(AntiGSessionEntity session);

    @Query("DELETE FROM anti_g_sessions")
    void deleteAll();

    @Query("SELECT * FROM anti_g_sessions ORDER BY date DESC")
    List<AntiGSessionEntity> getAllSessions();

    @Query("SELECT * FROM anti_g_sessions WHERE id = :id")
    AntiGSessionEntity getSessionById(long id);

    @Query("SELECT * FROM anti_g_sessions WHERE userId = :userId ORDER BY date DESC")
    List<AntiGSessionEntity> getUserSessions(long userId);

    @Query("SELECT COUNT(*) FROM anti_g_sessions")
    int getSessionsCount();
}