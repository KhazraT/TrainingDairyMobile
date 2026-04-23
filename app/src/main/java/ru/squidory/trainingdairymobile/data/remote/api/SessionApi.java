package ru.squidory.trainingdairymobile.data.remote.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.squidory.trainingdairymobile.data.model.CompleteSessionRequest;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseRequest;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.SessionHistoryResponse;
import ru.squidory.trainingdairymobile.data.model.SessionRequest;
import ru.squidory.trainingdairymobile.data.model.SessionResponse;
import ru.squidory.trainingdairymobile.data.model.SessionSetRequest;
import ru.squidory.trainingdairymobile.data.model.SessionSetResponse;
import ru.squidory.trainingdairymobile.data.model.SessionSummaryResponse;
import ru.squidory.trainingdairymobile.data.model.StartSessionRequest;

public interface SessionApi {

    // === Начало и завершение сессии ===

    /**
     * Начать сессию.
     * POST /api/sessions/start
     * Body: { "workoutId": 123 }
     * Response: SessionResponse с exercises, plannedSets, restTimerSeconds
     */
    @POST("sessions/start")
    Call<SessionResponse> startSession(@Body StartSessionRequest request);

    /**
     * Устаревший endpoint (совместимость).
     * POST /api/sessions/start
     */
    @POST("sessions/start")
    Call<SessionResponse> startSessionLegacy(@Body SessionRequest request);

    /**
     * Завершить тренировку (создаёт сессию на лету).
     * POST /api/sessions/complete
     * Body: CompleteSessionRequest с workoutId
     * Response: SessionResponse с sessionId, totalTonnage, totalSets
     */
    @POST("sessions/complete")
    Call<SessionResponse> completeSession(@Body CompleteSessionRequest request);

    /**
     * Устаревший endpoint (совместимость).
     * PUT /api/sessions/{id}/finish
     */
    @PUT("sessions/{id}/finish")
    Call<SessionResponse> finishSession(@Path("id") long id);

    // === Получение сессии ===

    /**
     * Получить сессию по ID.
     * GET /api/sessions/{sessionId}
     */
    @GET("sessions/{id}")
    Call<SessionResponse> getSessionById(@Path("id") long id);

    /**
     * Получить все сессии пользователя.
     * GET /api/sessions
     */
    @GET("sessions")
    Call<List<SessionResponse>> getSessions();

    /**
     * Получить сессии по тренировке.
     * GET /api/workouts/{workoutId}/sessions
     */
    @GET("workouts/{workoutId}/sessions")
    Call<List<SessionResponse>> getSessionsByWorkout(@Path("workoutId") long workoutId);

    /**
     * Получить историю сессий пользователя, сгруппированную по дням.
     * GET /api/sessions/history
     * @param year Год (опционально, по умолчанию текущий)
     * @param month Месяц 1-12 (опционально, по умолчанию текущий)
     * @param page Номер страницы (по умолчанию 0)
     * @param size Размер страницы (по умолчанию 20)
     */
    @GET("sessions/history")
    Call<SessionHistoryResponse> getSessionHistory(
            @Query("year") Integer year,
            @Query("month") Integer month,
            @Query("page") Integer page,
            @Query("size") Integer size
    );

    /**
     * Получить статистику сессии.
     * GET /api/sessions/{sessionId}/summary
     */
    @GET("sessions/{sessionId}/summary")
    Call<SessionSummaryResponse> getSessionSummary(@Path("sessionId") long sessionId);

    // === Управление сессией ===

    /**
     * Обновить сессию.
     * PUT /api/sessions/{id}
     */
    @PUT("sessions/{id}")
    Call<SessionResponse> updateSession(@Path("id") long id, @Body SessionRequest request);

    /**
     * Удалить сессию.
     * DELETE /api/sessions/{id}
     */
    @DELETE("sessions/{id}")
    Call<Void> deleteSession(@Path("id") long id);

    // === Упражнения в сессии ===

    /**
     * Получить упражнения сессии.
     * GET /api/session-exercises/{sessionId}
     */
    @GET("session-exercises/{sessionId}")
    Call<List<SessionExerciseResponse>> getSessionExercises(@Path("sessionId") long sessionId);

    /**
     * Добавить упражнение в сессию.
     * POST /api/sessions/{sessionId}/exercises
     * Body: { "exerciseId": 15 }
     */
    @POST("sessions/{sessionId}/exercises")
    Call<SessionExerciseResponse> addExerciseToSession(@Path("sessionId") long sessionId, @Body SessionExerciseRequest request);

    /**
     * Обновить упражнение в сессии.
     * PUT /api/session-exercises/{id}
     */
    @PUT("session-exercises/{id}")
    Call<SessionExerciseResponse> updateSessionExercise(@Path("id") long id, @Body SessionExerciseRequest request);

    /**
     * Удалить упражнение из сессии.
     * DELETE /api/session-exercises/{id}
     */
    @DELETE("session-exercises/{id}")
    Call<Void> deleteSessionExercise(@Path("id") long id);

    /**
     * Изменить порядок упражнений в сессии.
     * PUT /api/sessions/{sessionId}/exercises/reorder
     * Body: { "exerciseIds": [789, 791, 790] }
     */
    @PUT("sessions/{sessionId}/exercises/reorder")
    Call<Void> reorderSessionExercises(@Path("sessionId") long sessionId, @Body List<Long> exerciseIds);

    // === Подходы (одиночные, НЕ используются в MVP) ===

    /**
     * Получить подходы упражнения в сессии.
     * GET /api/session-exercises/{sessionExerciseId}/sets
     */
    @GET("session-exercises/{sessionExerciseId}/sets")
    Call<List<SessionSetResponse>> getSessionSets(@Path("sessionExerciseId") long sessionExerciseId);

    /**
     * Записать подход (одиночный).
     * POST /api/session-exercises/{sessionExerciseId}/sets
     * НЕ используется в MVP — подходы отправляются пакетно при завершении.
     */
    @POST("session-exercises/{sessionExerciseId}/sets")
    Call<SessionSetResponse> createSessionSet(@Path("sessionExerciseId") long sessionExerciseId, @Body SessionSetRequest request);

    /**
     * Обновить подход.
     * PUT /api/session-sets/{id}
     */
    @PUT("session-sets/{id}")
    Call<SessionSetResponse> updateSessionSet(@Path("id") long id, @Body SessionSetRequest request);

    /**
     * Удалить подход.
     * DELETE /api/session-sets/{id}
     */
    @DELETE("session-sets/{id}")
    Call<Void> deleteSessionSet(@Path("id") long id);
}
