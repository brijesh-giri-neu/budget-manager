package edu.northeastern.numad25su_group9.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Category implements Parcelable {
    private String name;

    // For firebase
    public Category() {}

    protected Category(Parcel in) {
        name = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<>() {
        @Override public Category createFromParcel(Parcel in) { return new Category(in); }
        @Override public Category[] newArray(int size) { return new Category[size]; }
    };

    @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
    }

    @Override public int describeContents() { return 0; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}