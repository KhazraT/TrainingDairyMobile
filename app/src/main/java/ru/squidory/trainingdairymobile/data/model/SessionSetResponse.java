package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SessionSetResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("sessionExerciseId")
    private Long sessionExerciseId;

    @SerializedName("setOrder")
    private Integer setOrder;

    @SerializedName("setNumber")
    private Integer setNumber;

    @SerializedName("weight")
    private Double weight;

    @SerializedName("reps")
    private Integer reps;

    @SerializedName("durationSeconds")
    private Integer durationSeconds;

    @SerializedName("time")
    private Integer timeLegacy;

    @SerializedName("distanceMeters")
    private Double distanceMeters;

    @SerializedName("distance")
    private Double distanceLegacy;

    @SerializedName("rpe")
    private Integer rpe;

    @SerializedName("notes")
    private String notes;

    @SerializedName("isWarmup")
    private Boolean isWarmup;

    @SerializedName("isDropset")
    private Boolean isDropset;

    @SerializedName("dropsetWeight")
    private Double dropsetWeight;

    @SerializedName("dropsetReps")
    private Integer dropsetReps;

    @SerializedName("dropsetWeights")
    private List<Double> dropsetWeightsLegacy;

    @SerializedName("dropsetRepsList")
    private List<Integer> dropsetRepsLegacy;

    @SerializedName("restTime")
    private Integer restTimeLegacy;

    public SessionSetResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSessionExerciseId() { return sessionExerciseId; }
    public void setSessionExerciseId(Long sessionExerciseId) { this.sessionExerciseId = sessionExerciseId; }

    public Integer getSetOrder() { return setOrder != null ? setOrder : (setNumber != null ? setNumber : 0); }
    public void setSetOrder(Integer setOrder) { this.setOrder = setOrder; }

    public Integer getSetNumber() { return setNumber; }
    public void setSetNumber(Integer setNumber) { this.setNumber = setNumber; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Integer getReps() { return reps; }
    public void setReps(Integer reps) { this.reps = reps; }

    public Integer getDurationSeconds() { return durationSeconds != null ? durationSeconds : timeLegacy; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }

    public Integer getTime() { return timeLegacy != null ? timeLegacy : durationSeconds; }
    public void setTime(Integer time) { this.timeLegacy = time; }

    public Double getDistanceMeters() { return distanceMeters != null ? distanceMeters : distanceLegacy; }
    public void setDistanceMeters(Double distanceMeters) { this.distanceMeters = distanceMeters; }

    public Double getDistance() { return distanceLegacy != null ? distanceLegacy : distanceMeters; }
    public void setDistance(Double distance) { this.distanceLegacy = distance; }

    public Integer getRpe() { return rpe; }
    public void setRpe(Integer rpe) { this.rpe = rpe; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean isWarmup() { return isWarmup != null && isWarmup; }
    public void setIsWarmup(Boolean isWarmup) { this.isWarmup = isWarmup; }

    public Boolean isDropset() { return isDropset != null && isDropset; }
    public void setIsDropset(Boolean isDropset) { this.isDropset = isDropset; }

    public Double getDropsetWeight() { return dropsetWeight; }
    public void setDropsetWeight(Double dropsetWeight) { this.dropsetWeight = dropsetWeight; }

    public Integer getDropsetReps() { return dropsetReps; }
    public void setDropsetReps(Integer dropsetReps) { this.dropsetReps = dropsetReps; }

    public List<Double> getDropsetWeights() { return dropsetWeightsLegacy; }
    public void setDropsetWeights(List<Double> dropsetWeights) { this.dropsetWeightsLegacy = dropsetWeights; }

    public List<Integer> getDropsetRepsList() { return dropsetRepsLegacy; }
    public void setDropsetRepsList(List<Integer> dropsetReps) { this.dropsetRepsLegacy = dropsetReps; }

    public Integer getRestTime() { return restTimeLegacy; }
    public void setRestTime(Integer restTime) { this.restTimeLegacy = restTime; }
}
