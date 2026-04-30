package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Модель ежедневного тоннажа.
 * GET /api/stats/volume/daily?startDate=...&endDate=...
 */
public class VolumeDailyResponse {
    @SerializedName("date")
    private String date; // yyyy-MM-dd

    @SerializedName("totalVolume")
    private Double totalVolume;

    @SerializedName("workoutCount")
    private Integer workoutCount;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }

    public Integer getWorkoutCount() { return workoutCount; }
    public void setWorkoutCount(Integer workoutCount) { this.workoutCount = workoutCount; }
}
