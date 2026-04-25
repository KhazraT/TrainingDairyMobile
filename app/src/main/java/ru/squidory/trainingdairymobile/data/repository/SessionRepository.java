package ru.squidory.trainingdairymobile.data.repository;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import ru.squidory.trainingdairymobile.data.local.DatabaseClient;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;
import ru.squidory.trainingdairymobile.data.local.dao.ActiveSessionDao;
import ru.squidory.trainingdairymobile.data.local.entity.ActiveSessionEntity;
import ru.squidory.trainingdairymobile.data.model.CompleteSessionRequest;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseRequest;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.SessionHistoryResponse;
import ru.squidory.trainingdairymobile.data.model.SessionResponse;
import ru.squidory.trainingdairymobile.data.model.SessionSetResponse;
import ru.squidory.trainingdairymobile.data.model.StartSessionRequest;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;
import ru.squidory.trainingdairymobile.data.remote.api.SessionApi;

/**
 * Репозиторий для управления тренировочными сессиями.
 * Обеспечивает локальное кеширование через Room и синхронизацию с сервером.
 */
public class SessionRepository {

    private static final String TAG = "SessionRepository";
    private static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private static SessionRepository instance;
    private final SessionApi sessionApi;
    private final ActiveSessionDao activeSessionDao;
    private final Gson gson;
    private final Executor executor;
    private final Handler mainHandler;

    private SessionRepository() {
        this.sessionApi = NetworkClient.getSessionApi();
        this.activeSessionDao = DatabaseClient.getActiveSessionDao();
        this.gson = new GsonBuilder().serializeNulls().create();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized SessionRepository getInstance() {
        if (instance == null) {
            instance = new SessionRepository();
        }
        return instance;
    }

    // ==================== Callback интерфейсы ====================

    public interface SessionCallback {
        void onSuccess(SessionResponse session);
        void onError(String error);
    }

    public interface SessionsCallback {
        void onSuccess(List<SessionResponse> sessions);
        void onError(String error);
    }

    public interface SessionHistoryCallback {
        void onSuccess(SessionHistoryResponse history);
        void onError(String error);
    }

    public interface SessionExerciseCallback {
        void onSuccess(SessionExerciseResponse exercise);
        void onError(String error);
    }

    public interface SessionExercisesCallback {
        void onSuccess(List<SessionExerciseResponse> exercises);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface ActiveSessionCallback {
        void onResult(ActiveSessionEntity session);
    }

    // ==================== Начало сессии ====================

    /**
     * Начать новую сессию.
     * Вызывает API startSession, сохраняет результат в Room.
     */
    public void startSession(long workoutId, SessionCallback callback) {
        StartSessionRequest request = new StartSessionRequest(workoutId);
        Timber.d("Calling startSession with workoutId=%d", workoutId);

        sessionApi.startSession(request).enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                Timber.d("startSession response: code=%d, body=%s", response.code(), response.body() != null ? "present" : "null");

                if (response.isSuccessful() && response.body() != null) {
                    SessionResponse session = response.body();
                    Timber.d("Session started: sessionId=%d, workoutId=%d, exercises=%s",
                            session.getSessionId(), session.getWorkoutId(),
                            session.getExercises() != null ? session.getExercises().size() : "null");

                    // Сохраняем в Room
                    executor.execute(() -> {
                        try {
                            ActiveSessionEntity entity = new ActiveSessionEntity(
                                    session.getSessionId(),
                                    session.getWorkoutId(),
                                    session.getWorkoutName(),
                                    session.getWorkoutComment(),
                                    session.getStartedAt() != null ? session.getStartedAt().getTime() : System.currentTimeMillis(),
                                    STATUS_IN_PROGRESS,
                                    session.getRestTimerSeconds(),
                                    gson.toJson(session.getExercises() != null ? session.getExercises() : new ArrayList<>())
                            );
                            activeSessionDao.insertOrUpdate(entity);
                            Timber.d("Session saved to local cache: sessionId=%d", session.getSessionId());
                            mainHandler.post(() -> callback.onSuccess(session));
                        } catch (Exception e) {
                            Timber.e(e, "Failed to save session to local cache");
                            mainHandler.post(() -> callback.onSuccess(session));
                        }
                    });
                } else {
                    String error = getErrorMessage(response);
                    Timber.e("Failed to start session (code %d): %s", response.code(), error);
                    mainHandler.post(() -> callback.onError("Ошибка сервера (" + response.code() + "): " + error));
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                Timber.e(t, "Network error starting session");
                mainHandler.post(() -> callback.onError("Нет связи с сервером: " + t.getMessage()));
            }
        });
    }

    // ==================== Получение активной сессии ====================

