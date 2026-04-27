package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Статистика тренировок по месяцам.
 * GET /api/stats/workouts/monthly
 */
public class WorkoutMonthlyStats {

    @SerializedName("month")
    private Integer month;

    @SerializedName("year")
    private Integer year;

    @SerializedName("workoutCount")
    private Integer workoutCount;

    @SerializedName("completedCount")
    private Integer completedCount;

    @SerializedName("completionRate")
    private Double completionRate;

    public WorkoutMonthlyStats() {}

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(Integer workoutCount) { this.workoutCount = workoutCount; }

    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
}
