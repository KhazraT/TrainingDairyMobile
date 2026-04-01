package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class ProgramRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("isPublic")
    private Boolean isPublic;

    public ProgramRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
}
