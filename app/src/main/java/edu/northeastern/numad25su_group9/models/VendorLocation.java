package edu.northeastern.numad25su_group9.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class VendorLocation implements Parcelable {
    private double latitude;
    private double longitude;

    // For firebase
    public VendorLocation() {}

    protected VendorLocation(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<VendorLocation> CREATOR = new Creator<>() {
        @Override public VendorLocation createFromParcel(Parcel in) { return new VendorLocation(in); }
        @Override public VendorLocation[] newArray(int size) { return new VendorLocation[size]; }
    };

    @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override public int describeContents() { return 0; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        return result;
    }

    // Getters and Setters
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
