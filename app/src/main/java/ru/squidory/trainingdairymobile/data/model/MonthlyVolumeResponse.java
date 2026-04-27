package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Месячная статистика объёма.
 * GET /api/stats/volume/monthly
 */
public class MonthlyVolumeResponse {

    @SerializedName("month")
    private Integer month;

    @SerializedName("year")
    private Integer year;

    @SerializedName("totalVolume")
    private Double totalVolume;

    @SerializedName("workoutCount")
    private Integer workoutCount;

    public MonthlyVolumeResponse() {}

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }

    public Integer getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(Integer workoutCount) { this.workoutCount = workoutCount; }
}
