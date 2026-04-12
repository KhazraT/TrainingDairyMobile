package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class SessionResponse {

    @SerializedName("sessionId")
    private Long sessionId;

    @SerializedName("id")
    private Long idLegacy;

    @SerializedName("workoutId")
    private Long workoutId;

    @SerializedName("workoutName")
    private String workoutName;

    @SerializedName("workoutComment")
    private String workoutComment;

    @SerializedName("startedAt")
    private Date startedAt;

    @SerializedName("startTime")
    private Date startTime;

    @SerializedName("completedAt")
    private Date completedAt;

    @SerializedName("endTime")
    private Date endTime;

    @SerializedName("status")
    private String status;

    @SerializedName("exercises")
    private List<SessionExerciseResponse> exercises;

    @SerializedName("restTimerSeconds")
    private Integer restTimerSeconds;

    @SerializedName("totalDurationSeconds")
    private Integer totalDurationSeconds;

    @SerializedName("totalSets")
    private Integer totalSets;

    @SerializedName("totalTonnage")
    private Double totalTonnage;

    @SerializedName("summary")
    private SessionSummaryResponse summary;

    public SessionResponse() {}

    public Long getSessionId() {
        if (sessionId != null) return sessionId;
        return idLegacy != null ? idLegacy : 0L;
    }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getWorkoutId() { return workoutId; }
    public void setWorkoutId(Long workoutId) { this.workoutId = workoutId; }

    public String getWorkoutName() { return workoutName; }
    public void setWorkoutName(String workoutName) { this.workoutName = workoutName; }

    public String getWorkoutComment() { return workoutComment; }
    public void setWorkoutComment(String workoutComment) { this.workoutComment = workoutComment; }

    public Date getStartedAt() { return startedAt != null ? startedAt : startTime; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }

    public Date getCompletedAt() { return completedAt != null ? completedAt : endTime; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<SessionExerciseResponse> getExercises() { return exercises; }
    public void setExercises(List<SessionExerciseResponse> exercises) { this.exercises = exercises; }

    public Integer getRestTimerSeconds() { return restTimerSeconds != null ? restTimerSeconds : 60; }
    public void setRestTimerSeconds(Integer restTimerSeconds) { this.restTimerSeconds = restTimerSeconds; }

    public Integer getTotalDurationSeconds() { return totalDurationSeconds; }
    public void setTotalDurationSeconds(Integer totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; }

    public Integer getTotalSets() { return totalSets; }
    public void setTotalSets(Integer totalSets) { this.totalSets = totalSets; }

    public Double getTotalTonnage() { return totalTonnage; }
    public void setTotalTonnage(Double totalTonnage) { this.totalTonnage = totalTonnage; }

    public SessionSummaryResponse getSummary() { return summary; }
    public void setSummary(SessionSummaryResponse summary) { this.summary = summary; }

    // Legacy fields compatibility
    public long getId() { return getSessionId() != null ? getSessionId() : 0; }
    public void setId(long id) { this.sessionId = id; }

    public long getUserId() { return 0; }
    public void setUserId(long userId) {}

    public Long getProgramId() { return null; }
    public void setProgramId(Long programId) {}

    public String getComment() { return workoutComment; }
    public void setComment(String comment) { this.workoutComment = comment; }

    public Date getStartTime() { return getStartedAt(); }
    public void setStartTime(Date startTime) { this.startedAt = startTime; }

    public Date getEndTime() { return getCompletedAt(); }
    public void setEndTime(Date endTime) { this.completedAt = endTime; }
}
