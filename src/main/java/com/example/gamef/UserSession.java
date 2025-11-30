package com.example.gamef;

public class UserSession {
    private static UserSession instance;

    private String username;
    private String role;
    private String memberId;
    private String fullName;

    private UserSession(String username, String role, String memberId, String fullName) {
        this.username = username;
        this.role = role;
        this.memberId = memberId;
        this.fullName = fullName;
    }

    public static void createSession(String username, String role, String memberId, String fullName) {
        instance = new UserSession(username, role, memberId, fullName);
    }

    public static UserSession getInstance() {
        return instance;
    }

    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getMemberId() { return memberId; }
    public String getFullName() { return fullName; }

    public static void clearSession() {
        instance = null;
    }
}
