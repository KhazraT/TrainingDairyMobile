package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Прогресс упражнения за период.
 * GET /api/stats/exercises/{id}/progress
 */
public class ExerciseProgressResponse {

    @SerializedName("period")
    private String period; // формат: "2026-01" для MONTHLY, "2026-W05" для WEEKLY, "2026-01-15" для DAILY

    @SerializedName("maxWeight")
    private Double maxWeight;

    @SerializedName("averageWeight")
    private Double averageWeight;

    @SerializedName("totalVolume")
    private Double totalVolume;

    @SerializedName("totalReps")
    private Integer totalReps;

    public ExerciseProgressResponse() {}

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(Double maxWeight) { this.maxWeight = maxWeight; }

    public Double getAverageWeight() { return averageWeight; }
    public void setAverageWeight(Double averageWeight) { this.averageWeight = averageWeight; }

    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }

    public Integer getTotalReps() { return totalReps; }
    public void setTotalReps(Integer totalReps) { this.totalReps = totalReps; }
}
