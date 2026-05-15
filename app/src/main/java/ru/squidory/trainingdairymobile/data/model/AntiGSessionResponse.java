package ru.squidory.trainingdairymobile.data.model;

import java.time.OffsetDateTime;

/**
 * Response model for an anti-G breathing training session.
 */
public class AntiGSessionResponse {
    private Long id;
    private Long userId;
    private int amount;
    private OffsetDateTime date;
    private String comment;

    public AntiGSessionResponse(Long id, Long userId, int amount, OffsetDateTime date, String comment) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.comment = comment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}