    /**
     * Получить активную сессию из Room.
     * Возвращает null, если активной сессии нет.
     */
    public ActiveSessionEntity getActiveSession() {
        // Room операции только в background потоке
        throw new UnsupportedOperationException("Используйте getActiveSessionAsync()");
    }

    /**
     * Асинхронное получение активной сессии.
     */
    public void getActiveSessionAsync(ActiveSessionCallback callback) {
        executor.execute(() -> {
            try {
                ActiveSessionEntity session = activeSessionDao.getActiveSession();
                mainHandler.post(() -> callback.onResult(session));
            } catch (Exception e) {
                Timber.e(e, "Failed to get active session");
                mainHandler.post(() -> callback.onResult(null));
            }
        });
    }

    /**
     * Получить сессию по ID из Room.
     */
    public void getSessionByIdAsync(long sessionId, ActiveSessionCallback callback) {
        executor.execute(() -> {
            try {
                ActiveSessionEntity session = activeSessionDao.getSessionById(sessionId);
                mainHandler.post(() -> callback.onResult(session));
            } catch (Exception e) {
                Timber.e(e, "Failed to get session by id");
                mainHandler.post(() -> callback.onResult(null));
            }
        });
    }

    // ==================== Обновление подходов упражнения ====================

    /**
     * Обновить подходы упражнения в локальной сессии.
     * Изменения сохраняются локально и отправляются на сервер при завершении.
     */
    public void updateExerciseSets(long sessionId, long sessionExerciseId, List<SessionSetResponse> sets) {
        executor.execute(() -> {
            try {
                ActiveSessionEntity entity = activeSessionDao.getSessionById(sessionId);
                if (entity == null) {
                    Timber.w("Session not found: sessionId=%d", sessionId);
                    return;
                }

                // Десериализуем упражнения
                Type type = new TypeToken<List<SessionExerciseResponse>>() {}.getType();
                List<SessionExerciseResponse> exercises = gson.fromJson(entity.exercisesJson, type);
                if (exercises == null) {
                    exercises = new ArrayList<>();
                }

                // Находим упражнение и обновляем подходы
                boolean found = false;
                for (SessionExerciseResponse exercise : exercises) {
                    if (exercise.getSessionExerciseId() == sessionExerciseId) {
                        exercise.setCompletedSets(sets);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Timber.w("Exercise not found in session: sessionExerciseId=%d", sessionExerciseId);
                    return;
                }

                // Сериализуем обратно и сохраняем
                String updatedJson = gson.toJson(exercises);
                activeSessionDao.updateExercises(sessionId, updatedJson, System.currentTimeMillis());
                Timber.d("Updated sets for exercise: sessionExerciseId=%d, sets=%d", sessionExerciseId, sets.size());
            } catch (Exception e) {
                Timber.e(e, "Failed to update exercise sets");
            }
        });
    }

    // ==================== Добавление упражнения в сессию ====================

    /**
     * Добавить упражнение в сессию (локально).
     */
    public void addExerciseToSession(long sessionId, SessionExerciseResponse exercise) {
        executor.execute(() -> {
            try {
                ActiveSessionEntity entity = activeSessionDao.getSessionById(sessionId);
                if (entity == null) {
                    Timber.w("Session not found: sessionId=%d", sessionId);
                    return;
                }

                // Десериализуем упражнения
                Type type = new TypeToken<List<SessionExerciseResponse>>() {}.getType();
                List<SessionExerciseResponse> exercises = gson.fromJson(entity.exercisesJson, type);
                if (exercises == null) {
                    exercises = new ArrayList<>();
                }

                // Добавляем новое упражнение
                exercises.add(exercise);

                // Сериализуем и сохраняем
                String updatedJson = gson.toJson(exercises);
                activeSessionDao.updateExercises(sessionId, updatedJson, System.currentTimeMillis());
                Timber.d("Added exercise to session: sessionId=%d, exerciseId=%d", sessionId, exercise.getExerciseId());
            } catch (Exception e) {
                Timber.e(e, "Failed to add exercise to session");
            }
        });
    }

    /**
     * Добавить упражнение в сессию через API.
     */
    public void addExerciseToSessionApi(long sessionId, SessionExerciseRequest request, SessionExerciseCallback callback) {
        sessionApi.addExerciseToSession(sessionId, request).enqueue(new Callback<SessionExerciseResponse>() {
            @Override
            public void onResponse(Call<SessionExerciseResponse> call, Response<SessionExerciseResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SessionExerciseResponse exercise = response.body();
                    Timber.d("Exercise added to session via API: sessionExerciseId=%d", exercise.getSessionExerciseId());

                    // Сохраняем локально
                    executor.execute(() -> {
                        try {
                            ActiveSessionEntity entity = activeSessionDao.getSessionById(sessionId);
                            if (entity != null) {
                                Type type = new TypeToken<List<SessionExerciseResponse>>() {}.getType();
                                List<SessionExerciseResponse> exercises = gson.fromJson(entity.exercisesJson, type);
                                if (exercises == null) {
                                    exercises = new ArrayList<>();
                                }
                                exercises.add(exercise);
                                activeSessionDao.updateExercises(sessionId, gson.toJson(exercises), System.currentTimeMillis());
                            }
                        } catch (Exception e) {
                            Timber.e(e, "Failed to cache exercise locally");
                        }
                    });
                    mainHandler.post(() -> callback.onSuccess(exercise));
                } else {
                    String error = getErrorMessage(response);
                    Timber.e("Failed to add exercise: %s", error);
                    mainHandler.post(() -> callback.onError(error));
                }
            }

            @Override
            public void onFailure(Call<SessionExerciseResponse> call, Throwable t) {
                Timber.e(t, "Network error adding exercise");
                mainHandler.post(() -> callback.onError(t.getMessage()));
            }
        });
    }

