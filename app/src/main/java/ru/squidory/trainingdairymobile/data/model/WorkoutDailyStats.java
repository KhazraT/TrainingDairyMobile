package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель ежедневных тренировок.
 * GET /api/stats/workouts/daily?startDate=...&endDate=...
 */
public class WorkoutDailyStats {
    @SerializedName("date")
    private String date; // yyyy-MM-dd

    @SerializedName("workoutCount")
    private Integer workoutCount;

    @SerializedName("completedCount")
    private Integer completedCount;

    @SerializedName("completionRate")
    private Double completionRate;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Integer getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(Integer workoutCount) { this.workoutCount = workoutCount; }

    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
}
