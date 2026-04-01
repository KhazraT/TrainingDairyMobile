package ru.squidory.trainingdairymobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises_cache")
public class ExerciseEntity {

    @PrimaryKey
    public long id;

    public String name;

    public String description;

    public String exerciseType;

    public String video;

    public String photo;

    public String technique;

    public long userId;

    public boolean isCustom;

    public long createdAt;

    public String muscleGroupsJson;

    public String equipmentJson;
}
