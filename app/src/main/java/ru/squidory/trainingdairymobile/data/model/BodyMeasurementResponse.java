package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Измерение тела.
 * GET /api/body-measurements
 */
public class BodyMeasurementResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("measurementType")
    private String measurementType;

    @SerializedName("value")
    private Double value;

    @SerializedName("valueUnit")
    private String valueUnit;

    @SerializedName("measuredAt")
    private String measuredAt;

    @SerializedName("notes")
    private String notes;

    @SerializedName("createdAt")
    private String createdAt;

    public BodyMeasurementResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMeasurementType() { return measurementType; }
    public void setMeasurementType(String measurementType) { this.measurementType = measurementType; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }

    public String getValueUnit() { return valueUnit; }
    public void setValueUnit(String valueUnit) { this.valueUnit = valueUnit; }

    public String getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(String measuredAt) { this.measuredAt = measuredAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Constants for measurement types
    public static final String BODY_WEIGHT = "BODY_WEIGHT";
    public static final String BODY_FAT_PERCENTAGE = "BODY_FAT_PERCENTAGE";
    public static final String CHEST_CIRCUMFERENCE = "CHEST_CIRCUMFERENCE";
    public static final String WAIST_CIRCUMFERENCE = "WAIST_CIRCUMFERENCE";
    public static final String HIP_CIRCUMFERENCE = "HIP_CIRCUMFERENCE";
    public static final String ARM_CIRCUMFERENCE = "ARM_CIRCUMFERENCE";
    public static final String THIGH_CIRCUMFERENCE = "THIGH_CIRCUMFERENCE";
    public static final String CALF_CIRCUMFERENCE = "CALF_CIRCUMFERENCE";
    public static final String NECK_CIRCUMFERENCE = "NECK_CIRCUMFERENCE";
    public static final String SHOULDER_CIRCUMFERENCE = "SHOULDER_CIRCUMFERENCE";
}
