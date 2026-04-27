package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Статистика времени выполнения по неделям.
 * GET /api/stats/duration/weekly
 */
public class DurationWeeklyStats {

    @SerializedName("week")
    private Integer week;

    @SerializedName("year")
    private Integer year;

    @SerializedName("totalDurationMinutes")
    private Integer totalDurationMinutes;

    @SerializedName("averageDurationMinutes")
    private Integer averageDurationMinutes;

    @SerializedName("workoutCount")
    private Integer workoutCount;

    public DurationWeeklyStats() {}

    public Integer getWeek() { return week; }
    public void setWeek(Integer week) { this.week = week; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getTotalDurationMinutes() { return totalDurationMinutes; }
    public void setTotalDurationMinutes(Integer totalDurationMinutes) { this.totalDurationMinutes = totalDurationMinutes; }

    public Integer getAverageDurationMinutes() { return averageDurationMinutes; }
    public void setAverageDurationMinutes(Integer averageDurationMinutes) { this.averageDurationMinutes = averageDurationMinutes; }

    public Integer getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(Integer workoutCount) { this.workoutCount = workoutCount; }
}
