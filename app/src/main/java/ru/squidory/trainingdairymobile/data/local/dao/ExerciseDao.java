package ru.squidory.trainingdairymobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import ru.squidory.trainingdairymobile.data.local.entity.ExerciseEntity;

@Dao
public interface ExerciseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExerciseEntity exercise);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ExerciseEntity> exercises);

    @Delete
    void delete(ExerciseEntity exercise);

    @Query("DELETE FROM exercises_cache")
    void deleteAll();

    @Query("SELECT * FROM exercises_cache ORDER BY name ASC")
    List<ExerciseEntity> getAllExercises();

    @Query("SELECT * FROM exercises_cache WHERE id = :id")
    ExerciseEntity getExerciseById(long id);

    @Query("SELECT * FROM exercises_cache WHERE userId = :userId")
    List<ExerciseEntity> getUserExercises(long userId);

    @Query("SELECT * FROM exercises_cache WHERE isCustom = 1 ORDER BY name ASC")
    List<ExerciseEntity> getCustomExercises();

    @Query("SELECT * FROM exercises_cache WHERE exerciseType = :type")
    List<ExerciseEntity> getExercisesByType(String type);

    @Query("SELECT COUNT(*) FROM exercises_cache")
    int getExercisesCount();
}
