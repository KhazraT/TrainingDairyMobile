package ru.squidory.trainingdairymobile.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.io.IOException;
import java.security.GeneralSecurityException;
import ru.squidory.trainingdairymobile.App;

public class PreferencesManager {

    private final SharedPreferences sharedPreferences;

    private static final String PREF_ACCESS_TOKEN = "access_token";
    private static final String PREF_REFRESH_TOKEN = "refresh_token";
    private static final String PREF_USER_ID = "user_id";
    private static final String PREF_USER_EMAIL = "user_email";

    // Настройки приложения
    private static final String PREF_WEIGHT_UNIT = "weight_unit";
    private static final String PREF_LENGTH_UNIT = "length_unit";
    private static final String PREF_DISTANCE_UNIT = "distance_unit";
    private static final String PREF_THEME = "theme";
    private static final String PREF_LANGUAGE = "language";

    private static PreferencesManager instance;

    private PreferencesManager(Context context) {
        this.sharedPreferences = createEncryptedSharedPreferences(context);
    }

    public static synchronized PreferencesManager getInstance() {
        if (instance == null) {
            instance = new PreferencesManager(App.getInstance());
        }
        return instance;
    }

    private SharedPreferences createEncryptedSharedPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    "training_diary_secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular SharedPreferences if encryption fails
            return context.getSharedPreferences("training_diary_prefs", Context.MODE_PRIVATE);
        }
    }

    public String getAccessToken() {
        return sharedPreferences.getString(PREF_ACCESS_TOKEN, "");
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(PREF_REFRESH_TOKEN, "");
    }

    public long getUserId() {
        return sharedPreferences.getLong(PREF_USER_ID, -1);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(PREF_USER_EMAIL, "");
    }

    public boolean isLoggedIn() {
        String token = getAccessToken();
        return token != null && !token.isEmpty();
    }

    public void saveTokens(String accessToken, String refreshToken) {
        sharedPreferences.edit()
                .putString(PREF_ACCESS_TOKEN, accessToken)
                .putString(PREF_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public void saveUserId(long userId) {
        sharedPreferences.edit()
                .putLong(PREF_USER_ID, userId)
                .apply();
    }

    public void saveUserEmail(String email) {
        sharedPreferences.edit()
                .putString(PREF_USER_EMAIL, email)
                .apply();
    }

    public void clearTokens() {
        sharedPreferences.edit()
                .remove(PREF_ACCESS_TOKEN)
                .remove(PREF_REFRESH_TOKEN)
                .apply();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }

    // ==================== Настройки приложения ====================

    public String getWeightUnit() {
        return sharedPreferences.getString(PREF_WEIGHT_UNIT, "kg");
    }

    public void setWeightUnit(String unit) {
        sharedPreferences.edit().putString(PREF_WEIGHT_UNIT, unit).apply();
    }

    public String getLengthUnit() {
        return sharedPreferences.getString(PREF_LENGTH_UNIT, "cm");
    }

    public void setLengthUnit(String unit) {
        sharedPreferences.edit().putString(PREF_LENGTH_UNIT, unit).apply();
    }

    public String getDistanceUnit() {
        return sharedPreferences.getString(PREF_DISTANCE_UNIT, "km");
    }

    public void setDistanceUnit(String unit) {
        sharedPreferences.edit().putString(PREF_DISTANCE_UNIT, unit).apply();
    }

    public String getTheme() {
        return sharedPreferences.getString(PREF_THEME, "light");
    }

    public void setTheme(String theme) {
        sharedPreferences.edit().putString(PREF_THEME, theme).apply();
    }

    public String getLanguage() {
        return sharedPreferences.getString(PREF_LANGUAGE, "ru");
    }

    public void setLanguage(String language) {
        sharedPreferences.edit().putString(PREF_LANGUAGE, language).apply();
    }
}
