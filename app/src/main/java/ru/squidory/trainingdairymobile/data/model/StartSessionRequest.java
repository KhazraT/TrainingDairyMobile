package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class StartSessionRequest {

    @SerializedName("workoutId")
    private Long workoutId;

    public StartSessionRequest() {}

    public StartSessionRequest(Long workoutId) {
        this.workoutId = workoutId;
    }

    public Long getWorkoutId() { return workoutId; }
    public void setWorkoutId(Long workoutId) { this.workoutId = workoutId; }
}
