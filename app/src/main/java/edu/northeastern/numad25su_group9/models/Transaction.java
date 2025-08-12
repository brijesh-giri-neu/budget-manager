package edu.northeastern.numad25su_group9.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a transaction in the system.
 */
@IgnoreExtraProperties
public class Transaction implements Parcelable {
    @Exclude
    private String transactionId;
    private String categoryName;
    private String description;
    private double amount;
    private boolean ignore;
    @Nullable private String vendorId;
    @Nullable private String vendorName;
    @Nullable private String locationId;
    @Nullable private Double latitude;
    @Nullable private Double longitude;
    // Stored as UNIX-based epoch time. Will format it when displaying on UI based on local time-zone.
    private long transactionDate;

    /** Required by Firebase */
    public Transaction() {}

    private Transaction(Builder builder) {
        this.categoryName = builder.categoryName;
        this.description = builder.description;
        this.amount = builder.amount;
        this.ignore = builder.ignore;
        this.vendorId = builder.vendorId;
        this.vendorName = builder.vendorName;
        this.locationId = builder.locationId;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.transactionDate = builder.transactionDate;
    }

    protected Transaction(Parcel in) {
        transactionId = in.readString();
        categoryName = in.readString();
        description = in.readString();
        amount = in.readDouble();
        ignore = in.readByte() != 0;
        vendorId = in.readString();
        vendorName = in.readString();
        locationId = in.readString();
        latitude = in.readByte() == 0 ? null : in.readDouble();
        longitude = in.readByte() == 0 ? null : in.readDouble();
        transactionDate = in.readLong();
    }

    public static final Creator<Transaction> CREATOR = new Creator<>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    @Override public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(transactionId);
        dest.writeString(categoryName);
        dest.writeString(description);
        dest.writeDouble(amount);
        dest.writeByte((byte) (ignore ? 1 : 0));
        dest.writeString(vendorId);
        dest.writeString(vendorName);
        dest.writeString(locationId);
        if (latitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(latitude);
        }
        if (longitude == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(longitude);
        }
        dest.writeLong(transactionDate);
    }

    /**
     * Serialize to map.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("categoryName", categoryName);
        result.put("description", description);
        result.put("amount", amount);
        result.put("ignore", ignore);
        result.put("vendorId", vendorId);
        result.put("vendorName", vendorName);
        result.put("locationId", locationId);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("transactionDate", transactionDate);
        return result;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isIgnore() { return ignore; }
    public void setIgnore(boolean ignore) { this.ignore = ignore; }

    @Nullable public String getVendorId() { return vendorId; }
    public void setVendorId(@Nullable String vendorId) { this.vendorId = vendorId; }

    @Nullable public String getVendorName() { return vendorName; }
    public void setVendorName(@Nullable String vendorName) { this.vendorName = vendorName; }

    @Nullable public String getLocationId() { return locationId; }
    public void setLocationId(@Nullable String locationId) { this.locationId = locationId; }

    @Nullable public Double getLatitude() { return latitude; }
    public void setLatitude(@Nullable Double latitude) { this.latitude = latitude; }

    @Nullable public Double getLongitude() { return longitude; }
    public void setLongitude(@Nullable Double longitude) { this.longitude = longitude; }

    /**
     * Returns the transaction date as a {@link LocalDateTime} in the device's default time zone.
     */
    public LocalDateTime getTransactionDateLocal() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(transactionDate), ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Sets the transaction date using a {@link LocalDateTime}, assuming the device's default time zone.
     * Converts it internally to epoch milliseconds for Firebase storage.
     */
    public void setTransactionDateLocal(LocalDateTime localDateTime) {
        ZonedDateTime zoned = localDateTime.atZone(ZoneId.systemDefault());
        this.transactionDate = zoned.toInstant().toEpochMilli();
    }

    /**
     * Returns the transaction date as a {@link LocalDateTime} in UTC.
     */
    public LocalDateTime getTransactionDateUtc() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(transactionDate), ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    /**
     * Sets the transaction date using a {@link LocalDateTime} in UTC.
     * The value is converted to epoch milliseconds for Firebase storage.
     */
    public void setTransactionDateUtc(LocalDateTime utcDateTime) {
        this.transactionDate = utcDateTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

    /**
     * Sets the transaction date using a linux based epoch milliseconds timestamp.
     */
    public void setTransactionDate(long epochMillis) {
        this.transactionDate = epochMillis;
    }

    /**
     * Gets the transaction date as a linux based epoch milliseconds timestamp.
     */
    public long getTransactionDate() {
        return transactionDate;
    }

    @NonNull
    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", description='" + description + '\'' +
                ", amount=" + amount +
                ", ignore=" + ignore +
                ", vendorId='" + vendorId + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", locationId='" + locationId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", transactionDate=" + transactionDate +
                '}';
    }

    /** Start building a Transaction. */
    public static Builder builder() { return new Transaction.Builder(); }

    public static class Builder {
        private String categoryName;
        private String description;
        private double amount;
        private boolean ignore;
        @Nullable private String vendorId;
        @Nullable private String vendorName;
        @Nullable private String locationId;
        @Nullable private Double latitude;
        @Nullable private Double longitude;
        private long transactionDate;

        public Builder setCategoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public Builder setDescription(String description) { this.description = description; return this; }
        public Builder setAmount(double amount) { this.amount = amount; return this; }
        public Builder setIgnore(boolean ignore) { this.ignore = ignore; return this; }
        public Builder setVendorId(@Nullable String vendorId) { this.vendorId = vendorId; return this; }
        public Builder setVendorName(@Nullable String vendorName) { this.vendorName = vendorName; return this; }
        public Builder setLocationId(@Nullable String locationId) { this.locationId = locationId; return this; }
        public Builder setLatitude(@Nullable Double latitude) { this.latitude = latitude; return this; }
        public Builder setLongitude(@Nullable Double longitude) { this.longitude = longitude; return this; }
        public Builder setTransactionDate(ZonedDateTime transactionDate) {
            this.transactionDate = transactionDate.toInstant().toEpochMilli();
            return this;
        }
        public Builder setTransactionDateUtc(LocalDateTime utcDateTime) {
            this.transactionDate = utcDateTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
            return this;
        }
        public Builder setTransactionDateLocal(LocalDateTime localDateTime) {
            this.transactionDate = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }
}
