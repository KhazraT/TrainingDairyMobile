package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Статистика по группе мышц.
 * GET /api/stats/muscles
 */
public class MuscleStatsResponse {

    @SerializedName("muscleGroupId")
    private Long muscleGroupId;

    @SerializedName("muscleGroupName")
    private String muscleGroupName;

    @SerializedName("totalVolume")
    private Double totalVolume;

    @SerializedName("totalReps")
    private Integer totalReps;

    @SerializedName("totalSets")
    private Integer totalSets;

    @SerializedName("averageWeight")
    private Double averageWeight;

    public MuscleStatsResponse() {}

    public Long getMuscleGroupId() { return muscleGroupId; }
    public void setMuscleGroupId(Long muscleGroupId) { this.muscleGroupId = muscleGroupId; }

    public String getMuscleGroupName() { return muscleGroupName; }
    public void setMuscleGroupName(String muscleGroupName) { this.muscleGroupName = muscleGroupName; }

    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }

    public Integer getTotalReps() { return totalReps; }
    public void setTotalReps(Integer totalReps) { this.totalReps = totalReps; }

    public Integer getTotalSets() { return totalSets; }
    public void setTotalSets(Integer totalSets) { this.totalSets = totalSets; }

    public Double getAverageWeight() { return averageWeight; }
    public void setAverageWeight(Double averageWeight) { this.averageWeight = averageWeight; }
}
