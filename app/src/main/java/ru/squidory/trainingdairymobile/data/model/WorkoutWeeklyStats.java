package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Статистика тренировок по неделям.
 * GET /api/stats/workouts/weekly
 */
public class WorkoutWeeklyStats {

    @SerializedName("week")
    private Integer week;

    @SerializedName("year")
    private Integer year;

    @SerializedName("workoutCount")
    private Integer workoutCount;

    @SerializedName("completedCount")
    private Integer completedCount;

    @SerializedName("completionRate")
    private Double completionRate;

    public WorkoutWeeklyStats() {}

    public Integer getWeek() { return week; }
    public void setWeek(Integer week) { this.week = week; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(Integer workoutCount) { this.workoutCount = workoutCount; }

    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
}
