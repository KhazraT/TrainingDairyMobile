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
import ru.squidory.trainingdairymobile.data.model.BodyMeasurementProgress;
import ru.squidory.trainingdairymobile.data.model.BodyMeasurementResponse;
import ru.squidory.trainingdairymobile.data.model.DurationMonthlyStats;
import ru.squidory.trainingdairymobile.data.model.DurationWeeklyStats;
import ru.squidory.trainingdairymobile.data.model.ExerciseProgressResponse;
import ru.squidory.trainingdairymobile.data.model.ExerciseStatsResponse;
import ru.squidory.trainingdairymobile.data.model.MonthlyVolumeResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleSetsStats;
import ru.squidory.trainingdairymobile.data.model.MuscleStatsResponse;
import ru.squidory.trainingdairymobile.data.model.StatisticsRequest;
import ru.squidory.trainingdairymobile.data.model.StatisticsResponse;
import ru.squidory.trainingdairymobile.data.model.StatsSummaryResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutMonthlyStats;
import ru.squidory.trainingdairymobile.data.model.WorkoutStatsResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutWeeklyStats;

/**
 * API для получения статистики.
 */
public interface StatisticsApi {

    // === Статистика тела (измерения) ===

    @GET("statistics")
    Call<List<StatisticsResponse>> getStatistics(
            @Query("type") String type,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    @GET("statistics/type/{type}")
    Call<List<StatisticsResponse>> getStatisticsByType(@Path("type") String type);

    @GET("statistics/{type}/latest")
    Call<StatisticsResponse> getLatestStatistics(@Path("type") String type);

    @POST("statistics")
    Call<StatisticsResponse> createStatistics(@Body StatisticsRequest request);

    @PUT("statistics/{id}")
    Call<StatisticsResponse> updateStatistics(@Path("id") long id, @Body StatisticsRequest request);

    @DELETE("statistics/{id}")
    Call<Void> deleteStatistics(@Path("id") long id);

    // === Статистика тренировок ===

    /**
     * Общая сводка статистики.
     * GET /api/stats/summary
     */
    @GET("stats/summary")
    Call<StatsSummaryResponse> getStatsSummary();

    /**
     * Статистика по группам мышц.
     * GET /api/stats/muscles?startDate=...&endDate=...
     */
    @GET("stats/muscles")
    Call<List<MuscleStatsResponse>> getMuscleStats(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    /**
     * Статистика по упражнениям.
     * GET /api/stats/exercises?startDate=...&endDate=...
     */
    @GET("stats/exercises")
    Call<List<ExerciseStatsResponse>> getExerciseStats(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    /**
     * Месячная статистика объёма.
     * GET /api/stats/volume/monthly?year=...
     */
    @GET("stats/volume/monthly")
    Call<List<MonthlyVolumeResponse>> getMonthlyVolumeStats(
            @Query("year") Integer year
    );

    /**
     * Статистика тренировок.
     * GET /api/stats/workouts?startDate=...&endDate=...
     */
    @GET("stats/workouts")
    Call<WorkoutStatsResponse> getWorkoutStats(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    /** 
     * Прогресс упражнения. 
     * GET /api/stats/exercises/{exerciseId}/progress?periodType=...&year=... 
     * periodType: DAILY, WEEKLY, MONTHLY 
     */ 
    @GET("stats/exercises/{exerciseId}/progress") 
    Call<List<ExerciseProgressResponse>> getExerciseProgress( 
            @Path("exerciseId") Long exerciseId, 
            @Query("periodType") String periodType, 
            @Query("year") Integer year 
    );

    // === Новые эндпоинты для расширенной статистики ===

    /**
     * Время выполнения по неделям.
     * GET /api/stats/duration/weekly?year=...
     */
    @GET("stats/duration/weekly")
    Call<List<DurationWeeklyStats>> getDurationWeeklyStats(@Query("year") Integer year);

    /**
     * Время выполнения по месяцам.
     * GET /api/stats/duration/monthly?year=...
     */
    @GET("stats/duration/monthly")
    Call<List<DurationMonthlyStats>> getDurationMonthlyStats(@Query("year") Integer year);

    /**
     * Количество подходов по группам мышц.
     * GET /api/stats/sets/muscles?startDate=...&endDate=...
     */
    @GET("stats/sets/muscles")
    Call<List<MuscleSetsStats>> getMuscleSetsStats(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );

    /**
     * Тренировки по неделям.
     * GET /api/stats/workouts/weekly?year=...
     */
    @GET("stats/workouts/weekly")
    Call<List<WorkoutWeeklyStats>> getWorkoutWeeklyStats(@Query("year") Integer year);

    /**
     * Тренировки по месяцам.
     * GET /api/stats/workouts/monthly?year=...
     */
    @GET("stats/workouts/monthly")
    Call<List<WorkoutMonthlyStats>> getWorkoutMonthlyStats(@Query("year") Integer year);

    // === Измерения тела ===

    /**
     * Получить все измерения тела.
     * GET /api/body-measurements?startDate=...&endDate=...&type=...
     */
    @GET("body-measurements")
    Call<List<BodyMeasurementResponse>> getBodyMeasurements(
            @Query("startDate") String startDate,
            @Query("endDate") String endDate,
            @Query("type") String type
    );

    /**
     * Получить измерения по типу.
     * GET /api/body-measurements/type/{type}
     */
    @GET("body-measurements/type/{type}")
    Call<List<BodyMeasurementResponse>> getBodyMeasurementsByType(@Path("type") String type);

    /**
     * Получить последнее измерение по типу.
     * GET /api/body-measurements/type/{type}/latest
     */
    @GET("body-measurements/type/{type}/latest")
    Call<BodyMeasurementResponse> getLatestBodyMeasurement(@Path("type") String type);

    /**
     * Создать измерение.
     * POST /api/body-measurements
     */
    @POST("body-measurements")
    Call<BodyMeasurementResponse> createBodyMeasurement(@Body BodyMeasurementResponse request);

    /**
     * Обновить измерение.
     * PUT /api/body-measurements/{id}
     */
    @PUT("body-measurements/{id}")
    Call<BodyMeasurementResponse> updateBodyMeasurement(
            @Path("id") Long id,
            @Body BodyMeasurementResponse request
    );

    /**
     * Удалить измерение.
     * DELETE /api/body-measurements/{id}
     */
    @DELETE("body-measurements/{id}")
    Call<Void> deleteBodyMeasurement(@Path("id") Long id);

    /**
     * Прогресс измерения тела.
     * GET /api/body-measurements/progress?type=...&startDate=...&endDate=...
     */
    @GET("body-measurements/progress")
    Call<BodyMeasurementProgress> getBodyMeasurementProgress(
            @Query("type") String type,
            @Query("startDate") String startDate,
            @Query("endDate") String endDate
    );
}
