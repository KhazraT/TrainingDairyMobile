package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Статистика по упражнению.
 * GET /api/stats/exercises
 */
public class ExerciseStatsResponse {

    @SerializedName("exerciseId")
    private Long exerciseId;

    @SerializedName("exerciseName")
    private String exerciseName;

    @SerializedName("totalVolume")
    private Double totalVolume;

    @SerializedName("totalReps")
    private Integer totalReps;

    @SerializedName("totalSets")
    private Integer totalSets;

    @SerializedName("maxWeight")
    private Double maxWeight;

    @SerializedName("averageWeight")
    private Double averageWeight;

    public ExerciseStatsResponse() {}

    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }

    public Integer getTotalReps() { return totalReps; }
    public void setTotalReps(Integer totalReps) { this.totalReps = totalReps; }

    public Integer getTotalSets() { return totalSets; }
    public void setTotalSets(Integer totalSets) { this.totalSets = totalSets; }

    public Double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(Double maxWeight) { this.maxWeight = maxWeight; }

    public Double getAverageWeight() { return averageWeight; }
    public void setAverageWeight(Double averageWeight) { this.averageWeight = averageWeight; }
}
