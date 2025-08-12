package edu.northeastern.numad25su_group9.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class Vendor implements Parcelable {
    private String name;
    private String categoryId;
    private String categoryName;

    // For firebase
    public Vendor() {}

    protected Vendor(Parcel in) {
        name = in.readString();
        categoryId = in.readString();
        categoryName = in.readString();
    }

    public static final Creator<Vendor> CREATOR = new Creator<>() {
        @Override public Vendor createFromParcel(Parcel in) { return new Vendor(in); }
        @Override public Vendor[] newArray(int size) { return new Vendor[size]; }
    };

    @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(categoryId);
        dest.writeString(categoryName);
    }

    @Override public int describeContents() { return 0; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("categoryId", categoryId);
        result.put("categoryName", categoryName);
        return result;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}