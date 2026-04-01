package ru.squidory.trainingdairymobile.data.remote.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import ru.squidory.trainingdairymobile.data.model.UserResponse;
import ru.squidory.trainingdairymobile.data.model.UserSettingsResponse;
import ru.squidory.trainingdairymobile.data.model.UserSettingsRequest;

public interface UserApi {

    @GET("users/me")
    Call<UserResponse> getCurrentUser();

    @PUT("users/me")
    Call<UserResponse> updateCurrentUser(@Body UserResponse request);

    @DELETE("users/me")
    Call<Void> deleteCurrentUser();

    @GET("users/settings")
    Call<UserSettingsResponse> getUserSettings();

    @PUT("users/settings")
    Call<UserSettingsResponse> updateUserSettings(@Body UserSettingsRequest request);
}
