package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class WorkoutStatsResponse {

    @SerializedName("totalWorkouts")
    private Integer totalWorkouts;

    @SerializedName("period")
    private String period;

    @SerializedName("count")
    private Integer count;

    public WorkoutStatsResponse() {}

    public Integer getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(Integer totalWorkouts) { this.totalWorkouts = totalWorkouts; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
