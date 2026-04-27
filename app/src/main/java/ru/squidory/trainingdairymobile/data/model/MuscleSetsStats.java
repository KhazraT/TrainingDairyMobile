package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Статистика подходов по группам мышц.
 * GET /api/stats/sets/muscles
 */
public class MuscleSetsStats {

    @SerializedName("muscleGroupId")
    private Long muscleGroupId;

    @SerializedName("muscleGroupName")
    private String muscleGroupName;

    @SerializedName("totalSets")
    private Integer totalSets;

    @SerializedName("totalReps")
    private Integer totalReps;

    @SerializedName("averageSetsPerWorkout")
    private Double averageSetsPerWorkout;

    public MuscleSetsStats() {}

    public Long getMuscleGroupId() { return muscleGroupId; }
    public void setMuscleGroupId(Long muscleGroupId) { this.muscleGroupId = muscleGroupId; }

    public String getMuscleGroupName() { return muscleGroupName; }
    public void setMuscleGroupName(String muscleGroupName) { this.muscleGroupName = muscleGroupName; }

    public Integer getTotalSets() { return totalSets; }
    public void setTotalSets(Integer totalSets) { this.totalSets = totalSets; }

    public Integer getTotalReps() { return totalReps; }
    public void setTotalReps(Integer totalReps) { this.totalReps = totalReps; }

    public Double getAverageSetsPerWorkout() { return averageSetsPerWorkout; }
    public void setAverageSetsPerWorkout(Double averageSetsPerWorkout) { this.averageSetsPerWorkout = averageSetsPerWorkout; }
}
