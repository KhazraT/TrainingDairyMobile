package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class UserSettingsRequest {

    @SerializedName("lengthUnit")
    private String lengthUnit;

    @SerializedName("weightUnit")
    private String weightUnit;

    @SerializedName("distanceUnit")
    private String distanceUnit;

    @SerializedName("theme")
    private String theme;

    @SerializedName("language")
    private String language;

    public UserSettingsRequest() {}

    public String getLengthUnit() { return lengthUnit; }
    public void setLengthUnit(String lengthUnit) { this.lengthUnit = lengthUnit; }

    public String getWeightUnit() { return weightUnit; }
    public void setWeightUnit(String weightUnit) { this.weightUnit = weightUnit; }

    public String getDistanceUnit() { return distanceUnit; }
    public void setDistanceUnit(String distanceUnit) { this.distanceUnit = distanceUnit; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
