package ru.squidory.trainingdairymobile.data.remote.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.squidory.trainingdairymobile.data.model.AntiGSessionResponse;
import ru.squidory.trainingdairymobile.data.model.AntiGSessionRequest;

/**
 * API interface for anti-G breathing training sessions.
 */
public interface AntiGSessionApi {
    @GET("anti-g-sessions")
    Call<List<AntiGSessionResponse>> getAllSessions(@Query("mine") Boolean mine);

    @GET("anti-g-sessions/{id}")
    Call<AntiGSessionResponse> getSessionById(@Path("id") Long id);

    @POST("anti-g-sessions")
    Call<AntiGSessionResponse> createSession(@Body AntiGSessionRequest request);

    @PUT("anti-g-sessions/{id}")
    Call<AntiGSessionResponse> updateSession(@Path("id") Long id, @Body AntiGSessionRequest request);

    @DELETE("anti-g-sessions/{id}")
    Call<Void> deleteSession(@Path("id") Long id);
}