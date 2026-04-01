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
import ru.squidory.trainingdairymobile.data.model.StatisticsRequest;
import ru.squidory.trainingdairymobile.data.model.StatisticsResponse;
import ru.squidory.trainingdairymobile.data.model.VolumeStatsResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutStatsResponse;

public interface StatisticsApi {

    // Статистика тела
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

    // Статистика тренировок
    @GET("stats/summary")
    Call<VolumeStatsResponse> getStatsSummary();

    @GET("stats/muscles")
    Call<List<VolumeStatsResponse>> getStatsByMuscles();

    @GET("stats/exercises")
    Call<List<VolumeStatsResponse>> getStatsByExercises();

    @GET("stats/exercises/{exerciseId}/progress")
    Call<List<VolumeStatsResponse>> getExerciseProgress(@Path("exerciseId") long exerciseId);

    @GET("stats/volume/monthly")
    Call<List<VolumeStatsResponse>> getMonthlyVolumeStats();

    @GET("stats/volume/yearly")
    Call<List<VolumeStatsResponse>> getYearlyVolumeStats();

    @GET("stats/workouts")
    Call<WorkoutStatsResponse> getWorkoutStats();

    @GET("stats/workouts/detail")
    Call<List<WorkoutStatsResponse>> getWorkoutStatsDetail();
}
