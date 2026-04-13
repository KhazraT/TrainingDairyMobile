package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class CompleteSessionRequest {

    @SerializedName("workoutId")
    private Long workoutId;

    @SerializedName("startedAt")
    private Date startedAt;

    @SerializedName("completedAt")
    private Date completedAt;

    @SerializedName("exercises")
    private List<ExerciseCompletion> exercises;

    @SerializedName("removedExerciseIds")
    private List<Long> removedExerciseIds;

    public CompleteSessionRequest() {}

    public Long getWorkoutId() { return workoutId; }
    public void setWorkoutId(Long workoutId) { this.workoutId = workoutId; }

    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public List<ExerciseCompletion> getExercises() { return exercises; }
    public void setExercises(List<ExerciseCompletion> exercises) { this.exercises = exercises; }

    public List<Long> getRemovedExerciseIds() { return removedExerciseIds; }
    public void setRemovedExerciseIds(List<Long> removedExerciseIds) { this.removedExerciseIds = removedExerciseIds; }

    public static class ExerciseCompletion {
        @SerializedName("exerciseId")
        private Long exerciseId;

        @SerializedName("exerciseOrder")
        private Integer exerciseOrder;

        @SerializedName("exerciseType")
        private String exerciseType;

        @SerializedName("completedSets")
        private List<CompletedSetData> completedSets;

        public ExerciseCompletion() {}

        public Long getExerciseId() { return exerciseId; }
        public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

        public Integer getExerciseOrder() { return exerciseOrder; }
        public void setExerciseOrder(Integer exerciseOrder) { this.exerciseOrder = exerciseOrder; }

        public String getExerciseType() { return exerciseType; }
        public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }

        public List<CompletedSetData> getCompletedSets() { return completedSets; }
        public void setCompletedSets(List<CompletedSetData> completedSets) { this.completedSets = completedSets; }
    }

    public static class CompletedSetData {
        @SerializedName("weight")
        private Double weight;

        @SerializedName("reps")
        private Integer reps;

        @SerializedName("durationSeconds")
        private Integer durationSeconds;

        @SerializedName("distanceMeters")
        private Double distanceMeters;

        @SerializedName("rpe")
        private Integer rpe;

        @SerializedName("notes")
        private String notes;

        @SerializedName("setOrder")
        private Integer setOrder;

        @SerializedName("isWarmup")
        private Boolean isWarmup;

        @SerializedName("isDropset")
        private Boolean isDropset;

        @SerializedName("dropsetWeight")
        private Double dropsetWeight;

        @SerializedName("dropsetReps")
        private Integer dropsetReps;

        public CompletedSetData() {}

        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }

        public Integer getReps() { return reps; }
        public void setReps(Integer reps) { this.reps = reps; }

        public Integer getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

        public Double getDistanceMeters() { return distanceMeters; }
        public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

        public Integer getRpe() { return rpe; }
        public void setRpe(Integer rpe) { this.rpe = rpe; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public Integer getSetOrder() { return setOrder; }
        public void setSetOrder(Integer setOrder) { this.setOrder = setOrder; }

        public Boolean isWarmup() { return isWarmup != null && isWarmup; }
        public void setIsWarmup(Boolean isWarmup) { this.isWarmup = isWarmup; }

        public Boolean isDropset() { return isDropset != null && isDropset; }
        public void setIsDropset(Boolean isDropset) { this.isDropset = isDropset; }

        public Double getDropsetWeight() { return dropsetWeight; }
        public void setDropsetWeight(Double dropsetWeight) { this.dropsetWeight = dropsetWeight; }

        public Integer getDropsetReps() { return dropsetReps; }
        public void setDropsetReps(Integer dropsetReps) { this.dropsetReps = dropsetReps; }
    }
}
