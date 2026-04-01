package ru.squidory.trainingdairymobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users_cache")
public class UserEntity {

    @PrimaryKey
    public long id;

    public String name;

    public String email;

    public long birthDate;

    public String gender;

    public long registrationDate;
}
