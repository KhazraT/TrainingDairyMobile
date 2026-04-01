package ru.squidory.trainingdairymobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;
import ru.squidory.trainingdairymobile.data.local.entity.UserEntity;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Delete
    void delete(UserEntity user);

    @Query("DELETE FROM users_cache")
    void deleteAll();

    @Query("SELECT * FROM users_cache WHERE id = :id")
    UserEntity getUserById(long id);

    @Query("SELECT * FROM users_cache LIMIT 1")
    UserEntity getCurrentUser();

    @Query("SELECT COUNT(*) FROM users_cache")
    int getUsersCount();
}
