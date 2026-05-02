package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * DTO for exercise sessions history response.
 * Contains all sessions where the exercise was performed, with all sets data.
 */
public class ExerciseSessionsHistoryResponse {
    @SerializedName("sessionId")
    private Long sessionId;
    
    @SerializedName("sessionDate")
    private String sessionDate; // ISO format
    
    @SerializedName("sets")
    private List<SessionSetHistoryResponse> sets;

    public ExerciseSessionsHistoryResponse() {
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(String sessionDate) {
        this.sessionDate = sessionDate;
    }

    public List<SessionSetHistoryResponse> getSets() {
        return sets;
    }

    public void setSets(List<SessionSetHistoryResponse> sets) {
        this.sets = sets;
    }
}
