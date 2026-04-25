package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SessionExerciseResponse {

    @SerializedName("sessionExerciseId")
    private Long sessionExerciseId;

    @SerializedName("id")
    private Long idLegacy;

    @SerializedName("sessionId")
    private Long sessionId;

    @SerializedName("exerciseId")
    private Long exerciseId;

    @SerializedName("exercise")
    private ExerciseResponse exercise;

    @SerializedName("exerciseName")
    private String exerciseName;

    @SerializedName("exerciseType")
    private String exerciseType;

    @SerializedName("exerciseOrder")
    private Integer exerciseOrder;

    @SerializedName("supersetGroupNumber")
    private Integer supersetGroupNumber;

    @SerializedName("targetMuscles")
    private List<MuscleGroupResponse> targetMuscles;

    @SerializedName("equipment")
    private List<EquipmentResponse> equipment;

    @SerializedName("plannedSets")
    private List<PlannedSetResponse> plannedSets;

    @SerializedName("completedSets")
    private List<SessionSetResponse> completedSets;

    // Fallback для API, которое возвращает "sets" вместо "completedSets"
    @SerializedName("sets")
    private List<SessionSetResponse> sets;

    @SerializedName("workoutExerciseId")
    private Long workoutExerciseId;

    public SessionExerciseResponse() {}

    public Long getSessionExerciseId() {
        if (sessionExerciseId != null) return sessionExerciseId;
        return idLegacy != null ? idLegacy : 0;
    }
    public void setSessionExerciseId(Long sessionExerciseId) { this.sessionExerciseId = sessionExerciseId; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getExerciseId() {
        if (exerciseId != null) return exerciseId;
        return exercise != null ? exercise.getId() : null;
    }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }

    public ExerciseResponse getExercise() { return exercise; }
    public void setExercise(ExerciseResponse exercise) { this.exercise = exercise; }

    public String getExerciseName() {
        if (exerciseName != null) return exerciseName;
        return exercise != null ? exercise.getName() : "";
    }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }

    public String getExerciseType() {
        if (exerciseType != null) return exerciseType;
        return exercise != null ? exercise.getExerciseType() : "REPS_WEIGHT";
    }
    public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }

    public Integer getExerciseOrder() { return exerciseOrder != null ? exerciseOrder : 1; }
    public void setExerciseOrder(Integer exerciseOrder) { this.exerciseOrder = exerciseOrder; }

    public Integer getSupersetGroupNumber() { return supersetGroupNumber; }
    public void setSupersetGroupNumber(Integer supersetGroupNumber) { this.supersetGroupNumber = supersetGroupNumber; }

    public List<MuscleGroupResponse> getTargetMuscles() { return targetMuscles; }
    public void setTargetMuscles(List<MuscleGroupResponse> targetMuscles) { this.targetMuscles = targetMuscles; }

    public List<EquipmentResponse> getEquipment() { return equipment; }
    public void setEquipment(List<EquipmentResponse> equipment) { this.equipment = equipment; }

    public List<PlannedSetResponse> getPlannedSets() { return plannedSets; }
    public void setPlannedSets(List<PlannedSetResponse> plannedSets) { this.plannedSets = plannedSets; }

    public List<SessionSetResponse> getCompletedSets() {
        if (completedSets != null) return completedSets;
        return sets;
    }
    public void setCompletedSets(List<SessionSetResponse> completedSets) { this.completedSets = completedSets; }

    // Legacy compatibility
    public long getId() { return getSessionExerciseId(); }
    public void setId(long id) { this.sessionExerciseId = id; }

    public long getWorkoutExerciseId() { return workoutExerciseId != null ? workoutExerciseId : 0; }
    public void setWorkoutExerciseId(long workoutExerciseId) { this.workoutExerciseId = workoutExerciseId; }
}
