package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class SessionExerciseRequest {

    @SerializedName("workoutExerciseId")
    private long workoutExerciseId;

    @SerializedName("exerciseOrder")
    private Integer exerciseOrder;

    public SessionExerciseRequest() {}

    public long getWorkoutExerciseId() { return workoutExerciseId; }
    public void setWorkoutExerciseId(long workoutExerciseId) { this.workoutExerciseId = workoutExerciseId; }

    public Integer getExerciseOrder() { return exerciseOrder; }
    public void setExerciseOrder(Integer exerciseOrder) { this.exerciseOrder = exerciseOrder; }
}
