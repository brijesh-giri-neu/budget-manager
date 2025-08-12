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

public class Budget implements Parcelable {
    private double amount;
    // Stored as UNIX-based epoch time. Will format it when displaying on UI based on local time-zone.
    private long monthUtcTs;

    // For firebase
    public Budget() {}

    // Private constructor for builder
    private Budget(Builder builder) {
        this.amount = builder.amount;
        this.monthUtcTs = builder.monthUtcTs;
    }

    protected Budget(Parcel in) {
        amount = in.readDouble();
        monthUtcTs = in.readLong();
    }

    public static final Creator<Budget> CREATOR = new Creator<>() {
        @Override public Budget createFromParcel(Parcel in) { return new Budget(in); }
        @Override public Budget[] newArray(int size) { return new Budget[size]; }
    };

    @Override public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeDouble(amount);
        dest.writeLong(monthUtcTs);
    }

    @Override public int describeContents() { return 0; }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("amount", amount);
        result.put("monthUtcTs", monthUtcTs);
        return result;
    }

    // Getters and setters
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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
     * For firebase deserialization.
     */
    public void setMonthUtcTs(long monthUtcTs) {
        this.monthUtcTs = monthUtcTs;
    }

    /** Start building a Budget. */
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private double amount;
        private long monthUtcTs; // epoch millis

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
        /** month from ZonedDateTime. */
        public Builder setMonthDate(ZonedDateTime zoned) {
            this.monthUtcTs = zoned.toInstant().toEpochMilli(); return this;
        }
        /** month from epoch millis (UTC). */
        public Builder setMonthDateUtcTs(long epochMillis) {
            this.monthUtcTs = epochMillis; return this;
        }

        public Budget build() { return new Budget(this); }
    }
}
