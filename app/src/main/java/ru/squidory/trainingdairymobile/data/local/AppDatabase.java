package ru.squidory.trainingdairymobile.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import ru.squidory.trainingdairymobile.data.local.dao.ActiveSessionDao;
import ru.squidory.trainingdairymobile.data.local.dao.ExerciseDao;
import ru.squidory.trainingdairymobile.data.local.dao.ProgramDao;
import ru.squidory.trainingdairymobile.data.local.dao.SessionDao;
import ru.squidory.trainingdairymobile.data.local.dao.UserDao;
import ru.squidory.trainingdairymobile.data.local.entity.ActiveSessionEntity;
import ru.squidory.trainingdairymobile.data.local.entity.ExerciseEntity;
import ru.squidory.trainingdairymobile.data.local.entity.ProgramEntity;
import ru.squidory.trainingdairymobile.data.local.entity.SessionEntity;
import ru.squidory.trainingdairymobile.data.local.entity.UserEntity;
import ru.squidory.trainingdairymobile.util.Converters;

@Database(
    entities = {
        ExerciseEntity.class,
        ProgramEntity.class,
        SessionEntity.class,
        ActiveSessionEntity.class,
        UserEntity.class
    },
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ExerciseDao exerciseDao();

    public abstract ProgramDao programDao();

    public abstract SessionDao sessionDao();

    public abstract ActiveSessionDao activeSessionDao();

    public abstract UserDao userDao();
}
