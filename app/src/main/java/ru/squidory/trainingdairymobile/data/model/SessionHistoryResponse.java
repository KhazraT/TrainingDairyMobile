package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Ответ API истории сессий, сгруппированных по дням.
 * GET /api/sessions/history
 */
public class SessionHistoryResponse {

    @SerializedName("sessionHistory")
    private List<DaySessions> sessionHistory;

    @SerializedName("pagination")
    private PaginationInfo pagination;

    @SerializedName("summary")
    private HistorySummary summary;

    public SessionHistoryResponse() {}

    public List<DaySessions> getSessionHistory() {
        return sessionHistory;
    }

    public void setSessionHistory(List<DaySessions> sessionHistory) {
        this.sessionHistory = sessionHistory;
    }

    public PaginationInfo getPagination() {
        return pagination;
    }

    public void setPagination(PaginationInfo pagination) {
        this.pagination = pagination;
    }

    public HistorySummary getSummary() {
        return summary;
    }

    public void setSummary(HistorySummary summary) {
        this.summary = summary;
    }

    /**
     * Сессии за один день
     */
    public static class DaySessions {
        @SerializedName("date")
        private String date; // "YYYY-MM-DD"

        @SerializedName("sessions")
        private List<SessionInHistory> sessions;

        public DaySessions() {}

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public List<SessionInHistory> getSessions() {
            return sessions;
        }

        public void setSessions(List<SessionInHistory> sessions) {
            this.sessions = sessions;
        }
    }

    /**
     * Информация о сессии в истории
     */
    public static class SessionInHistory {
        @SerializedName("sessionId")
        private Long sessionId;

        @SerializedName("workoutId")
        private Long workoutId;

        @SerializedName("workoutName")
        private String workoutName;

        @SerializedName("programName")
        private String programName;

        @SerializedName("startedAt")
        private String startedAt; // ISO 8601 string

        @SerializedName("completedAt")
        private String completedAt; // ISO 8601 string

        @SerializedName("durationMinutes")
        private Integer durationMinutes;

        @SerializedName("totalTonnage")
        private Double totalTonnage;

        @SerializedName("totalSets")
        private Integer totalSets;

        @SerializedName("exercises")
        private List<ExerciseInHistory> exercises;

        public SessionInHistory() {}

        public Long getSessionId() {
            return sessionId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }

        public Long getWorkoutId() {
            return workoutId;
        }

        public void setWorkoutId(Long workoutId) {
            this.workoutId = workoutId;
        }

        public String getWorkoutName() {
            return workoutName;
        }

        public void setWorkoutName(String workoutName) {
            this.workoutName = workoutName;
        }

        public String getProgramName() {
            return programName;
        }

        public void setProgramName(String programName) {
            this.programName = programName;
        }

        public String getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(String startedAt) {
            this.startedAt = startedAt;
        }

        public String getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(String completedAt) {
            this.completedAt = completedAt;
        }

        public Integer getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
        }

        public Double getTotalTonnage() {
            return totalTonnage;
        }

        public void setTotalTonnage(Double totalTonnage) {
            this.totalTonnage = totalTonnage;
        }

        public Integer getTotalSets() {
            return totalSets;
        }

        public void setTotalSets(Integer totalSets) {
            this.totalSets = totalSets;
        }

        public List<ExerciseInHistory> getExercises() {
            return exercises;
        }

