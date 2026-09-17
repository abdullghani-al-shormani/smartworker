package com.abdullghani.smartworkerlibrary.models;

public class UserProfile {
    private String name;
    private String role;

    public UserProfile(String name, String role) {
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}