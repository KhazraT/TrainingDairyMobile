package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class WorkoutRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("comment")
    private String comment;

    @SerializedName("workoutOrder")
    private Integer workoutOrder;

    public WorkoutRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getWorkoutOrder() { return workoutOrder; }
    public void setWorkoutOrder(Integer workoutOrder) { this.workoutOrder = workoutOrder; }
}
