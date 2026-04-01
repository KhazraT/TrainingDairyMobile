package ru.squidory.trainingdairymobile.data.repository;

import android.util.Log;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Response;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;
import ru.squidory.trainingdairymobile.data.model.AuthRequest;
import ru.squidory.trainingdairymobile.data.model.AuthResponse;
import ru.squidory.trainingdairymobile.data.model.RefreshRequest;
import ru.squidory.trainingdairymobile.data.remote.NetworkClient;
import ru.squidory.trainingdairymobile.data.remote.api.AuthApi;

public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private static AuthRepository instance;

    private final AuthApi authApi;
    private final PreferencesManager preferencesManager;

    private AuthRepository() {
        this.authApi = NetworkClient.getAuthApi();
        this.preferencesManager = PreferencesManager.getInstance();
    }

    public static synchronized AuthRepository getInstance() {
        if (instance == null) {
            instance = new AuthRepository();
        }
        return instance;
    }

    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }

    public void register(String email, String password, String name, String birthDate, String gender, AuthCallback callback) {
        AuthRequest request = new AuthRequest(email, password, name, birthDate, gender);
        authApi.register(request).enqueue(new retrofit2.Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    preferencesManager.saveTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());
                    preferencesManager.saveUserId(authResponse.getUserId());
                    preferencesManager.saveUserEmail(authResponse.getEmail());
                    Log.d(TAG, "Registration successful");
                    callback.onSuccess(authResponse);
                } else {
                    String error = getErrorMessage(response);
                    Log.e(TAG, "Registration failed: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "Registration network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public void login(String email, String password, AuthCallback callback) {
        AuthRequest request = new AuthRequest(email, password);
        authApi.login(request).enqueue(new retrofit2.Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    preferencesManager.saveTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());
                    preferencesManager.saveUserId(authResponse.getUserId());
                    preferencesManager.saveUserEmail(authResponse.getEmail());
                    Log.d(TAG, "Login successful");
                    callback.onSuccess(authResponse);
                } else {
                    String error = getErrorMessage(response);
                    Log.e(TAG, "Login failed: " + error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "Login network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public void logout() {
        authApi.logout().enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "Logout successful");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Logout error: " + t.getMessage());
            }
        });
        preferencesManager.clearTokens();
    }

    public void refreshToken(String refreshToken, AuthCallback callback) {
        RefreshRequest request = new RefreshRequest(refreshToken);
        authApi.refreshToken(request).enqueue(new retrofit2.Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    preferencesManager.saveTokens(authResponse.getAccessToken(), authResponse.getRefreshToken());
                    Log.d(TAG, "Token refreshed successfully");
                    callback.onSuccess(authResponse);
                } else {
                    Log.e(TAG, "Token refresh failed");
                    callback.onError("Token refresh failed");
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "Token refresh network error: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public boolean isLoggedIn() {
        return preferencesManager.isLoggedIn();
    }

    public String getAccessToken() {
        return preferencesManager.getAccessToken();
    }

    public String getRefreshToken() {
        return preferencesManager.getRefreshToken();
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
