package ru.squidory.trainingdairymobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import ru.squidory.trainingdairymobile.data.local.entity.SessionEntity;

@Dao
public interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SessionEntity session);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SessionEntity> sessions);

    @Delete
    void delete(SessionEntity session);

    @Query("DELETE FROM sessions_cache")
    void deleteAll();

    @Query("SELECT * FROM sessions_cache ORDER BY startTime DESC")
    List<SessionEntity> getAllSessions();

    @Query("SELECT * FROM sessions_cache WHERE id = :id")
    SessionEntity getSessionById(long id);

    @Query("SELECT * FROM sessions_cache WHERE userId = :userId ORDER BY startTime DESC")
    List<SessionEntity> getUserSessions(long userId);

    @Query("SELECT * FROM sessions_cache WHERE workoutId = :workoutId ORDER BY startTime DESC")
    List<SessionEntity> getSessionsByWorkout(long workoutId);

    @Query("SELECT COUNT(*) FROM sessions_cache")
    int getSessionsCount();
}
