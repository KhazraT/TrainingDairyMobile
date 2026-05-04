package ru.squidory.trainingdairymobile.data.remote;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;
import ru.squidory.trainingdairymobile.data.model.AuthResponse;
import ru.squidory.trainingdairymobile.data.model.RefreshRequest;
import ru.squidory.trainingdairymobile.data.remote.api.AuthApi;
import ru.squidory.trainingdairymobile.util.Constants;

/**
 * Authenticator для автоматического обновления JWT токена при получении 401 ошибки.
 */
public class AuthAuthenticator implements Authenticator {

    private static final String TAG = "AuthAuthenticator";
    private static final Object lock = new Object();
    private static boolean isRefreshing = false;

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        String url = response.request().url().toString();
        
        // Не пытаемся обновить токен, если это уже запрос на обновление
        if (url.contains("/auth/refresh")) {
            Log.e(TAG, "Refresh token request failed (401), clearing tokens");
            PreferencesManager.getInstance().clearTokens();
            return null;
        }

        synchronized (lock) {
            // Если уже обновляем токен, ждем завершения
            if (isRefreshing) {
                // Простой подход: возвращаем null
                // В продакшене здесь лучше использовать wait/notify
                return null;
            }
            isRefreshing = true;
        }

        try {
            PreferencesManager preferencesManager = PreferencesManager.getInstance();
            String refreshToken = preferencesManager.getRefreshToken();

            if (refreshToken == null || refreshToken.isEmpty()) {
                Log.e(TAG, "No refresh token available");
                return null;
            }

            // Создаем отдельный клиент без перехватчиков для избежания рекурсии
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            AuthApi authApi = retrofit.create(AuthApi.class);

            // Синхронный запрос для обновления токена
            retrofit2.Call<AuthResponse> call = authApi.refreshToken(new RefreshRequest(refreshToken));
            retrofit2.Response<AuthResponse> refreshResponse = call.execute();

            if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
                AuthResponse authResponse = refreshResponse.body();
                preferencesManager.saveTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());

                Log.d(TAG, "Token refreshed successfully");

                // Повторяем оригинальный запрос с новым токеном
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + authResponse.getAccessToken())
                        .build();
            } else {
                Log.e(TAG, "Token refresh failed: " + refreshResponse.code());
                preferencesManager.clearTokens();
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Token refresh exception: " + e.getMessage());
            return null;
        } finally {
            synchronized (lock) {
                isRefreshing = false;
            }
        }
    }
}