        public void setExercises(List<ExerciseInHistory> exercises) {
            this.exercises = exercises;
        }
    }

    /**
     * Упражнение в истории
     */
    public static class ExerciseInHistory {
        @SerializedName("exerciseId")
        private Long exerciseId;

        @SerializedName("exerciseName")
        private String exerciseName;

        @SerializedName("exerciseType")
        private String exerciseType;

        @SerializedName("sets")
        private List<SetInHistory> sets;

        public ExerciseInHistory() {}

        public Long getExerciseId() {
            return exerciseId;
        }

        public void setExerciseId(Long exerciseId) {
            this.exerciseId = exerciseId;
        }

        public String getExerciseName() {
            return exerciseName;
        }

        public void setExerciseName(String exerciseName) {
            this.exerciseName = exerciseName;
        }

        public String getExerciseType() {
            return exerciseType;
        }

        public void setExerciseType(String exerciseType) {
            this.exerciseType = exerciseType;
        }

        public List<SetInHistory> getSets() {
            return sets;
        }

        public void setSets(List<SetInHistory> sets) {
            this.sets = sets;
        }
    }

    /**
     * Подход в истории
     */
    public static class SetInHistory {
        @SerializedName("setNumber")
        private Integer setNumber;

        @SerializedName("weight")
        private Double weight;

        @SerializedName("reps")
        private Integer reps;

        @SerializedName("durationSeconds")
        private Integer durationSeconds;

        @SerializedName("distanceMeters")
        private Double distanceMeters;

        @SerializedName("isWarmup")
        private Boolean isWarmup;

        @SerializedName("isDropset")
        private Boolean isDropset;

        public SetInHistory() {}

        public Integer getSetNumber() {
            return setNumber;
        }

        public void setSetNumber(Integer setNumber) {
            this.setNumber = setNumber;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        public Integer getReps() {
            return reps;
        }

        public void setReps(Integer reps) {
            this.reps = reps;
        }

        public Integer getDurationSeconds() {
            return durationSeconds;
        }

        public void setDurationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
        }

        public Double getDistanceMeters() {
            return distanceMeters;
        }

        public void setDistanceMeters(Double distanceMeters) {
            this.distanceMeters = distanceMeters;
        }

        public Boolean getIsWarmup() {
            return isWarmup;
        }

        public void setIsWarmup(Boolean isWarmup) {
            this.isWarmup = isWarmup;
        }

        public Boolean getIsDropset() {
            return isDropset;
        }

        public void setIsDropset(Boolean isDropset) {
            this.isDropset = isDropset;
        }
    }

    /**
     * Информация пагинации
     */
    public static class PaginationInfo {
        @SerializedName("currentPage")
        private Integer currentPage;

        @SerializedName("pageSize")
        private Integer pageSize;

        @SerializedName("totalElements")
        private Long totalElements;

        @SerializedName("totalPages")
        private Integer totalPages;

        @SerializedName("hasNext")
        private Boolean hasNext;

        @SerializedName("hasPrevious")
        private Boolean hasPrevious;

        public PaginationInfo() {}

        public Integer getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(Integer currentPage) {
            this.currentPage = currentPage;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }

        public Long getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(Long totalElements) {
            this.totalElements = totalElements;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public Boolean getHasNext() {
            return hasNext;
        }

        public void setHasNext(Boolean hasNext) {
            this.hasNext = hasNext;
        }

        public Boolean getHasPrevious() {
            return hasPrevious;
        }

        public void setHasPrevious(Boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
    }

    /**
     * Общая статистика истории
     */
    public static class HistorySummary {
        @SerializedName("totalSessions")
        private Long totalSessions;

        @SerializedName("totalDurationMinutes")
        private Long totalDurationMinutes;

        @SerializedName("totalTonnage")
        private Double totalTonnage;

        @SerializedName("averageSessionsPerDay")
        private Double averageSessionsPerDay;

        public HistorySummary() {}

        public Long getTotalSessions() {
            return totalSessions;
        }

        public void setTotalSessions(Long totalSessions) {
            this.totalSessions = totalSessions;
        }

        public Long getTotalDurationMinutes() {
            return totalDurationMinutes;
        }

        public void setTotalDurationMinutes(Long totalDurationMinutes) {
            this.totalDurationMinutes = totalDurationMinutes;
        }

        public Double getTotalTonnage() {
            return totalTonnage;
        }

        public void setTotalTonnage(Double totalTonnage) {
            this.totalTonnage = totalTonnage;
        }

        public Double getAverageSessionsPerDay() {
            return averageSessionsPerDay;
        }

        public void setAverageSessionsPerDay(Double averageSessionsPerDay) {
            this.averageSessionsPerDay = averageSessionsPerDay;
        }
    }
}
