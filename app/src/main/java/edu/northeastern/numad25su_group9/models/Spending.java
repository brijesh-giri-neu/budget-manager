package edu.northeastern.numad25su_group9.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class Spending implements Parcelable {
    private double amount;
    private String categoryName;
    // Stored as UNIX-based epoch time. Will format it when displaying on UI based on local time-zone.
    private long monthUtcTs;

    // For firebase
    public Spending() {}

    // Private constructor for builder
    private Spending(Builder builder) {
        this.categoryName = builder.categoryName;
        this.amount = builder.amount;
        this.monthUtcTs = builder.monthUtcTs;
    }

    protected Spending(Parcel in) {
        categoryName = in.readString();
        amount = in.readDouble();
        monthUtcTs = in.readLong();
    }

    public static final Creator<Spending> CREATOR = new Creator<>() {
        @Override public Spending createFromParcel(Parcel in) { return new Spending(in); }
        @Override public Spending[] newArray(int size) { return new Spending[size]; }
    };

    @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(categoryName);
        dest.writeDouble(amount);
        dest.writeLong(monthUtcTs);
    }

    @Override public int describeContents() { return 0; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("categoryName", categoryName);
        result.put("amount", amount);
        result.put("effectiveFromDate", monthUtcTs);
        return result;
    }

    // Getters and setters
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /**
     * Returns the month as a {@link LocalDateTime} in the device's default time zone.
     */
    public LocalDateTime getMonthDateLocal() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(monthUtcTs), ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Sets the month using a {@link LocalDateTime}, assuming the device's default time zone.
     * Converts it internally to epoch milliseconds for Firebase storage.
     */
    public void setMonthDateLocal(LocalDateTime localDateTime) {
        ZonedDateTime zoned = localDateTime.atZone(ZoneId.systemDefault());
        this.monthUtcTs = zoned.toInstant().toEpochMilli();
    }

    /**
     * Returns the month as a {@link LocalDateTime} in UTC.
     */
    public LocalDateTime getMonthDateUtc() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(monthUtcTs), ZoneId.of("UTC"))
                .toLocalDateTime();
    }

    /**
     * Sets the month using a {@link LocalDateTime} in UTC.
     * The value is converted to epoch milliseconds for Firebase storage.
     */
    public void setMonthDateUtc(LocalDateTime utcDateTime) {
        this.monthUtcTs = utcDateTime.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

    /**
     * Gets the month as a UTC epoch.
     */
    public long getMonthUtcTs() {
        return this.monthUtcTs;
    }

    /**
     * Sets the month for Firebase deserialization.
     */
    public void setMonthUtcTs(long monthUtcTs) {
        this.monthUtcTs = monthUtcTs;
    }

    /** Start building a Spending. */
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private double amount;
        private String categoryName;
        private long monthUtcTs; // epoch millis

        public Builder setCategoryName(String categoryName) { this.categoryName = categoryName; return this; }
        public Builder setAmount(double amount) { this.amount = amount; return this; }

        /** month from LocalDateTime in device zone. */
        public Builder setMonthDateLocal(LocalDateTime local) {
            this.monthUtcTs = local.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return this;
        }
        /** month from LocalDateTime in UTC. */
        public Builder setMonthDateUtc(LocalDateTime utc) {
            this.monthUtcTs = utc.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
            return this;
        }
        /** month from epoch millis (UTC). */
        public Builder setMonthDateUtcTs(long epochMillis) {
            this.monthUtcTs = epochMillis; return this;
        }

        public Spending build() { return new Spending(this); }
    }
}
