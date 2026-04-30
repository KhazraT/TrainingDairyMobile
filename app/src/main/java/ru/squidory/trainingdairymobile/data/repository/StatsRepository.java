package ru.squidory.trainingdairymobile.data.repository;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import ru.squidory.trainingdairymobile.data.model.BodyMeasurementProgress;
import ru.squidory.trainingdairymobile.data.model.BodyMeasurementResponse;
import ru.squidory.trainingdairymobile.data.model.DurationDailyStats;
import ru.squidory.trainingdairymobile.data.model.DurationMonthlyStats;
import ru.squidory.trainingdairymobile.data.model.DurationWeeklyStats;
import ru.squidory.trainingdairymobile.data.model.ExerciseProgressResponse;
import ru.squidory.trainingdairymobile.data.model.ExerciseStatsResponse;
import ru.squidory.trainingdairymobile.data.model.MonthlyVolumeResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleSetsStats;
import ru.squidory.trainingdairymobile.data.model.MuscleStatsResponse;
import ru.squidory.trainingdairymobile.data.model.StatsSummaryResponse;
import ru.squidory.trainingdairymobile.data.model.VolumeDailyResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutDailyStats;
import ru.squidory.trainingdairymobile.data.model.WorkoutMonthlyStats;
import ru.squidory.trainingdairymobile.data.model.WorkoutStatsResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutWeeklyStats;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;
import ru.squidory.trainingdairymobile.data.remote.api.StatisticsApi;

/**
 * Репозиторий для получения статистики.
 */
public class StatsRepository {

    private static final String TAG = "StatsRepository";
    private static StatsRepository instance;

    private final StatisticsApi statisticsApi;
    private final Executor executor;
    private final Handler mainHandler;

    private StatsRepository() {
        this.statisticsApi = NetworkClient.getStatisticsApi();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized StatsRepository getInstance() {
        if (instance == null) {
            instance = new StatsRepository();
        }
        return instance;
    }

    // ==================== Callback интерфейсы ====================

    public interface StatsSummaryCallback {
        void onSuccess(StatsSummaryResponse summary);
        void onError(String error);
    }

    public interface MuscleStatsCallback {
        void onSuccess(List<MuscleStatsResponse> muscles);
        void onError(String error);
    }

    public interface ExerciseStatsCallback {
        void onSuccess(List<ExerciseStatsResponse> exercises);
        void onError(String error);
    }

    public interface MonthlyVolumeCallback {
        void onSuccess(List<MonthlyVolumeResponse> volumes);
        void onError(String error);
    }

    public interface WorkoutStatsCallback {
        void onSuccess(WorkoutStatsResponse stats);
        void onError(String error);
    }

    public interface ExerciseProgressCallback {
        void onSuccess(List<ExerciseProgressResponse> progress);
        void onError(String error);
    }

    // === Новые callback интерфейсы ===

    public interface DurationWeeklyCallback {
        void onSuccess(List<DurationWeeklyStats> stats);
        void onError(String error);
    }

    public interface DurationMonthlyCallback {
        void onSuccess(List<DurationMonthlyStats> stats);
        void onError(String error);
    }

    public interface MuscleSetsCallback {
        void onSuccess(List<MuscleSetsStats> stats);
        void onError(String error);
    }

    public interface WorkoutWeeklyCallback {
        void onSuccess(List<WorkoutWeeklyStats> stats);
        void onError(String error);
    }

    public interface WorkoutMonthlyCallback {
        void onSuccess(List<WorkoutMonthlyStats> stats);
        void onError(String error);
    }

    // === Daily callbacks ===

    public interface VolumeDailyCallback {
        void onSuccess(List<VolumeDailyResponse> dailyVolumes);
        void onError(String error);
    }

    public interface DurationDailyCallback {
        void onSuccess(List<DurationDailyStats> dailyDurations);
        void onError(String error);
    }

    public interface WorkoutDailyCallback {
        void onSuccess(List<WorkoutDailyStats> dailyWorkouts);
        void onError(String error);
    }

    public interface BodyMeasurementsCallback {
        void onSuccess(List<BodyMeasurementResponse> measurements);
        void onError(String error);
    }

    public interface BodyMeasurementCallback {
        void onSuccess(BodyMeasurementResponse measurement);
        void onError(String error);
    }

    public interface BodyMeasurementProgressCallback {
        void onSuccess(BodyMeasurementProgress progress);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    // ==================== Методы API ====================

    /** Получить общую сводку статистики. */
    public void getStatsSummary(StatsSummaryCallback callback) {
        statisticsApi.getStatsSummary().enqueue(new Callback<StatsSummaryResponse>() {
            @Override
            public void onResponse(Call<StatsSummaryResponse> call, Response<StatsSummaryResponse> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "stats summary");
            }
            @Override
            public void onFailure(Call<StatsSummaryResponse> call, Throwable t) {
                handleFailure(t, callback::onError, "stats summary");
            }
        });
    }

    /** Получить статистику по группам мышц. */
    public void getMuscleStats(String startDate, String endDate, MuscleStatsCallback callback) {
        statisticsApi.getMuscleStats(startDate, endDate).enqueue(new Callback<List<MuscleStatsResponse>>() {
            @Override
            public void onResponse(Call<List<MuscleStatsResponse>> call, Response<List<MuscleStatsResponse>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "muscle stats");
            }
            @Override
            public void onFailure(Call<List<MuscleStatsResponse>> call, Throwable t) {
                handleFailure(t, callback::onError, "muscle stats");
            }
        });
    }

