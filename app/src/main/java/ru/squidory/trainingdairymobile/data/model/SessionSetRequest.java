package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SessionSetRequest {

    @SerializedName("setNumber")
    private Integer setNumber;

    @SerializedName("reps")
    private Integer reps;

    @SerializedName("weight")
    private Double weight;

    @SerializedName("time")
    private Integer time;

    @SerializedName("distance")
    private Double distance;

    @SerializedName("restTime")
    private Integer restTime;

    @SerializedName("dropsetWeights")
    private List<Double> dropsetWeights;

    @SerializedName("dropsetReps")
    private List<Integer> dropsetReps;

    public SessionSetRequest() {}

    // Getters and Setters
    public Integer getSetNumber() { return setNumber; }
    public void setSetNumber(Integer setNumber) { this.setNumber = setNumber; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Integer getTime() { return time; }
    public void setTime(Integer time) { this.time = time; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public Integer getRestTime() { return restTime; }
    public void setRestTime(Integer restTime) { this.restTime = restTime; }

    public List<Double> getDropsetWeights() { return dropsetWeights; }
    public void setDropsetWeights(List<Double> dropsetWeights) { this.dropsetWeights = dropsetWeights; }

    public List<Integer> getDropsetReps() { return dropsetReps; }
    public void setDropsetReps(List<Integer> dropsetReps) { this.dropsetReps = dropsetReps; }
}
