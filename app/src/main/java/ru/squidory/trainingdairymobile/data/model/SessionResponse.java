package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class SessionResponse {

    @SerializedName("id")
    private long id;

    @SerializedName("userId")
    private long userId;

    @SerializedName("programId")
    private Long programId;

    @SerializedName("workoutId")
    private Long workoutId;

    @SerializedName("comment")
    private String comment;

    @SerializedName("startTime")
    private Date startTime;

    @SerializedName("endTime")
    private Date endTime;

    public SessionResponse() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }

    public Long getWorkoutId() { return workoutId; }
    public void setWorkoutId(Long workoutId) { this.workoutId = workoutId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
}
