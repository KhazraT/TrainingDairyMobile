package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class WorkoutExerciseResponse {

    @SerializedName("id")
    private long id;

    @SerializedName("exerciseId")
    private long exerciseId;

    @SerializedName("workoutId")
    private long workoutId;

    @SerializedName("exercise")
    private ExerciseResponse exercise;

    @SerializedName("setsCount")
    private Integer setsCount;

    @SerializedName("setType")
    private String setType;

    @SerializedName("supersetGroupNumber")
    private Integer supersetGroupNumber;

    @SerializedName("exerciseOrder")
    private Integer exerciseOrder;

    public WorkoutExerciseResponse() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getExerciseId() { return exerciseId; }
    public void setExerciseId(long exerciseId) { this.exerciseId = exerciseId; }

    public long getWorkoutId() { return workoutId; }
    public void setWorkoutId(long workoutId) { this.workoutId = workoutId; }

    public ExerciseResponse getExercise() { return exercise; }
    public void setExercise(ExerciseResponse exercise) { this.exercise = exercise; }

    public Integer getSetsCount() { return setsCount; }
    public void setSetsCount(Integer setsCount) { this.setsCount = setsCount; }

    public String getSetType() { return setType; }
    public void setSetType(String setType) { this.setType = setType; }

    public Integer getSupersetGroupNumber() { return supersetGroupNumber; }
    public void setSupersetGroupNumber(Integer supersetGroupNumber) { this.supersetGroupNumber = supersetGroupNumber; }

    public Integer getExerciseOrder() { return exerciseOrder; }
    public void setExerciseOrder(Integer exerciseOrder) { this.exerciseOrder = exerciseOrder; }
}
