package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class ProgramResponse {

    @SerializedName("id")
    private long id;

    @SerializedName("userId")
    private Long userId;  // Long (может быть null для публичных программ)

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("isPublic")
    private Boolean isPublic;

    @SerializedName("createdAt")
    private Date createdAt;

    public ProgramResponse() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public Long getUserId() { return userId; }  // Возвращает Long (может быть null)
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
