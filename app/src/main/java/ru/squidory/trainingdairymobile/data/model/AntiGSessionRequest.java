package ru.squidory.trainingdairymobile.data.model;

/**
 * Request model for creating/updating an anti-G breathing training session.
 */
public class AntiGSessionRequest {
    private int amount;
    private String comment;

    public AntiGSessionRequest(int amount, String comment) {
        this.amount = amount;
        this.comment = comment;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}