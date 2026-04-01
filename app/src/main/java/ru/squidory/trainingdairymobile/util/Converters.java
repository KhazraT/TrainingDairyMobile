package ru.squidory.trainingdairymobile.util;

import androidx.room.TypeConverter;
import java.util.Date;

public class Converters {

    @TypeConverter
    public static long fromTimestamp(Date value) {
        return value != null ? value.getTime() : -1;
    }

    @TypeConverter
    public static Date toTimestamp(long value) {
        return value != -1 ? new Date(value) : null;
    }
}
