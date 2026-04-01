package ru.squidory.trainingdairymobile.data.remote.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import ru.squidory.trainingdairymobile.data.model.ProgramRequest;
import ru.squidory.trainingdairymobile.data.model.ProgramResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutRequest;
import ru.squidory.trainingdairymobile.data.model.WorkoutResponse;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseRequest;
import ru.squidory.trainingdairymobile.data.model.WorkoutExerciseResponse;
import ru.squidory.trainingdairymobile.data.model.PlannedSetRequest;
import ru.squidory.trainingdairymobile.data.model.PlannedSetResponse;

public interface ProgramApi {

    // Программы тренировок
    @GET("programs")
    Call<List<ProgramResponse>> getPrograms();

    @GET("programs/{id}")
    Call<ProgramResponse> getProgramById(@Path("id") long id);

    @POST("programs")
    Call<ProgramResponse> createProgram(@Body ProgramRequest request);

    @PUT("programs/{id}")
    Call<ProgramResponse> updateProgram(@Path("id") long id, @Body ProgramRequest request);

    @DELETE("programs/{id}")
    Call<Void> deleteProgram(@Path("id") long id);

    // Тренировки внутри программы
    @GET("programs/{programId}/workouts")
    Call<List<WorkoutResponse>> getWorkoutsByProgram(@Path("programId") long programId);

    @POST("programs/{programId}/workouts")
    Call<WorkoutResponse> createWorkout(@Path("programId") long programId, @Body WorkoutRequest request);

    @PUT("workouts/{id}")
    Call<WorkoutResponse> updateWorkout(@Path("id") long id, @Body WorkoutRequest request);

    @DELETE("workouts/{id}")
    Call<Void> deleteWorkout(@Path("id") long id);

    // Упражнения в тренировке
    @GET("workouts/{workoutId}/exercises")
    Call<List<WorkoutExerciseResponse>> getWorkoutExercises(@Path("workoutId") long workoutId);

    @GET("workouts/{workoutId}/exercises/grouped-by-supersets")
    Call<List<List<WorkoutExerciseResponse>>> getWorkoutExercisesGrouped(@Path("workoutId") long workoutId);

    @POST("workouts/{workoutId}/exercises")
    Call<WorkoutExerciseResponse> addExerciseToWorkout(@Path("workoutId") long workoutId, @Body WorkoutExerciseRequest request);

    @PUT("workout-exercises/{id}")
    Call<WorkoutExerciseResponse> updateWorkoutExercise(@Path("id") long id, @Body WorkoutExerciseRequest request);

    @DELETE("workout-exercises/{id}")
    Call<Void> deleteWorkoutExercise(@Path("id") long id);

    // Планируемые подходы
    @GET("workout-exercises/{workoutExerciseId}/sets")
    Call<List<PlannedSetResponse>> getPlannedSets(@Path("workoutExerciseId") long workoutExerciseId);

    @POST("workout-exercises/{workoutExerciseId}/sets")
    Call<PlannedSetResponse> createPlannedSet(@Path("workoutExerciseId") long workoutExerciseId, @Body PlannedSetRequest request);

    @PUT("planned-sets/{id}")
    Call<PlannedSetResponse> updatePlannedSet(@Path("id") long id, @Body PlannedSetRequest request);

    @DELETE("planned-sets/{id}")
    Call<Void> deletePlannedSet(@Path("id") long id);
}
