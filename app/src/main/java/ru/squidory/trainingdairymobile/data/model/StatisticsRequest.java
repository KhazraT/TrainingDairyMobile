package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class StatisticsRequest {

    @SerializedName("date")
    private String date;

    @SerializedName("measurementType")
    private String measurementType;

    @SerializedName("unit")
    private String unit;

    @SerializedName("value")
    private Double value;

    public StatisticsRequest() {}

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getMeasurementType() { return measurementType; }
    public void setMeasurementType(String measurementType) { this.measurementType = measurementType; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
}