    /** Получить статистику по упражнениям. */
    public void getExerciseStats(String startDate, String endDate, ExerciseStatsCallback callback) {
        statisticsApi.getExerciseStats(startDate, endDate).enqueue(new Callback<List<ExerciseStatsResponse>>() {
            @Override
            public void onResponse(Call<List<ExerciseStatsResponse>> call, Response<List<ExerciseStatsResponse>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "exercise stats");
            }
            @Override
            public void onFailure(Call<List<ExerciseStatsResponse>> call, Throwable t) {
                handleFailure(t, callback::onError, "exercise stats");
            }
        });
    }

    /** Получить месячную статистику объёма. */
    public void getMonthlyVolume(int year, MonthlyVolumeCallback callback) {
        statisticsApi.getMonthlyVolumeStats(year).enqueue(new Callback<List<MonthlyVolumeResponse>>() {
            @Override
            public void onResponse(Call<List<MonthlyVolumeResponse>> call, Response<List<MonthlyVolumeResponse>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "monthly volume");
            }
            @Override
            public void onFailure(Call<List<MonthlyVolumeResponse>> call, Throwable t) {
                handleFailure(t, callback::onError, "monthly volume");
            }
        });
    }

    /** Получить статистику тренировок. */
    public void getWorkoutStats(String startDate, String endDate, WorkoutStatsCallback callback) {
        statisticsApi.getWorkoutStats(startDate, endDate).enqueue(new Callback<WorkoutStatsResponse>() {
            @Override
            public void onResponse(Call<WorkoutStatsResponse> call, Response<WorkoutStatsResponse> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "workout stats");
            }
            @Override
            public void onFailure(Call<WorkoutStatsResponse> call, Throwable t) {
                handleFailure(t, callback::onError, "workout stats");
            }
        });
    }

    /** Получить прогресс упражнения. */
    public void getExerciseProgress(long exerciseId, String periodType, int year, ExerciseProgressCallback callback) {
        statisticsApi.getExerciseProgress(exerciseId, periodType, year).enqueue(new Callback<List<ExerciseProgressResponse>>() {
            @Override
            public void onResponse(Call<List<ExerciseProgressResponse>> call, Response<List<ExerciseProgressResponse>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "exercise progress");
            }
            @Override
            public void onFailure(Call<List<ExerciseProgressResponse>> call, Throwable t) {
                handleFailure(t, callback::onError, "exercise progress");
            }
        });
    }

    // === Новые методы для расширенной статистики ===

    /** Время выполнения по неделям. */
    public void getDurationWeeklyStats(int year, DurationWeeklyCallback callback) {
        statisticsApi.getDurationWeeklyStats(year).enqueue(new Callback<List<DurationWeeklyStats>>() {
            @Override
            public void onResponse(Call<List<DurationWeeklyStats>> call, Response<List<DurationWeeklyStats>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "duration weekly");
            }
            @Override
            public void onFailure(Call<List<DurationWeeklyStats>> call, Throwable t) {
                handleFailure(t, callback::onError, "duration weekly");
            }
        });
    }

    /** Время выполнения по месяцам. */
    public void getDurationMonthlyStats(int year, DurationMonthlyCallback callback) {
        statisticsApi.getDurationMonthlyStats(year).enqueue(new Callback<List<DurationMonthlyStats>>() {
            @Override
            public void onResponse(Call<List<DurationMonthlyStats>> call, Response<List<DurationMonthlyStats>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "duration monthly");
            }
            @Override
            public void onFailure(Call<List<DurationMonthlyStats>> call, Throwable t) {
                handleFailure(t, callback::onError, "duration monthly");
            }
        });
    }

    /** Количество подходов по группам мышц. */
    public void getMuscleSetsStats(String startDate, String endDate, MuscleSetsCallback callback) {
        statisticsApi.getMuscleSetsStats(startDate, endDate).enqueue(new Callback<List<MuscleSetsStats>>() {
            @Override
            public void onResponse(Call<List<MuscleSetsStats>> call, Response<List<MuscleSetsStats>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "muscle sets");
            }
            @Override
            public void onFailure(Call<List<MuscleSetsStats>> call, Throwable t) {
                handleFailure(t, callback::onError, "muscle sets");
            }
        });
    }

    /** Тренировки по неделям. */
    public void getWorkoutWeeklyStats(int year, WorkoutWeeklyCallback callback) {
        statisticsApi.getWorkoutWeeklyStats(year).enqueue(new Callback<List<WorkoutWeeklyStats>>() {
            @Override
            public void onResponse(Call<List<WorkoutWeeklyStats>> call, Response<List<WorkoutWeeklyStats>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "workout weekly");
            }
            @Override
            public void onFailure(Call<List<WorkoutWeeklyStats>> call, Throwable t) {
                handleFailure(t, callback::onError, "workout weekly");
            }
        });
    }

    /** Тренировки по месяцам. */
    public void getWorkoutMonthlyStats(int year, WorkoutMonthlyCallback callback) {
        statisticsApi.getWorkoutMonthlyStats(year).enqueue(new Callback<List<WorkoutMonthlyStats>>() {
            @Override
            public void onResponse(Call<List<WorkoutMonthlyStats>> call, Response<List<WorkoutMonthlyStats>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "workout monthly");
            }
            @Override
            public void onFailure(Call<List<WorkoutMonthlyStats>> call, Throwable t) {
                handleFailure(t, callback::onError, "workout monthly");
            }
        });
    }

    // === Daily methods ===

    /** Ежедневный тоннаж. */
    public void getDailyVolume(String startDate, String endDate, VolumeDailyCallback callback) {
        statisticsApi.getDailyVolume(startDate, endDate).enqueue(new Callback<List<VolumeDailyResponse>>() {
            @Override
            public void onResponse(Call<List<VolumeDailyResponse>> call, Response<List<VolumeDailyResponse>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "daily volume");
            }
            @Override
            public void onFailure(Call<List<VolumeDailyResponse>> call, Throwable t) {
                handleFailure(t, callback::onError, "daily volume");
            }
        });
    }

    /** Ежедневная длительность. */
    public void getDailyDuration(String startDate, String endDate, DurationDailyCallback callback) {
        statisticsApi.getDailyDuration(startDate, endDate).enqueue(new Callback<List<DurationDailyStats>>() {
            @Override
            public void onResponse(Call<List<DurationDailyStats>> call, Response<List<DurationDailyStats>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "daily duration");
            }
            @Override
            public void onFailure(Call<List<DurationDailyStats>> call, Throwable t) {
                handleFailure(t, callback::onError, "daily duration");
            }
        });
    }

    /** Ежедневные тренировки. */
    public void getDailyWorkouts(String startDate, String endDate, WorkoutDailyCallback callback) {
        statisticsApi.getDailyWorkouts(startDate, endDate).enqueue(new Callback<List<WorkoutDailyStats>>() {
            @Override
            public void onResponse(Call<List<WorkoutDailyStats>> call, Response<List<WorkoutDailyStats>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "daily workouts");
            }
            @Override
            public void onFailure(Call<List<WorkoutDailyStats>> call, Throwable t) {
                handleFailure(t, callback::onError, "daily workouts");
            }
        });
    }

    // === Методы для измерений тела ===

    /** Получить все измерения тела. */
    public void getBodyMeasurements(String startDate, String endDate, String type, BodyMeasurementsCallback callback) {
        statisticsApi.getBodyMeasurements(startDate, endDate, type).enqueue(new Callback<List<BodyMeasurementResponse>>() {
            @Override
            public void onResponse(Call<List<BodyMeasurementResponse>> call, Response<List<BodyMeasurementResponse>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "body measurements");
            }
            @Override
            public void onFailure(Call<List<BodyMeasurementResponse>> call, Throwable t) {
                handleFailure(t, callback::onError, "body measurements");
            }
        });
    }

    /** Получить измерения по типу. */
    public void getBodyMeasurementsByType(String type, BodyMeasurementsCallback callback) {
        statisticsApi.getBodyMeasurementsByType(type).enqueue(new Callback<List<BodyMeasurementResponse>>() {
            @Override
            public void onResponse(Call<List<BodyMeasurementResponse>> call, Response<List<BodyMeasurementResponse>> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "body measurements by type");
            }
            @Override
            public void onFailure(Call<List<BodyMeasurementResponse>> call, Throwable t) {
                handleFailure(t, callback::onError, "body measurements by type");
            }
        });
    }

    /** Получить последнее измерение по типу. */
    public void getLatestBodyMeasurement(String type, BodyMeasurementCallback callback) {
        statisticsApi.getLatestBodyMeasurement(type).enqueue(new Callback<BodyMeasurementResponse>() {
            @Override
            public void onResponse(Call<BodyMeasurementResponse> call, Response<BodyMeasurementResponse> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "latest body measurement");
            }
            @Override
            public void onFailure(Call<BodyMeasurementResponse> call, Throwable t) {
                handleFailure(t, callback::onError, "latest body measurement");
            }
        });
    }

    /** Создать измерение. */
    public void createBodyMeasurement(BodyMeasurementResponse request, BodyMeasurementCallback callback) {
        statisticsApi.createBodyMeasurement(request).enqueue(new Callback<BodyMeasurementResponse>() {
            @Override
            public void onResponse(Call<BodyMeasurementResponse> call, Response<BodyMeasurementResponse> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "create body measurement");
            }
            @Override
            public void onFailure(Call<BodyMeasurementResponse> call, Throwable t) {
                handleFailure(t, callback::onError, "create body measurement");
            }
        });
    }

    /** Обновить измерение. */
    public void updateBodyMeasurement(Long id, BodyMeasurementResponse request, BodyMeasurementCallback callback) {
        statisticsApi.updateBodyMeasurement(id, request).enqueue(new Callback<BodyMeasurementResponse>() {
            @Override
            public void onResponse(Call<BodyMeasurementResponse> call, Response<BodyMeasurementResponse> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "update body measurement");
            }
            @Override
            public void onFailure(Call<BodyMeasurementResponse> call, Throwable t) {
                handleFailure(t, callback::onError, "update body measurement");
            }
        });
    }

    /** Удалить измерение. */
    public void deleteBodyMeasurement(Long id, SimpleCallback callback) {
        statisticsApi.deleteBodyMeasurement(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    mainHandler.post(callback::onSuccess);
                } else {
                    String error = getErrorMessage(response);
                    Timber.e("Failed to delete body measurement: %s", error);
                    mainHandler.post(() -> callback.onError("Ошибка сервера (" + response.code() + "): " + error));
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                handleFailure(t, callback::onError, "delete body measurement");
            }
        });
    }

    /** Прогресс измерения тела. */
    public void getBodyMeasurementProgress(String type, String startDate, String endDate, BodyMeasurementProgressCallback callback) {
        statisticsApi.getBodyMeasurementProgress(type, startDate, endDate).enqueue(new Callback<BodyMeasurementProgress>() {
            @Override
            public void onResponse(Call<BodyMeasurementProgress> call, Response<BodyMeasurementProgress> response) {
                handleResponse(response, callback::onSuccess, callback::onError, "body measurement progress");
            }
            @Override
            public void onFailure(Call<BodyMeasurementProgress> call, Throwable t) {
                handleFailure(t, callback::onError, "body measurement progress");
            }
        });
    }

    // ==================== Вспомогательные методы ====================

    private <T> void handleResponse(Response<T> response, ResponseCallback<T> successCallback,
                                    ErrorCallback errorCallback, String operation) {
        if (response.isSuccessful() && response.body() != null) {
            mainHandler.post(() -> successCallback.onSuccess(response.body()));
        } else {
            String error = getErrorMessage(response);
            Timber.e("Failed to get %s: %s", operation, error);
            mainHandler.post(() -> errorCallback.onError("Ошибка сервера (" + response.code() + "): " + error));
        }
    }

    private void handleFailure(Throwable t, ErrorCallback errorCallback, String operation) {
        Timber.e(t, "Network error getting %s", operation);
        mainHandler.post(() -> errorCallback.onError("Нет связи с сервером: " + t.getMessage()));
    }

    private String getErrorMessage(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                return response.errorBody().string();
            } catch (IOException e) {
                return "Unknown error";
            }
        }
        return "Error: " + response.code();
    }

    @FunctionalInterface
    private interface ResponseCallback<T> {
        void onSuccess(T data);
    }

    @FunctionalInterface
    private interface ErrorCallback {
        void onError(String error);
    }
}
