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
}
