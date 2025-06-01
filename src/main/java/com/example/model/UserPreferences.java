package com.example.model;

public class UserPreferences {
    private String notifications;
    private String language;

    public UserPreferences(String notifications, String language) {
        this.notifications = notifications;
        this.language = language;
    }

    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
