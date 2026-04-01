package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;

public class AuthRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    // Для регистрации
    @SerializedName("name")
    private String name;

    @SerializedName("birthDate")
    private String birthDate;

    @SerializedName("gender")
    private String gender;

    public AuthRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public AuthRequest(String email, String password, String name, String birthDate, String gender) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
