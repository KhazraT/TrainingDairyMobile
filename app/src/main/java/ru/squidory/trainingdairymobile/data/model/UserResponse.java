package ru.squidory.trainingdairymobile.data.model;

import com.google.gson.annotations.SerializedName;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserResponse {

    @SerializedName("id")
    private long id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("birthDate")
    private String birthDate; // Format: yyyy-MM-dd for backend compatibility

    @SerializedName("gender")
    private String gender;

    public UserResponse() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getBirthDate() {
        if (birthDate == null) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            return sdf.parse(birthDate);
        } catch (Exception e) {
            return null;
        }
    }

    public void setBirthDate(Date date) {
        if (date == null) {
            this.birthDate = null;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            this.birthDate = sdf.format(date);
        }
    }

    // For direct string access (used by Gson)
    public String getBirthDateString() { return birthDate; }
    public void setBirthDateString(String birthDate) { this.birthDate = birthDate; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
}
