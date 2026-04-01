package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class SessionExerciseResponse {

    @SerializedName("id")
    private long id;

    @SerializedName("sessionId")
    private long sessionId;

    @SerializedName("workoutExerciseId")
    private long workoutExerciseId;

    @SerializedName("exercise")
    private ExerciseResponse exercise;

    @SerializedName("exerciseOrder")
    private Integer exerciseOrder;

    public SessionExerciseResponse() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }

    public long getWorkoutExerciseId() { return workoutExerciseId; }
    public void setWorkoutExerciseId(long workoutExerciseId) { this.workoutExerciseId = workoutExerciseId; }

    public ExerciseResponse getExercise() { return exercise; }
    public void setExercise(ExerciseResponse exercise) { this.exercise = exercise; }

    public Integer getExerciseOrder() { return exerciseOrder; }
    public void setExerciseOrder(Integer exerciseOrder) { this.exerciseOrder = exerciseOrder; }
}
