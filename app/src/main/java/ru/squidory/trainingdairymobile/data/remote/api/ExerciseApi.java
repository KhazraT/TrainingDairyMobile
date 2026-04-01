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
import ru.squidory.trainingdairymobile.data.model.EquipmentResponse;
import ru.squidory.trainingdairymobile.data.model.ExerciseRequest;
import ru.squidory.trainingdairymobile.data.model.ExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.MuscleGroupResponse;

public interface ExerciseApi {

    @GET("exercises")
    Call<List<ExerciseResponse>> getExercises(
        @Query("muscle") String muscle,
        @Query("equipment") String equipment,
        @Query("type") String type
    );

    @GET("exercises/{id}")
    Call<ExerciseResponse> getExerciseById(@Path("id") long id);

    @POST("exercises")
    Call<ExerciseResponse> createExercise(@Body ExerciseRequest request);

    @PUT("exercises/{id}")
    Call<ExerciseResponse> updateExercise(@Path("id") long id, @Body ExerciseRequest request);

    @DELETE("exercises/{id}")
    Call<Void> deleteExercise(@Path("id") long id);

    @GET("muscle-groups")
    Call<List<MuscleGroupResponse>> getMuscleGroups();

    @GET("equipment")
    Call<List<EquipmentResponse>> getEquipment();
}
