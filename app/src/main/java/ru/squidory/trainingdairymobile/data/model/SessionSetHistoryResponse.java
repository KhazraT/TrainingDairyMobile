package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for individual set data in exercise sessions history.
 */
public class SessionSetHistoryResponse {
    @SerializedName("setOrder")
    private Integer setOrder;
    
    @SerializedName("weight")
    private Double weight;
    
    @SerializedName("reps")
    private Integer reps;
    
    @SerializedName("durationSeconds")
    private Integer durationSeconds;
    
    @SerializedName("distanceMeters")
    private Double distanceMeters;
    
    @SerializedName("isDropset")
    private Boolean isDropset;
    
    @SerializedName("dropsetWeight")
    private Double dropsetWeight;
    
    @SerializedName("dropsetReps")
    private Integer dropsetReps;

    public SessionSetHistoryResponse() {
    }

    public Integer getSetOrder() {
        return setOrder;
    }

    public void setSetOrder(Integer setOrder) {
        this.setOrder = setOrder;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    public Boolean getIsDropset() {
        return isDropset;
    }

    public void setIsDropset(Boolean dropset) {
        this.isDropset = dropset;
    }

    public Double getDropsetWeight() {
        return dropsetWeight;
    }

    public void setDropsetWeight(Double dropsetWeight) {
        this.dropsetWeight = dropsetWeight;
    }

    public Integer getDropsetReps() {
        return dropsetReps;
    }

    public void setDropsetReps(Integer dropsetReps) {
        this.dropsetReps = dropsetReps;
    }
}