    // ==================== Удаление упражнения из сессии ====================

    /**
     * Удалить упражнение из сессии (локально).
     * ID упражнения добавляется в removedExerciseIdsJson для отправки при завершении.
     */
    public void removeExercise(long sessionId, long sessionExerciseId) {
        executor.execute(() -> {
            try {
                ActiveSessionEntity entity = activeSessionDao.getSessionById(sessionId);
                if (entity == null) {
                    Timber.w("Session not found: sessionId=%d", sessionId);
                    return;
                }

                // Десериализуем упражнения
                Type type = new TypeToken<List<SessionExerciseResponse>>() {}.getType();
                List<SessionExerciseResponse> exercises = gson.fromJson(entity.exercisesJson, type);
                if (exercises == null) {
                    exercises = new ArrayList<>();
                }

                // Удаляем упражнение из списка
                exercises.removeIf(ex -> ex.getSessionExerciseId() == sessionExerciseId);

                // Сохраняем обновлённый список упражнений
                String updatedJson = gson.toJson(exercises);
                activeSessionDao.updateExercises(sessionId, updatedJson, System.currentTimeMillis());
                Timber.d("Removed exercise from session: sessionId=%d, exerciseId=%d", sessionId, sessionExerciseId);
            } catch (Exception e) {
                Timber.e(e, "Failed to remove exercise from session");
            }
        });
    }

