package ru.squidory.trainingdairymobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "programs_cache")
public class ProgramEntity {

    @PrimaryKey
    public long id;

    public long userId;

    public String name;

    public String description;

    public boolean isPublic;

    public long createdAt;
}
