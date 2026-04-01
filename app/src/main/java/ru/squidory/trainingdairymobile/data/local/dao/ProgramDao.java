package ru.squidory.trainingdairymobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import ru.squidory.trainingdairymobile.data.local.entity.ProgramEntity;

@Dao
public interface ProgramDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProgramEntity program);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProgramEntity> programs);

    @Delete
    void delete(ProgramEntity program);

    @Query("DELETE FROM programs_cache WHERE userId = :userId")
    void deleteUserPrograms(long userId);

    @Query("SELECT * FROM programs_cache ORDER BY createdAt DESC")
    List<ProgramEntity> getAllPrograms();

    @Query("SELECT * FROM programs_cache WHERE id = :id")
    ProgramEntity getProgramById(long id);

    @Query("SELECT * FROM programs_cache WHERE userId = :userId")
    List<ProgramEntity> getUserPrograms(long userId);

    @Query("SELECT COUNT(*) FROM programs_cache")
    int getProgramsCount();
}