    /**
     * Удалить упражнение из сессии через API.
     */
    public void removeExerciseApi(long sessionExerciseId, SimpleCallback callback) {
        sessionApi.deleteSessionExercise(sessionExerciseId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Timber.d("Exercise deleted via API: sessionExerciseId=%d", sessionExerciseId);
                    mainHandler.post(callback::onSuccess);
                } else {
                    String error = getErrorMessage(response);
                    Timber.e("Failed to delete exercise: %s", error);
                    mainHandler.post(() -> callback.onError(error));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Timber.e(t, "Network error deleting exercise");
                mainHandler.post(() -> callback.onError(t.getMessage()));
            }
        });
    }

    // ==================== Reorder упражнений ====================

    /**
     * Изменить порядок упражнений в сессии (локально).
     * exerciseIds — новый порядок ID упражнений.
     */
    public void reorderExercises(long sessionId, List<Long> exerciseIds) {
        executor.execute(() -> {
            try {
                ActiveSessionEntity entity = activeSessionDao.getSessionById(sessionId);
                if (entity == null) {
                    Timber.w("Session not found: sessionId=%d", sessionId);
                    return;
                }

                // Десериализуем упражнения
                Type type = new TypeToken<List<SessionExerciseResponse>>() {}.getType();
                List<SessionExerciseResponse> exercises = gson.fromJson(entity.exercisesJson, type);
                if (exercises == null) {
                    exercises = new ArrayList<>();
                }

                // Перестраиваем список в новом порядке
                List<SessionExerciseResponse> reordered = new ArrayList<>();
                for (Long id : exerciseIds) {
                    for (SessionExerciseResponse ex : exercises) {
                        if (ex.getSessionExerciseId().equals(id)) {
                            reordered.add(ex);
                            break;
                        }
                    }
                }

                // Обновляем order у каждого упражнения
                for (int i = 0; i < reordered.size(); i++) {
                    reordered.get(i).setExerciseOrder(i + 1);
                }

                // Сохраняем
                String updatedJson = gson.toJson(reordered);
                activeSessionDao.updateExercises(sessionId, updatedJson, System.currentTimeMillis());
                Timber.d("Reordered exercises in session: sessionId=%d, count=%d", sessionId, exerciseIds.size());
            } catch (Exception e) {
                Timber.e(e, "Failed to reorder exercises");
            }
        });
    }

    /**
     * Изменить порядок упражнений через API.
     */
    public void reorderExercisesApi(long sessionId, List<Long> exerciseIds, SimpleCallback callback) {
        sessionApi.reorderSessionExercises(sessionId, exerciseIds).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Timber.d("Exercises reordered via API: sessionId=%d", sessionId);
                    mainHandler.post(callback::onSuccess);
                } else {
                    String error = getErrorMessage(response);
                    Timber.e("Failed to reorder exercises: %s", error);
                    mainHandler.post(() -> callback.onError(error));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Timber.e(t, "Network error reordering exercises");
                mainHandler.post(() -> callback.onError(t.getMessage()));
            }
        });
    }

    // ==================== Завершение сессии ====================

    /**
     * Завершить тренировку — отправляет ВСЕ данные одним запросом.
     * Сервер создаёт сессию на лету.
     */
    public void completeSession(long workoutId, long startedAt, CompleteSessionRequest request, SessionCallback callback) {
        sessionApi.completeSession(request).enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SessionResponse session = response.body();
                    Timber.d("Session completed: sessionId=%d", session.getSessionId());
                    mainHandler.post(() -> callback.onSuccess(session));
                } else {
                    String error = getErrorMessage(response);
                    Timber.e("Failed to complete session (code %d): %s", response.code(), error);
                    mainHandler.post(() -> callback.onError("Ошибка сервера (" + response.code() + "): " + error));
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                Timber.e(t, "Network error completing session");
                mainHandler.post(() -> callback.onError("Нет связи с сервером: " + t.getMessage()));
            }
        });
    }

    // ==================== Удаление активной сессии ====================

    /**
     * Удалить активную сессию из Room.
     */
    public void deleteActiveSession(long sessionId) {
        executor.execute(() -> {
            try {
                activeSessionDao.deleteSession(sessionId);
                Timber.d("Active session deleted from local cache: sessionId=%d", sessionId);
            } catch (Exception e) {
                Timber.e(e, "Failed to delete active session");
            }
        });
    }

    /**
     * Удалить активную сессию через API.
     */
    public void deleteSessionApi(long sessionId, SimpleCallback callback) {
        sessionApi.deleteSession(sessionId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Timber.d("Session deleted via API: sessionId=%d", sessionId);
                    // Удаляем и локально
                    executor.execute(() -> {
                        try {
                            activeSessionDao.deleteSession(sessionId);
                        } catch (Exception e) {
                            Timber.e(e, "Failed to delete session locally");
                        }
                    });
                    mainHandler.post(callback::onSuccess);
                } else {
                    String error = getErrorMessage(response);
                    Timber.e("Failed to delete session: %s", error);
                    mainHandler.post(() -> callback.onError(error));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Timber.e(t, "Network error deleting session");
                mainHandler.post(() -> callback.onError(t.getMessage()));
            }
        });
    }

    // ==================== Получение сессии по ID ====================

    /**
     * Получить сессию по ID.
     */
    public void getSessionById(long sessionId, SessionCallback callback) {
        sessionApi.getSessionById(sessionId).enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> callback.onSuccess(response.body()));
                } else {
                    String error = getErrorMessage(response);
                    Timber.e("Failed to get session by id: %s", error);
                    mainHandler.post(() -> callback.onError("Ошибка сервера (" + response.code() + "): " + error));
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                Timber.e(t, "Network error getting session by id");
                mainHandler.post(() -> callback.onError("Нет связи с сервером: " + t.getMessage()));
            }
        });
    }

    // ==================== История сессий ====================

    /**
     * Получить историю сессий пользователя, сгруппированную по дням.
     */
    public void getSessionHistory(Integer year, Integer month, Integer page, Integer size, SessionHistoryCallback callback) {
        sessionApi.getSessionHistory(year, month, page, size).enqueue(new Callback<SessionHistoryResponse>() {
            @Override
            public void onResponse(Call<SessionHistoryResponse> call, Response<SessionHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> callback.onSuccess(response.body()));
                } else {
                    String error = getErrorMessage(response);
                    Timber.e("Failed to get session history: %s", error);
                    mainHandler.post(() -> callback.onError("Ошибка сервера (" + response.code() + "): " + error));
                }
            }

            @Override
            public void onFailure(Call<SessionHistoryResponse> call, Throwable t) {
                Timber.e(t, "Network error getting session history");
                mainHandler.post(() -> callback.onError("Нет связи с сервером: " + t.getMessage()));
            }
        });
    }

    // ==================== Синхронизация ====================

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
