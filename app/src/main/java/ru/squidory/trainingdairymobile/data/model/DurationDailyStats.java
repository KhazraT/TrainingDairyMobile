package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель ежедневной длительности.
 * GET /api/stats/duration/daily?startDate=...&endDate=...
 */
public class DurationDailyStats {
    @SerializedName("date")
    private String date; // yyyy-MM-dd

    @SerializedName("totalDurationMinutes")
    private Double totalDurationMinutes;

    @SerializedName("workoutCount")
    private Integer workoutCount;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Double getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(Double totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }

    public Integer getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(Integer workoutCount) { this.workoutCount = workoutCount; }
}
