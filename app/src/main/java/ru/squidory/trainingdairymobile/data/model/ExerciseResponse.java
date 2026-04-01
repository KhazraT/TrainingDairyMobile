package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class ExerciseResponse {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("video")
    private String video;

    @SerializedName("photo")
    private String photo;

    @SerializedName("technique")
    private String technique;

    @SerializedName("muscleGroups")
    private List<MuscleGroupResponse> muscleGroups;

    @SerializedName("equipment")
    private List<EquipmentResponse> equipment;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("exerciseType")
    private String exerciseType;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("isCustom")
    private Boolean isCustom;

    public ExerciseResponse() {}

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVideo() { return video; }
    public void setVideo(String video) { this.video = video; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getTechnique() { return technique; }
    public void setTechnique(String technique) { this.technique = technique; }

    public List<MuscleGroupResponse> getMuscleGroups() { return muscleGroups; }
    public void setMuscleGroups(List<MuscleGroupResponse> muscleGroups) { this.muscleGroups = muscleGroups; }

    public List<EquipmentResponse> getEquipment() { return equipment; }
    public void setEquipment(List<EquipmentResponse> equipment) { this.equipment = equipment; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getExerciseType() { return exerciseType; }
    public void setExerciseType(String exerciseType) { this.exerciseType = exerciseType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Boolean getIsCustom() { return isCustom; }
    public void setIsCustom(Boolean isCustom) { this.isCustom = isCustom; }
}
