package ru.squidory.trainingdairymobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sessions_cache")
public class SessionEntity {

    @PrimaryKey
    public long id;

    public long userId;

    public long programId;

    public long workoutId;

    public String comment;

    public long startTime;

    public long endTime;
}
