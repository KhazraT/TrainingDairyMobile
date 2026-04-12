package ru.squidory.trainingdairymobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Кэш активной сессии.
 * Используется для локального хранения данных сессии во время выполнения тренировки.
 */
@Entity(tableName = "active_session_cache")
public class ActiveSessionEntity {

    @PrimaryKey
    public long sessionId;

    public long workoutId;

    public String workoutName;

    public String workoutComment;

    // Timestamp начала сессии (ms)
    public long startedAt;

    // Статус: "IN_PROGRESS", "COMPLETED", "ABANDONED"
    public String status;

    // Длительность таймера отдыха по умолчанию (секунды)
    public int restTimerSeconds;

    // JSON-сериализованный список упражнений (SessionExerciseResponse[])
    public String exercisesJson;

    // ID упражнений, которые были удалены во время сессии
    // JSON-сериализованный List<Long>
    public String removedExerciseIdsJson;

    // Флаг: сессия синхронизирована с сервером
    public boolean isSynced;

    // Timestamp последнего обновления (ms)
    public long lastUpdatedAt;

    public ActiveSessionEntity() {}

    @Ignore
    public ActiveSessionEntity(long sessionId, long workoutId, String workoutName,
                               String workoutComment, long startedAt, String status,
                               int restTimerSeconds, String exercisesJson) {
        this.sessionId = sessionId;
        this.workoutId = workoutId;
        this.workoutName = workoutName;
        this.workoutComment = workoutComment;
        this.startedAt = startedAt;
        this.status = status;
        this.restTimerSeconds = restTimerSeconds;
        this.exercisesJson = exercisesJson;
        this.removedExerciseIdsJson = "[]";
        this.isSynced = false;
        this.lastUpdatedAt = System.currentTimeMillis();
    }
}
