package ru.squidory.trainingdairymobile.data.remote.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import ru.squidory.trainingdairymobile.data.model.AuthRequest;
import ru.squidory.trainingdairymobile.data.model.AuthResponse;
import ru.squidory.trainingdairymobile.data.model.RefreshRequest;

public interface AuthApi {

    @POST("auth/register")
    Call<AuthResponse> register(@Body AuthRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    @POST("auth/refresh")
    Call<AuthResponse> refreshToken(@Body RefreshRequest request);

    @POST("auth/logout")
    Call<Void> logout();
}
