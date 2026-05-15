package ru.squidory.trainingdairymobile.data.repository;

import android.os.Handler;
import android.os.Looper;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import ru.squidory.trainingdairymobile.data.local.PreferencesManager;
import ru.squidory.trainingdairymobile.data.model.AntiGSessionRequest;
import ru.squidory.trainingdairymobile.data.model.AntiGSessionResponse;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;
import ru.squidory.trainingdairymobile.data.remote.api.AntiGSessionApi;

/**
 * Репозиторий для управления сеансами анти-G тренировки.
 * Обеспечивает взаимодействие с backend API.
 */
public class AntiGSessionRepository {

    private static final String TAG = "AntiGSessionRepository";

    private static AntiGSessionRepository instance;
    private final AntiGSessionApi antiGSessionApi;
    private final Executor executor;
    private final Handler mainHandler;

    private AntiGSessionRepository() {
        this.antiGSessionApi = NetworkClient.getAntiGSessionApi();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized AntiGSessionRepository getInstance() {
        if (instance == null) {
            instance = new AntiGSessionRepository();
        }
        return instance;
    }

    // ==================== Callback интерфейсы ====================

    public interface AntiGSessionCallback {
        void onSuccess(AntiGSessionResponse session);
        void onError(String error);
    }

    public interface AntiGSessionListCallback {
        void onSuccess(List<AntiGSessionResponse> sessions);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }

    // ==================== Сохранение сессии ====================

    /**
     * Сохранить сессию анти-G тренировки через API.
     */
    public void saveSession(int amount, String comment, AntiGSessionCallback callback) {
        // Получаем текущий пользовательский ID из PreferencesManager
        long userId = PreferencesManager.getInstance().getUserId();

        // Создаем объект запроса
        AntiGSessionRequest request = new AntiGSessionRequest(amount, comment);

        // Выполняем запрос к API
        antiGSessionApi.createSession(request).enqueue(new Callback<AntiGSessionResponse>() {
            @Override
            public void onResponse(Call<AntiGSessionResponse> call, Response<AntiGSessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AntiGSessionResponse session = response.body();
                    // Убедимся, что userId установлен (если API не возвращает его, мы можем установить его здесь)
                    // Но предполагаем, что API возвращает сессию с userId, установленным на текущего пользователя.
                    mainHandler.post(() -> callback.onSuccess(session));
                } else {
                    StringBuilder errorBuilder = new StringBuilder("Ошибка сервера (" + response.code() + ")");
                    if (response.errorBody() != null) {
                        try {
                            errorBuilder.append(": ").append(response.errorBody().string());
                        } catch (Exception e) {
                            errorBuilder.append(": Не удалось получить тело ошибки");
                        }
                    }
                    final String error = errorBuilder.toString();
                    Timber.e("Failed to save anti-G session (code %d): %s", response.code(), error);
                    mainHandler.post(() -> callback.onError(error));
                }
            }

            @Override
            public void onFailure(Call<AntiGSessionResponse> call, Throwable t) {
                Timber.e(t, "Network error saving anti-G session");
                mainHandler.post(() -> callback.onError("Нет связи с сервером: " + t.getMessage()));
            }
        });
    }

    // ==================== Получение истории сессий ====================

    /**
     * Получить историю сессий анти-G тренировки для текущего пользователя.
     */
    public void getAllSessions(AntiGSessionListCallback callback) {
        antiGSessionApi.getAllSessions(true).enqueue(new Callback<List<AntiGSessionResponse>>() {
            @Override
            public void onResponse(Call<List<AntiGSessionResponse>> call, Response<List<AntiGSessionResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mainHandler.post(() -> callback.onSuccess(response.body()));
                } else {
                    StringBuilder errorBuilder = new StringBuilder("Ошибка сервера (" + response.code() + ")");
                    if (response.errorBody() != null) {
                        try {
                            errorBuilder.append(": ").append(response.errorBody().string());
                        } catch (Exception e) {
                            errorBuilder.append(": Не удалось получить тело ошибки");
                        }
                    }
                    final String error = errorBuilder.toString();
                    Timber.e("Failed to get anti-G sessions (code %d): %s", response.code(), error);
                    mainHandler.post(() -> callback.onError(error));
                }
            }

            @Override
            public void onFailure(Call<List<AntiGSessionResponse>> call, Throwable t) {
                Timber.e(t, "Network error getting anti-G sessions");
                mainHandler.post(() -> callback.onError("Нет связи с сервером: " + t.getMessage()));
            }
        });
    }

    // ==================== Удаление сессии ====================

    /**
     * Удалить сессию анти-G тренировки по ID.
     */
    public void deleteSession(long id, SimpleCallback callback) {
        antiGSessionApi.deleteSession(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    mainHandler.post(callback::onSuccess);
                } else {
                    StringBuilder errorBuilder = new StringBuilder("Ошибка сервера (" + response.code() + ")");
                    if (response.errorBody() != null) {
                        try {
                            errorBuilder.append(": ").append(response.errorBody().string());
                        } catch (Exception e) {
                            errorBuilder.append(": Не удалось получить тело ошибки");
                        }
                    }
                    final String error = errorBuilder.toString();
                    Timber.e("Failed to delete anti-G session (code %d): %s", response.code(), error);
                    mainHandler.post(() -> callback.onError(error));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Timber.e(t, "Network error deleting anti-G session");
                mainHandler.post(() -> callback.onError("Нет связи с сервером: " + t.getMessage()));
            }
        });
    }
}