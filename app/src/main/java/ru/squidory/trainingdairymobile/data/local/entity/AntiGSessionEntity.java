package ru.squidory.trainingdairymobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity for anti-G breathing training sessions.
 */
@Entity(tableName = "anti_g_sessions")
public class AntiGSessionEntity {

    @PrimaryKey
    public long id;

    public long userId;

    /** Number of completed cycles */
    public int amount;

    public long date;

    public String comment;
}