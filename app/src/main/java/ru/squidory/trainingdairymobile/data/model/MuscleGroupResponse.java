package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class MuscleGroupResponse {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    public MuscleGroupResponse() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
