package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class StatisticsResponse {

    @SerializedName("id")
    private long id;

    @SerializedName("userId")
    private long userId;

    @SerializedName("date")
    private String date;

    @SerializedName("measurementType")
    private String measurementType;

    @SerializedName("unit")
    private String unit;

    @SerializedName("value")
    private Double value;

    public StatisticsResponse() {}

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMeasurementType() { return measurementType; }
    public void setMeasurementType(String measurementType) { this.measurementType = measurementType; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
