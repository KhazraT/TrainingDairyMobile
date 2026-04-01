package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ExerciseRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("video")
    private String video;

    @SerializedName("technique")
    private String technique;

    @SerializedName("exerciseType")
    private String exerciseType;

    @SerializedName("muscleGroupIds")
    private List<Long> muscleGroupIds;

    @SerializedName("equipmentIds")
    private List<Long> equipmentIds;

    public ExerciseRequest() {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVideo() { return video; }
    public void setVideo(String video) { this.video = video; }

    public String getTechnique() { return technique; }
    public void setTechnique(String technique) { this.technique = technique; }

    public String getExerciseType() { return exerciseType; }
    public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }

    public List<Long> getMuscleGroupIds() { return muscleGroupIds; }
    public void setMuscleGroupIds(List<Long> muscleGroupIds) { this.muscleGroupIds = muscleGroupIds; }

    public List<Long> getEquipmentIds() { return equipmentIds; }
    public void setEquipmentIds(List<Long> equipmentIds) { this.equipmentIds = equipmentIds; }
}
