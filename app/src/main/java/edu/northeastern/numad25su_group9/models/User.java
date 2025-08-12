package edu.northeastern.numad25su_group9.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import edu.northeastern.numad25su_group9.models.Types.Theme;

public class User implements Parcelable {
    private String email;
    private String displayName;
    private Theme theme;
    private boolean isAnonymous;
    private boolean locationAlerts;

    // For firebase
    public User() {}

    protected User(Parcel in) {
        email = in.readString();
        displayName = in.readString();
        theme = Theme.fromString(in.readString());
        isAnonymous = in.readByte() != 0;
        locationAlerts = in.readByte() != 0;
    }

    public static final Creator<User> CREATOR = new Creator<>() {
        @Override public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(displayName);
        dest.writeString(theme.toString());
        dest.writeByte((byte) (isAnonymous ? 1 : 0));
        dest.writeByte((byte) (locationAlerts ? 1 : 0));
    }

    @Override public int describeContents() {
        return 0;
    }

    // Serialize
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("displayName", displayName);
        result.put("theme", theme.toString());
        result.put("isAnonymous", isAnonymous);
        result.put("locationAlerts", locationAlerts);
        return result;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = Theme.fromString(theme);
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.isAnonymous = anonymous;
    }

    public boolean isLocationAlerts() {
        return locationAlerts;
    }

    public void setLocationAlerts(boolean locationAlerts) {
        this.locationAlerts = locationAlerts;
    }
}

