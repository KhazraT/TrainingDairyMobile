package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class VolumeStatsResponse {

    @SerializedName("period")
    private String period;

    @SerializedName("volume")
    private Double volume;

    @SerializedName("muscleGroup")
    private String muscleGroup;

    @SerializedName("exerciseId")
    private Long exerciseId;

    @SerializedName("exerciseName")
    private String exerciseName;

    public VolumeStatsResponse() {}

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }

    public String getMuscleGroup() { return muscleGroup; }
    public void setMuscleGroup(String muscleGroup) { this.muscleGroup = muscleGroup; }

    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }
}
