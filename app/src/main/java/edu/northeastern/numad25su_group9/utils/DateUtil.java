package edu.northeastern.numad25su_group9.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for dates.
 */
public final class DateUtil {

    // Formatter for yyyyMM in UTC
    private static final DateTimeFormatter MONTH_KEY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMM");

    private DateUtil() {
        // Prevent instantiation
    }

    /**
     * Converts a LocalDateTime to a Firebase year_month key.
     * Format: yyyyMM (e.g., 202507 for July 2025).
     *
     * @return The yyyyMM key string.
     */
    public static String toDateKey(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("dateTime cannot be null");
        }
        return dateTime.format(MONTH_KEY_FORMATTER);
    }

    /**
     * Converts epoch milliseconds to a Firebase year_month key.
     *
     * @param epochMillis The time in milliseconds since epoch.
     * @return The yyyyMM key string.
     */
    public static String toDateKey(long epochMillis) {
        LocalDateTime ldt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMillis),
                ZoneOffset.UTC
        );
        return toDateKey(ldt);
    }

    /**
     * Builds a Firebase month key from year and month values.
     *
     * @param year  The year (e.g., 2025)
     * @param month The month (1-12)
     * @return The yyyyMM key string
     */
    public static String toDateKey(int year, int month) {
        LocalDateTime dateTime = LocalDateTime.of(year, month, 1, 0, 0);
        return toDateKey(dateTime);
    }
}
