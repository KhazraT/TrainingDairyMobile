package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("userId")
    private long userId;

    @SerializedName("email")
    private String email;

    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("refreshToken")
    private String refreshToken;

    public AuthResponse() {}

    // Getters and Setters
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
