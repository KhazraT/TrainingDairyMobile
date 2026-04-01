package ru.squidory.trainingdairymobile.util;

public class Constants {

    // Базовый URL API
    // ============================================================================
    // ВАЖНО: Выберите правильный URL в зависимости от способа подключения:
    //
    // 1. Для физического устройства с adb reverse:
    //    adb reverse tcp:8080 tcp:8080
    //    Используйте: http://localhost:8080/api/
    //
    // 2. Для эмулятора Android:
    //    Используйте: http://10.0.2.2:8080/api/
    //    (10.0.2.2 - это специальный alias для localhost хост-машины в эмуляторе)
    //
    // 3. Для подключения к реальному серверу:
    //    Используйте: http://<IP-адрес-сервера>:8080/api/
    //    (убедитесь, что сервер доступен из сети)
    // ============================================================================
    public static final String BASE_URL = "http://localhost:8080/api/";

    // Preferences
    public static final String PREFS_NAME = "training_diary_prefs";
    public static final String PREF_ACCESS_TOKEN = "access_token";
    public static final String PREF_REFRESH_TOKEN = "refresh_token";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_EMAIL = "user_email";

    // Настройки
    public static final String SETTING_LENGTH_UNIT = "length_unit";
    public static final String SETTING_WEIGHT_UNIT = "weight_unit";
    public static final String SETTING_DISTANCE_UNIT = "distance_unit";
    public static final String SETTING_THEME = "theme";
    public static final String SETTING_LANGUAGE = "language";

    // Значения по умолчанию
    public static final String DEFAULT_LENGTH_UNIT = "cm";
    public static final String DEFAULT_WEIGHT_UNIT = "kg";
    public static final String DEFAULT_DISTANCE_UNIT = "km";
    public static final String DEFAULT_THEME = "light";
    public static final String DEFAULT_LANGUAGE = "ru";

    // Типы упражнений
    public static final String EXERCISE_TYPE_REPS_WEIGHT = "reps_weight";
    public static final String EXERCISE_TYPE_TIME_WEIGHT = "time_weight";
    public static final String EXERCISE_TYPE_TIME_DISTANCE = "time_distance";
    public static final String EXERCISE_TYPE_TIME_WEIGHT_DISTANCE = "time_weight_distance";

    // Типы подходов
    public static final String SET_TYPE_REGULAR = "regular";
    public static final String SET_TYPE_DROPSET = "dropset";
    public static final String SET_TYPE_MYOREPS = "myoreps";

    // Типы измерений тела
    public static final String MEASUREMENT_BODY_WEIGHT = "BODY_WEIGHT";
    public static final String MEASUREMENT_CHEST = "CHEST";
    public static final String MEASUREMENT_WAIST = "WAIST";
    public static final String MEASUREMENT_HIPS = "HIPS";
    public static final String MEASUREMENT_ARM = "ARM";
    public static final String MEASUREMENT_THIGH = "THIGH";
    public static final String MEASUREMENT_CALF = "CALF";
    public static final String MEASUREMENT_NECK = "NECK";
    public static final String MEASUREMENT_SHOULDERS = "SHOULDERS";
    public static final String MEASUREMENT_BODY_FAT_PERCENTAGE = "BODY_FAT_PERCENTAGE";
    public static final String MEASUREMENT_MUSCLE_MASS = "MUSCLE_MASS";
    public static final String MEASUREMENT_BMI = "BMI";

    // Категории статей
    public static final String ARTICLE_CATEGORY_TECHNIQUE = "TECHNIQUE";
    public static final String ARTICLE_CATEGORY_THEORY = "THEORY";
    public static final String ARTICLE_CATEGORY_NUTRITION = "NUTRITION";
    public static final String ARTICLE_CATEGORY_RECOVERY = "RECOVERY";
    public static final String ARTICLE_CATEGORY_PROGRAMMING = "PROGRAMMING";
    public static final String ARTICLE_CATEGORY_FAQ = "FAQ";

    // Пол
    public static final String GENDER_MALE = "MALE";
    public static final String GENDER_FEMALE = "FEMALE";
    public static final String GENDER_OTHER = "OTHER";

    // Timeout для API запросов (в секундах)
    public static final int API_TIMEOUT_SECONDS = 30;

    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 20;
}
