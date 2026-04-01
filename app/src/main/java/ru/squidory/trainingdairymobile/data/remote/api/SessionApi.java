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
import ru.squidory.trainingdairymobile.data.model.SessionRequest;
import ru.squidory.trainingdairymobile.data.model.SessionResponse;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseRequest;
import ru.squidory.trainingdairymobile.data.model.SessionExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.SessionSetRequest;
import ru.squidory.trainingdairymobile.data.model.SessionSetResponse;

public interface SessionApi {

    // Сессии
    @GET("sessions")
    Call<List<SessionResponse>> getSessions();

    @GET("sessions/{id}")
    Call<SessionResponse> getSessionById(@Path("id") long id);

    @GET("workouts/{workoutId}/sessions")
    Call<List<SessionResponse>> getSessionsByWorkout(@Path("workoutId") long workoutId);

    @POST("sessions/start")
    Call<SessionResponse> startSession(@Body SessionRequest request);

    @PUT("sessions/{id}/finish")
    Call<SessionResponse> finishSession(@Path("id") long id);

    @PUT("sessions/{id}")
    Call<SessionResponse> updateSession(@Path("id") long id, @Body SessionRequest request);

    @DELETE("sessions/{id}")
    Call<Void> deleteSession(@Path("id") long id);

    // Упражнения в сессии
    @GET("session-exercises/{sessionId}")
    Call<List<SessionExerciseResponse>> getSessionExercises(@Path("sessionId") long sessionId);

    @POST("sessions/{sessionId}/exercises")
    Call<SessionExerciseResponse> addExerciseToSession(@Path("sessionId") long sessionId, @Body SessionExerciseRequest request);

    @PUT("session-exercises/{id}")
    Call<SessionExerciseResponse> updateSessionExercise(@Path("id") long id, @Body SessionExerciseRequest request);

    @DELETE("session-exercises/{id}")
    Call<Void> deleteSessionExercise(@Path("id") long id);

    // Выполненные подходы
    @GET("session-exercises/{sessionExerciseId}/sets")
    Call<List<SessionSetResponse>> getSessionSets(@Path("sessionExerciseId") long sessionExerciseId);

    @POST("session-exercises/{sessionExerciseId}/sets")
    Call<SessionSetResponse> createSessionSet(@Path("sessionExerciseId") long sessionExerciseId, @Body SessionSetRequest request);

    @PUT("session-sets/{id}")
    Call<SessionSetResponse> updateSessionSet(@Path("id") long id, @Body SessionSetRequest request);

    @DELETE("session-sets/{id}")
    Call<Void> deleteSessionSet(@Path("id") long id);
}
