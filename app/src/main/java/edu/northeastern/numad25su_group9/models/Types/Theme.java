package edu.northeastern.numad25su_group9.models.Types;

import androidx.annotation.NonNull;

public enum Theme {
    AUTO,
    LIGHT,
    DARK;

    public static Theme fromString(String value) {
        try {
            return Theme.valueOf(value.trim().toUpperCase());
        } catch (Exception e) {
            return AUTO; // fallback
        }
    }

    @NonNull
    public String toString() {
        return this.name().toLowerCase();
    }
}