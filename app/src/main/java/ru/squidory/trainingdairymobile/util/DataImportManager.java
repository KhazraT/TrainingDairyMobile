package ru.squidory.trainingdairymobile.util;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.room.Room;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.squidory.trainingdairymobile.data.local.DatabaseClient;
import ru.squidory.trainingdairymobile.data.local.entity.ExerciseEntity;
import ru.squidory.trainingdairymobile.data.local.entity.ProgramEntity;
import ru.squidory.trainingdairymobile.data.local.entity.SessionEntity;
import ru.squidory.trainingdairymobile.data.local.entity.UserEntity;
import ru.squidory.trainingdairymobile.data.local.PreferencesManager;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Менеджер для импорта данных пользователя из JSON формата.
 */
public class DataImportManager {

    private static final String TAG = "DataImportManager";
    private static final Gson gson = new GsonBuilder().create();

    /**
     * Импортирует данные из JSON файла.
     * @param context Контекст приложения
     * @param fileUri URI файла для импорта
     * @return true если импорт успешен, false при ошибке
     */
    public static boolean importDataFromUri(Context context, Uri fileUri) {
        try (InputStream is = context.getContentResolver().openInputStream(fileUri);
             InputStreamReader reader = new InputStreamReader(is)) {

            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> importData = gson.fromJson(reader, type);

            return importDataFromMap(context, importData);

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при импорте данных из URI", e);
            return false;
        }
    }

    /**
     * Импортирует данные из JSON файла (по пути к файлу).
     */
    public static boolean importDataFromFile(Context context, String filePath) {
        try (FileReader reader = new FileReader(filePath)) {

            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> importData = gson.fromJson(reader, type);

            return importDataFromMap(context, importData);

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при импорте данных из файла", e);
            return false;
        }
    }

    /**
     * Импортирует данные из Map (парсинг JSON).
     */
    private static boolean importDataFromMap(Context context, Map<String, Object> importData) {
        try {
            // Импорт пользователя
            if (importData.containsKey("user")) {
                UserEntity user = gson.fromJson(gson.toJson(importData.get("user")), UserEntity.class);
                if (user != null) {
                    DatabaseClient.getUserDao().insert(user);
                }
            }

            // Импорт пользовательских упражнений
            if (importData.containsKey("customExercises")) {
                Type listType = new TypeToken<List<ExerciseEntity>>(){}.getType();
                List<ExerciseEntity> exercises = gson.fromJson(gson.toJson(importData.get("customExercises")), listType);
                if (exercises != null && !exercises.isEmpty()) {
                    DatabaseClient.getExerciseDao().insertAll(exercises);
                }
            }

            // Импорт программ
            if (importData.containsKey("programs")) {
                Type listType = new TypeToken<List<ProgramEntity>>(){}.getType();
                List<ProgramEntity> programs = gson.fromJson(gson.toJson(importData.get("programs")), listType);
                if (programs != null && !programs.isEmpty()) {
                    DatabaseClient.getProgramDao().insertAll(programs);
                }
            }

            // Импорт сессий
            if (importData.containsKey("sessions")) {
                Type listType = new TypeToken<List<SessionEntity>>(){}.getType();
                List<SessionEntity> sessions = gson.fromJson(gson.toJson(importData.get("sessions")), listType);
                if (sessions != null && !sessions.isEmpty()) {
                    DatabaseClient.getSessionDao().insertAll(sessions);
                }
            }

            // Импорт настроек
            if (importData.containsKey("settings")) {
                Map<String, Object> settings = (Map<String, Object>) importData.get("settings");
                PreferencesManager prefs = PreferencesManager.getInstance();

                if (settings.containsKey("weightUnit")) {
                    prefs.setWeightUnit((String) settings.get("weightUnit"));
                }
                if (settings.containsKey("lengthUnit")) {
                    prefs.setLengthUnit((String) settings.get("lengthUnit"));
                }
                if (settings.containsKey("distanceUnit")) {
                    prefs.setDistanceUnit((String) settings.get("distanceUnit"));
                }
                if (settings.containsKey("theme")) {
                    prefs.setTheme((String) settings.get("theme"));
                }
                if (settings.containsKey("language")) {
                    prefs.setLanguage((String) settings.get("language"));
                }
            }

            Log.d(TAG, "Данные успешно импортированы");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Ошибка при импорте данных", e);
            return false;
        }
    }
}
