package ru.squidory.trainingdairymobile.data.repository;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;
import ru.squidory.trainingdairymobile.data.model.UserResponse;
import ru.squidory.trainingdairymobile.data.model.UserSettingsRequest;
import ru.squidory.trainingdairymobile.data.model.UserSettingsResponse;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;
import ru.squidory.trainingdairymobile.data.remote.api.UserApi;

/**
 * Репозиторий для работы с профилем пользователя и настройками.
 */
public class UserRepository {

    private static final String TAG = "UserRepository";
    private static UserRepository instance;

    private final UserApi userApi;
    private final Executor executor;
    private final Handler mainHandler;

    private UserRepository() {
        this.userApi = NetworkClient.getUserApi();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    // ==================== Callback интерфейсы ====================

    public interface GetUserCallback {
        void onSuccess(UserResponse user);
        void onError(String error);
    }

    public interface UpdateUserCallback {
        void onSuccess(UserResponse user);
        void onError(String error);
    }

    public interface GetSettingsCallback {
        void onSuccess(UserSettingsResponse settings);
        void onError(String error);
    }

    public interface UpdateSettingsCallback {
        void onSuccess(UserSettingsResponse settings);
        void onError(String error);
    }

    public interface DeleteUserCallback {
        void onSuccess();
        void onError(String error);
    }

    // ==================== Методы API ====================

    /**
     * Получить данные текущего пользователя
     */
    public void getCurrentUser(GetUserCallback callback) {
        executor.execute(() -> {
            try {
                Call<UserResponse> call = userApi.getCurrentUser();
                Response<UserResponse> response = call.execute();

                mainHandler.post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        Timber.d("getCurrentUser success: %s", response.body().getEmail());
                        callback.onSuccess(response.body());
                    } else {
                        String error = "Ошибка загрузки профиля: " + response.code();
                        Timber.e("getCurrentUser failed: %s", error);
                        callback.onError(error);
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    Timber.e(e, "getCurrentUser network error");
                    callback.onError("Ошибка сети: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Обновить данные пользователя (имя, дата рождения, пол)
     */
    public void updateCurrentUser(UserResponse request, UpdateUserCallback callback) {
        executor.execute(() -> {
            try {
                Call<UserResponse> call = userApi.updateCurrentUser(request);
                Response<UserResponse> response = call.execute();

                mainHandler.post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        Timber.d("updateCurrentUser success: %s", response.body().getName());
                        callback.onSuccess(response.body());
                    } else {
                        String error = "Ошибка обновления профиля: " + response.code();
                        Timber.e("updateCurrentUser failed: %s", error);
                        callback.onError(error);
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    Timber.e(e, "updateCurrentUser network error");
                    callback.onError("Ошибка сети: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Удалить аккаунт пользователя
     */
    public void deleteCurrentUser(DeleteUserCallback callback) {
        executor.execute(() -> {
            try {
                Call<Void> call = userApi.deleteCurrentUser();
                Response<Void> response = call.execute();

                mainHandler.post(() -> {
                    if (response.isSuccessful()) {
                        Timber.d("deleteCurrentUser success");
                        callback.onSuccess();
                    } else {
                        String error = "Ошибка удаления аккаунта: " + response.code();
                        Timber.e("deleteCurrentUser failed: %s", error);
                        callback.onError(error);
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    Timber.e(e, "deleteCurrentUser network error");
                    callback.onError("Ошибка сети: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Получить настройки пользователя
     */
    public void getUserSettings(GetSettingsCallback callback) {
        executor.execute(() -> {
            try {
                Call<UserSettingsResponse> call = userApi.getUserSettings();
                Response<UserSettingsResponse> response = call.execute();

                mainHandler.post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        Timber.d("getUserSettings success");
                        callback.onSuccess(response.body());
                    } else {
                        String error = "Ошибка загрузки настроек: " + response.code();
                        Timber.e("getUserSettings failed: %s", error);
                        callback.onError(error);
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    Timber.e(e, "getUserSettings network error");
                    callback.onError("Ошибка сети: " + e.getMessage());
                });
            }
        });
    }

    /**
     * Обновить настройки пользователя
     */
    public void updateUserSettings(UserSettingsRequest request, UpdateSettingsCallback callback) {
        executor.execute(() -> {
            try {
                Call<UserSettingsResponse> call = userApi.updateUserSettings(request);
                Response<UserSettingsResponse> response = call.execute();

                mainHandler.post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        Timber.d("updateUserSettings success");
                        callback.onSuccess(response.body());
                    } else {
                        String error = "Ошибка обновления настроек: " + response.code();
                        Timber.e("updateUserSettings failed: %s", error);
                        callback.onError(error);
                    }
                });
            } catch (IOException e) {
                mainHandler.post(() -> {
                    Timber.e(e, "updateUserSettings network error");
                    callback.onError("Ошибка сети: " + e.getMessage());
                });
            }
        });
    }
}
