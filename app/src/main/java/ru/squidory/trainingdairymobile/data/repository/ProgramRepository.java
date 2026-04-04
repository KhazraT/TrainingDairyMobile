package ru.squidory.trainingdairymobile.data.repository;

import android.util.Log;
import java.io.IOException;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.squidory.trainingdairymobile.data.model.ProgramRequest;
import ru.squidory.trainingdairymobile.data.model.ProgramResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutRequest;
import ru.squidory.trainingdairymobile.data.model.WorkoutResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseRequest;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.PlannedSetRequest;
import ru.squidory.trainingdairymobile.data.model.PlannedSetResponse;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;
import ru.squidory.trainingdairymobile.data.remote.api.ProgramApi;

/**
 * Репозиторий для работы с тренировочными программами.
 */
public class ProgramRepository {

    private static final String TAG = "ProgramRepository";
    private static ProgramRepository instance;

    private final ProgramApi programApi;

    private ProgramRepository() {
        this.programApi = NetworkClient.getProgramApi();
    }

    public static synchronized ProgramRepository getInstance() {
        if (instance == null) {
            instance = new ProgramRepository();
        }
        return instance;
    }

    public interface ProgramsCallback {
        void onSuccess(List<ProgramResponse> programs);
        void onError(String error);
    }

    public interface ProgramCallback {
        void onSuccess(ProgramResponse program);
        void onError(String error);
    }

    public interface WorkoutsCallback {
        void onSuccess(List<WorkoutResponse> workouts);
        void onError(String error);
    }

    public interface WorkoutCallback {
        void onSuccess(WorkoutResponse workout);
        void onError(String error);
    }

    public interface WorkoutExercisesCallback {
        void onSuccess(List<WorkoutExerciseResponse> exercises);
        void onError(String error);
    }

    public interface WorkoutExerciseCallback {
        void onSuccess(WorkoutExerciseResponse exercise);
        void onError(String error);
    }

    public interface PlannedSetsCallback {
        void onSuccess(List<PlannedSetResponse> sets);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    // ==================== Программы ====================

    public void getPrograms(ProgramsCallback callback) {
        programApi.getPrograms().enqueue(new Callback<List<ProgramResponse>>() {
            @Override
            public void onResponse(Call<List<ProgramResponse>> call, Response<List<ProgramResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Loaded " + response.body().size() + " programs");
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<ProgramResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getProgramById(long id, ProgramCallback callback) {
        programApi.getProgramById(id).enqueue(new Callback<ProgramResponse>() {
            @Override
            public void onResponse(Call<ProgramResponse> call, Response<ProgramResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ProgramResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createProgram(ProgramRequest request, ProgramCallback callback) {
        programApi.createProgram(request).enqueue(new Callback<ProgramResponse>() {
            @Override
            public void onResponse(Call<ProgramResponse> call, Response<ProgramResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ProgramResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateProgram(long id, ProgramRequest request, ProgramCallback callback) {
        programApi.updateProgram(id, request).enqueue(new Callback<ProgramResponse>() {
            @Override
            public void onResponse(Call<ProgramResponse> call, Response<ProgramResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<ProgramResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteProgram(long id, SimpleCallback callback) {
        programApi.deleteProgram(id).enqueue(new Callback<Void>() {
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

    // ==================== Тренировки ====================

    public void getWorkoutsByProgram(long programId, WorkoutsCallback callback) {
        programApi.getWorkoutsByProgram(programId).enqueue(new Callback<List<WorkoutResponse>>() {
            @Override
            public void onResponse(Call<List<WorkoutResponse>> call, Response<List<WorkoutResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createWorkout(long programId, WorkoutRequest request, WorkoutCallback callback) {
        programApi.createWorkout(programId, request).enqueue(new Callback<WorkoutResponse>() {
            @Override
            public void onResponse(Call<WorkoutResponse> call, Response<WorkoutResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<WorkoutResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateWorkout(long id, WorkoutRequest request, WorkoutCallback callback) {
        programApi.updateWorkout(id, request).enqueue(new Callback<WorkoutResponse>() {
            @Override
            public void onResponse(Call<WorkoutResponse> call, Response<WorkoutResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<WorkoutResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteWorkout(long id, SimpleCallback callback) {
        programApi.deleteWorkout(id).enqueue(new Callback<Void>() {
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

    // ==================== Упражнения в тренировке ====================

    public void getWorkoutExercises(long workoutId, WorkoutExercisesCallback callback) {
        programApi.getWorkoutExercises(workoutId).enqueue(new Callback<List<WorkoutExerciseResponse>>() {
            @Override
            public void onResponse(Call<List<WorkoutExerciseResponse>> call, Response<List<WorkoutExerciseResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<WorkoutExerciseResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void addExerciseToWorkout(long workoutId, WorkoutExerciseRequest request, WorkoutExerciseCallback callback) {
        programApi.addExerciseToWorkout(workoutId, request).enqueue(new Callback<WorkoutExerciseResponse>() {
            @Override
            public void onResponse(Call<WorkoutExerciseResponse> call, Response<WorkoutExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<WorkoutExerciseResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateWorkoutExercise(long id, WorkoutExerciseRequest request, WorkoutExerciseCallback callback) {
        programApi.updateWorkoutExercise(id, request).enqueue(new Callback<WorkoutExerciseResponse>() {
            @Override
            public void onResponse(Call<WorkoutExerciseResponse> call, Response<WorkoutExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<WorkoutExerciseResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteWorkoutExercise(long id, SimpleCallback callback) {
        programApi.deleteWorkoutExercise(id).enqueue(new Callback<Void>() {
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

    // ==================== Планируемые подходы ====================

    public void getPlannedSets(long workoutExerciseId, PlannedSetsCallback callback) {
        programApi.getPlannedSets(workoutExerciseId).enqueue(new Callback<List<PlannedSetResponse>>() {
            @Override
            public void onResponse(Call<List<PlannedSetResponse>> call, Response<List<PlannedSetResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(getErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<List<PlannedSetResponse>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createPlannedSet(long workoutExerciseId, PlannedSetRequest request, Callback<PlannedSetResponse> callback) {
        programApi.createPlannedSet(workoutExerciseId, request).enqueue(callback);
    }

    public void updatePlannedSet(long id, PlannedSetRequest request, Callback<PlannedSetResponse> callback) {
        programApi.updatePlannedSet(id, request).enqueue(callback);
    }

    public void deletePlannedSet(long id, SimpleCallback callback) {
        programApi.deletePlannedSet(id).enqueue(new Callback<Void>() {
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
