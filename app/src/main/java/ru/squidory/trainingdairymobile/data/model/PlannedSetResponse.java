package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlannedSetResponse {

    @SerializedName("id")
    private long id;

    @SerializedName("workoutExerciseId")
    private long workoutExerciseId;

    @SerializedName("setNumber")
    private Integer setNumber;

    @SerializedName("setType")
    private String setType;

    @SerializedName("targetWeight")
    private Double targetWeight;

    @SerializedName("targetReps")
    private Integer targetReps;

    @SerializedName("targetTime")
    private Integer targetTime;

    @SerializedName("targetDistance")
    private Double targetDistance;

    @SerializedName("restTime")
    private Integer restTime;

    @SerializedName("dropsetEntries")
    private List<DropsetEntry> dropsetEntries;

    public PlannedSetResponse() {}

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getWorkoutExerciseId() { return workoutExerciseId; }
    public void setWorkoutExerciseId(long workoutExerciseId) { this.workoutExerciseId = workoutExerciseId; }

    public Integer getSetNumber() { return setNumber; }
    public void setSetNumber(Integer setNumber) { this.setNumber = setNumber; }

    public String getSetType() { return setType; }
    public void setSetType(String setType) { this.setType = setType; }

    public Double getTargetWeight() { return targetWeight; }
    public void setTargetWeight(Double targetWeight) { this.targetWeight = targetWeight; }

    public Integer getTargetReps() { return targetReps; }
    public void setTargetReps(Integer targetReps) { this.targetReps = targetReps; }

    public Integer getTargetTime() { return targetTime; }
    public void setTargetTime(Integer targetTime) { this.targetTime = targetTime; }

    public Double getTargetDistance() { return targetDistance; }
    public void setTargetDistance(Double targetDistance) { this.targetDistance = targetDistance; }

    public Integer getRestTime() { return restTime; }
    public void setRestTime(Integer restTime) { this.restTime = restTime; }

    public List<DropsetEntry> getDropsetEntries() { return dropsetEntries; }
    public void setDropsetEntries(List<DropsetEntry> dropsetEntries) { this.dropsetEntries = dropsetEntries; }

    public static class DropsetEntry {
        @SerializedName("weight")
        private Double weight;

        @SerializedName("reps")
        private Integer reps;

        public DropsetEntry() {}

        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }

        public Integer getReps() { return reps; }
        public void setReps(Integer reps) { this.reps = reps; }
    }
}
