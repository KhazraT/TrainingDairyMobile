package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Общая сводка статистики пользователя.
 * GET /api/stats/summary
 */
public class StatsSummaryResponse {

    @SerializedName("totalWorkouts")
    private Integer totalWorkouts;

    @SerializedName("totalSessions")
    private Integer totalSessions;

    @SerializedName("totalVolume")
    private Double totalVolume;

    @SerializedName("averageSessionDuration")
    private Integer averageSessionDuration; // в секундах

    @SerializedName("favoriteExercise")
    private String favoriteExercise;

    @SerializedName("lastWorkoutDate")
    private String lastWorkoutDate;

    public StatsSummaryResponse() {}

    public Integer getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(Integer totalWorkouts) { this.totalWorkouts = totalWorkouts; }

    public Integer getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Integer totalSessions) { this.totalSessions = totalSessions; }

    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }

    public Integer getAverageSessionDuration() { return averageSessionDuration; }
    public void setAverageSessionDuration(Integer averageSessionDuration) { this.averageSessionDuration = averageSessionDuration; }

    public String getFavoriteExercise() { return favoriteExercise; }
    public void setFavoriteExercise(String favoriteExercise) { this.favoriteExercise = favoriteExercise; }

    public String getLastWorkoutDate() { return lastWorkoutDate; }
    public void setLastWorkoutDate(String lastWorkoutDate) { this.lastWorkoutDate = lastWorkoutDate; }
}
