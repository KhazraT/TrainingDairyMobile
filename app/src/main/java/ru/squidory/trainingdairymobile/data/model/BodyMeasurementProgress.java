package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Прогресс измерения тела за период.
 * GET /api/body-measurements/progress
 */
public class BodyMeasurementProgress {

    @SerializedName("measurementType")
    private String measurementType;

    @SerializedName("startValue")
    private Double startValue;

    @SerializedName("endValue")
    private Double endValue;

    @SerializedName("change")
    private Double change;

    @SerializedName("changePercent")
    private Double changePercent;

    @SerializedName("minValue")
    private Double minValue;

    @SerializedName("maxValue")
    private Double maxValue;

    @SerializedName("dataPoints")
    private List<DataPoint> dataPoints;

    public BodyMeasurementProgress() {}

    public String getMeasurementType() { return measurementType; }
    public void setMeasurementType(String measurementType) { this.measurementType = measurementType; }

    public Double getStartValue() { return startValue; }
    public void setStartValue(Double startValue) { this.startValue = startValue; }

    public Double getEndValue() { return endValue; }
    public void setEndValue(Double endValue) { this.endValue = endValue; }

    public Double getChange() { return change; }
    public void setChange(Double change) { this.change = change; }

    public Double getChangePercent() { return changePercent; }
    public void setChangePercent(Double changePercent) { this.changePercent = changePercent; }

    public Double getMinValue() { return minValue; }
    public void setMinValue(Double minValue) { this.minValue = minValue; }

    public Double getMaxValue() { return maxValue; }
    public void setMaxValue(Double maxValue) { this.maxValue = maxValue; }

    public List<DataPoint> getDataPoints() { return dataPoints; }
    public void setDataPoints(List<DataPoint> dataPoints) { this.dataPoints = dataPoints; }

    public static class DataPoint {
        @SerializedName("date")
        private String date;

        @SerializedName("value")
        private Double value;

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public Double getValue() { return value; }
        public void setValue(Double value) { this.value = value; }
    }
}
