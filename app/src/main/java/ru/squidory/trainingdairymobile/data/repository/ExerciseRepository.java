package ru.squidory.trainingdairymobile.data.repository;

import android.util.Log;
import java.io.IOException;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.squidory.trainingdairymobile.data.model.ExerciseRequest;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.EquipmentResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleGroupResponse;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;
import ru.squidory.trainingdairymobile.data.remote.api.ExerciseApi;

/**
 * Репозиторий для работы с упражнениями.
 */
public class ExerciseRepository {

    private static final String TAG = "ExerciseRepository";
    private static ExerciseRepository instance;

    private final ExerciseApi exerciseApi;

    private ExerciseRepository() {
        this.exerciseApi = NetworkClient.getExerciseApi();
    }

    public static synchronized ExerciseRepository getInstance() {
        if (instance == null) {
            instance = new ExerciseRepository();
        }
        return instance;
    }

    public interface ExercisesCallback {
        void onSuccess(List<ExerciseResponse> exercises);
        void onError(String error);
    }

    public interface ExerciseCallback {
        void onSuccess(ExerciseResponse exercise);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface MuscleGroupsCallback {
        void onSuccess(List<MuscleGroupResponse> muscleGroups);
        void onError(String error);
    }

    public interface EquipmentCallback {
        void onSuccess(List<EquipmentResponse> equipment);
        void onError(String error);
    }

    // ==================== Группы мышц ====================

    public void getMuscleGroups(MuscleGroupsCallback callback) {
        exerciseApi.getMuscleGroups().enqueue(new Callback<List<MuscleGroupResponse>>() {
            @Override
            public void onResponse(Call<List<MuscleGroupResponse>> call, Response<List<MuscleGroupResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<MuscleGroupResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // ==================== Оборудование ====================

    public void getEquipment(EquipmentCallback callback) {
        exerciseApi.getEquipment().enqueue(new Callback<List<EquipmentResponse>>() {
            @Override
            public void onResponse(Call<List<EquipmentResponse>> call, Response<List<EquipmentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<EquipmentResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Получить все упражнения с фильтрацией.
     */
    public void getExercises(String muscle, String equipment, String type, ExercisesCallback callback) {
        // Загружаем упражнения из реального API
        exerciseApi.getExercises(muscle, equipment, type).enqueue(new Callback<List<ExerciseResponse>>() {
            @Override
            public void onResponse(Call<List<ExerciseResponse>> call, Response<List<ExerciseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Loaded " + response.body().size() + " exercises from API");
                    callback.onSuccess(response.body());
                } else {
                    String error = getErrorMessage(response);
                    Log.e(TAG, "Failed to load exercises: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<List<ExerciseResponse>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Получить упражнение по ID.
     */
    public void getExerciseById(long id, ExerciseCallback callback) {
        exerciseApi.getExerciseById(id).enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Создать новое упражнение.
     */
    public void createExercise(ExerciseRequest request, ExerciseCallback callback) {
        exerciseApi.createExercise(request).enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Обновить упражнение.
     */
    public void updateExercise(long id, ExerciseRequest request, ExerciseCallback callback) {
        exerciseApi.updateExercise(id, request).enqueue(new Callback<ExerciseResponse>() {
            @Override
            public void onResponse(Call<ExerciseResponse> call, Response<ExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ExerciseResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Удалить упражнение.
     */
    public void deleteExercise(long id, SimpleCallback callback) {
        exerciseApi.deleteExercise(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Получить список групп мышц.
     */
    public void getMuscleGroups(ExercisesCallback callback) {
        // Заглушка - будет реализовано при необходимости
    }

    /**
     * Получить список оборудования.
     */
    public void getEquipment(ExercisesCallback callback) {
        // Заглушка - будет реализовано при необходимости
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
}
