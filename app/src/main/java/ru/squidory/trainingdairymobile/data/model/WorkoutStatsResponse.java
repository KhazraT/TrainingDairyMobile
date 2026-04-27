package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Статистика тренировок (общая сводка).
 * GET /api/stats/workouts
 */
public class WorkoutStatsResponse {

    @SerializedName("totalWorkouts")
    private Integer totalWorkouts;

    @SerializedName("completedWorkouts")
    private Integer completedWorkouts;

    @SerializedName("averagePerWeek")
    private Double averagePerWeek;

    public WorkoutStatsResponse() {}

    public Integer getTotalWorkouts() { return totalWorkouts; }
    public void setTotalWorkouts(Integer totalWorkouts) { this.totalWorkouts = totalWorkouts; }

    public Integer getCompletedWorkouts() { return completedWorkouts; }
    public void setCompletedWorkouts(Integer completedWorkouts) { this.completedWorkouts = completedWorkouts; }

    public Double getAveragePerWeek() { return averagePerWeek; }
    public void setAveragePerWeek(Double averagePerWeek) { this.averagePerWeek = averagePerWeek; }
}
