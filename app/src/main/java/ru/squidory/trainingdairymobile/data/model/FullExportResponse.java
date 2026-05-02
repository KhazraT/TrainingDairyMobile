package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Полный экспорт данных пользователя.
 * Используется для бэкапа и последующего импорта на другой аккаунт.
 */
public class FullExportResponse {

    @SerializedName("version")
    public String version;

    @SerializedName("exportedAt")
    public String exportedAt;

    @SerializedName("user")
    public UserData user;

    @SerializedName("exercises")
    public List<ExerciseExportData> exercises;

    @SerializedName("programs")
    public List<ProgramExportData> programs;

    @SerializedName("sessions")
    public List<SessionExportData> sessions;

    // --- Вложенные классы ---

    public static class UserData {
        @SerializedName("email")
        public String email;
        @SerializedName("name")
        public String name;
    }

    public static class ExerciseExportData {
        @SerializedName("id")
        public long id;
        @SerializedName("name")
        public String name;
        @SerializedName("description")
        public String description;
        @SerializedName("type")
        public String type;
        // ... другие поля упражнения
    }

    public static class ProgramExportData {
        @SerializedName("id")
        public long id;
        @SerializedName("name")
        public String name;
        @SerializedName("description")
        public String description;
        @SerializedName("workouts")
        public List<WorkoutExportData> workouts;
    }

    public static class WorkoutExportData {
        @SerializedName("id")
        public long id;
        @SerializedName("name")
        public String name;
        @SerializedName("comment")
        public String comment;
        @SerializedName("workoutOrder")
        public int workoutOrder;
        @SerializedName("exercises")
        public List<WorkoutExerciseExportData> exercises;
    }

    public static class WorkoutExerciseExportData {
        @SerializedName("id")
        public long id;
        @SerializedName("exerciseId")
        public long exerciseId;
        @SerializedName("exerciseOrder")
        public int exerciseOrder;
        @SerializedName("supersetGroupNumber")
        public Integer supersetGroupNumber;
        @SerializedName("plannedSets")
        public List<PlannedSetExportData> plannedSets;
    }

    public static class PlannedSetExportData {
        @SerializedName("setOrder")
        public int setOrder;
        @SerializedName("weight")
        public Float weight;
        @SerializedName("reps")
        public Integer reps;
        @SerializedName("isDropset")
        public boolean isDropset;
        // ... другие поля подхода
    }

    public static class SessionExportData {
        @SerializedName("id")
        public long id;
        @SerializedName("workoutId")
        public long workoutId; // Старый ID для маппинга при импорте
        @SerializedName("completedAt")
        public String completedAt;
        @SerializedName("durationMinutes")
        public int durationMinutes;
        @SerializedName("exercises")
        public List<SessionExerciseExportData> exercises;
    }

    public static class SessionExerciseExportData {
        @SerializedName("exerciseId")
        public long exerciseId;
        @SerializedName("completedSets")
        public List<SessionSetExportData> completedSets;
    }

    public static class SessionSetExportData {
        @SerializedName("setOrder")
        public int setOrder;
        @SerializedName("weight")
        public Float weight;
        @SerializedName("reps")
        public Integer reps;
        @SerializedName("durationSeconds")
        public Integer durationSeconds;
        @SerializedName("isDropset")
        public boolean isDropset;
    }
}
