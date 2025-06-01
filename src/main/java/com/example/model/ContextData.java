package com.example.model;

import java.sql.Timestamp; // Import for java.sql.Timestamp

public class ContextData {
    private String userId;
    private String theme;
    private UserPreferences preferences;
    private String timestamp; // Keep as String as per requirement

    public ContextData(String userId, String theme, UserPreferences preferences, String timestamp) {
        this.userId = userId;
        this.theme = theme;
        this.preferences = preferences;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
