package ru.squidory.trainingdairymobile.data.remote;

import android.util.Log;
import androidx.annotation.NonNull;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;

/**
 * Interceptor для добавления JWT токена в заголовки запросов.
 */
public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // Получаем токен из PreferencesManager
        String accessToken = PreferencesManager.getInstance().getAccessToken();
        
        Log.d(TAG, "Access token: " + (accessToken != null && !accessToken.isEmpty() ? "present" : "empty"));
        
        // Если токен есть, добавляем его в заголовок
        if (accessToken != null && !accessToken.isEmpty()) {
            Request authorizedRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
            Log.d(TAG, "Request with auth header: " + authorizedRequest.url());
            return chain.proceed(authorizedRequest);
        }
        
        Log.d(TAG, "Request without auth header: " + originalRequest.url());
        return chain.proceed(originalRequest);
    }
}
