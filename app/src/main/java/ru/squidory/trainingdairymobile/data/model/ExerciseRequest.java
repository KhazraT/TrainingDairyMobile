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

    @SerializedName("muscles")
    private List<MuscleRequest> muscles;

    @SerializedName("equipmentIds")
    private List<Long> equipmentIds;

    // Вложенный класс для мышцы с типом
    public static class MuscleRequest {
        @SerializedName("muscleGroupId")
        private Long muscleGroupId;

        @SerializedName("isPrimary")
        private Boolean isPrimary;

        public MuscleRequest(Long muscleGroupId, Boolean isPrimary) {
            this.muscleGroupId = muscleGroupId;
            this.isPrimary = isPrimary;
        }

        public Long getMuscleGroupId() { return muscleGroupId; }
        public void setMuscleGroupId(Long muscleGroupId) { this.muscleGroupId = muscleGroupId; }

        public Boolean getIsPrimary() { return isPrimary; }
        public void setIsPrimary(Boolean isPrimary) { this.isPrimary = isPrimary; }
    }

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

    public List<MuscleRequest> getMuscles() { return muscles; }
    public void setMuscles(List<MuscleRequest> muscles) { this.muscles = muscles; }

    public List<Long> getEquipmentIds() { return equipmentIds; }
    public void setEquipmentIds(List<Long> equipmentIds) { this.equipmentIds = equipmentIds; }
}
