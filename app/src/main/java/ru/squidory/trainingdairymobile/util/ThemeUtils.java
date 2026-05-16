package ru.squidory.trainingdairymobile.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;

import androidx.core.content.ContextCompat;

import ru.squidory.trainingdairymobile.R;

public class ThemeUtils {

    public static boolean isDarkMode(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int getChartTextColor(Context context) {
        return isDarkMode(context) ? Color.WHITE : Color.DKGRAY;
    }

    public static int getChartAxisTextColor(Context context) {
        return isDarkMode(context) ? Color.LTGRAY : Color.DKGRAY;
    }

    public static int getChartGridColor(Context context) {
        return isDarkMode(context) ? Color.argb(50, 255, 255, 255) : Color.argb(50, 0, 0, 0);
    }

    public static int getChartBackgroundColor(Context context) {
        return isDarkMode(context) ? Color.TRANSPARENT : Color.TRANSPARENT;
    }

    public static int getProgramDescriptionColor(Context context) {
        return isDarkMode(context) ? Color.LTGRAY : ContextCompat.getColor(context, android.R.color.secondary_text_light);
    }

    public static int getProgramDescriptionPlaceholderColor(Context context) {
        return isDarkMode(context) ? Color.argb(255, 120, 120, 120) : ContextCompat.getColor(context, android.R.color.darker_gray);
    }

    public static int getCustomChartDateLabelColor(Context context) {
        return isDarkMode(context) ? Color.LTGRAY : Color.GRAY;
    }
}
