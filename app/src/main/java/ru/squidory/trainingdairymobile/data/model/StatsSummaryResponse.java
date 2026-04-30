package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Общая сводка статистики пользователя.
 * GET /api/stats/summary
 */
public class StatsSummaryResponse {

    @SerializedName("totalWorkouts")
    private Integer totalWorkouts;

    @SerializedName("totalVolume")
    private Double totalVolume;

    @SerializedName("favoriteExercise")
    private String favoriteExercise;

    @SerializedName("averageDurationMinutes")
    private Integer averageDurationMinutes;

    @SerializedName("totalSets")
    private Integer totalSets;

    @SerializedName("completionRate")
    private Double completionRate;

    public StatsSummaryResponse() {}

    public Integer getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(Integer totalWorkouts) { this.totalWorkouts = totalWorkouts; }

    public Double getTotalVolume() { return totalVolume; }
    public void setTotalVolume(Double totalVolume) { this.totalVolume = totalVolume; }

    public String getFavoriteExercise() { return favoriteExercise; }
    public void setFavoriteExercise(String favoriteExercise) { this.favoriteExercise = favoriteExercise; }

    public Integer getAverageDurationMinutes() { return averageDurationMinutes; }
    public void setAverageDurationMinutes(Integer averageDurationMinutes) { this.averageDurationMinutes = averageDurationMinutes; }

    public Integer getTotalSets() { return totalSets; }
    public void setTotalSets(Integer totalSets) { this.totalSets = totalSets; }

    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
}
