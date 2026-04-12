package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SessionSummaryResponse {

    @SerializedName("byExercise")
    private List<ExerciseSummary> byExercise;

    @SerializedName("byMuscle")
    private List<MuscleSummary> byMuscle;

    public SessionSummaryResponse() {}

    public List<ExerciseSummary> getByExercise() { return byExercise; }
    public void setByExercise(List<ExerciseSummary> byExercise) { this.byExercise = byExercise; }

    public List<MuscleSummary> getByMuscle() { return byMuscle; }
    public void setByMuscle(List<MuscleSummary> byMuscle) { this.byMuscle = byMuscle; }

    public static class ExerciseSummary {
        @SerializedName("exerciseId")
        private Long exerciseId;

        @SerializedName("exerciseName")
        private String exerciseName;

        @SerializedName("setsCompleted")
        private Integer setsCompleted;

        @SerializedName("tonnage")
        private Double tonnage;

        @SerializedName("maxWeight")
        private Double maxWeight;

        @SerializedName("totalReps")
        private Integer totalReps;

        public ExerciseSummary() {}

        public Long getExerciseId() { return exerciseId; }
        public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

        public String getExerciseName() { return exerciseName; }
        public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

        public Integer getSetsCompleted() { return setsCompleted; }
        public void setSetsCompleted(Integer setsCompleted) { this.setsCompleted = setsCompleted; }

        public Double getTonnage() { return tonnage; }
        public void setTonnage(Double tonnage) { this.tonnage = tonnage; }

        public Double getMaxWeight() { return maxWeight; }
        public void setMaxWeight(Double maxWeight) { this.maxWeight = maxWeight; }

        public Integer getTotalReps() { return totalReps; }
        public void setTotalReps(Integer totalReps) { this.totalReps = totalReps; }
    }

    public static class MuscleSummary {
        @SerializedName("muscleId")
        private Long muscleId;

        @SerializedName("muscleName")
        private String muscleName;

        @SerializedName("totalTonnage")
        private Double totalTonnage;

        @SerializedName("totalSets")
        private Integer totalSets;

        public MuscleSummary() {}

        public Long getMuscleId() { return muscleId; }
        public void setMuscleId(Long muscleId) { this.muscleId = muscleId; }

        public String getMuscleName() { return muscleName; }
        public void setMuscleName(String muscleName) { this.muscleName = muscleName; }

        public Double getTotalTonnage() { return totalTonnage; }
        public void setTotalTonnage(Double totalTonnage) { this.totalTonnage = totalTonnage; }

        public Integer getTotalSets() { return totalSets; }
        public void setTotalSets(Integer totalSets) { this.totalSets = totalSets; }
    }
}
