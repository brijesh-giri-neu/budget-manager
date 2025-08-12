package edu.northeastern.numad25su_group9.utils;

import java.util.Locale;

/**
 * Utility class for dates.
 */
public final class LocationUtil {

    private LocationUtil() {
        // Prevent instantiation
    }

    /** Rounds lat/lon to 4 decimals to cluster frequent places and for stable keys. */
    public static String roundLocToDecimals(double lat, double lon) {
        return String.format(Locale.US, "%.4f,%.4f", lat, lon);
    }
}
