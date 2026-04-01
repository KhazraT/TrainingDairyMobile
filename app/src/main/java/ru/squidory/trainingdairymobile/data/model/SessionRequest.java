package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class SessionRequest {

    @SerializedName("programId")
    private Long programId;

    @SerializedName("workoutId")
    private Long workoutId;

    @SerializedName("comment")
    private String comment;

    public SessionRequest() {}

    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }

    public Long getWorkoutId() { return workoutId; }
    public void setWorkoutId(Long workoutId) { this.workoutId = workoutId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
