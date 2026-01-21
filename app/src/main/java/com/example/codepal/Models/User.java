package com.example.codepal.Models;

public class User {
    private int id;
    private String username;
    private String created_at;
    private int count;
    public User(int id, String username, String created_at, int count) {
        this.id = id;
        this.username = username;
        this.created_at = created_at;
        this.count = count;
    }
    public int getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getCreated_at() {
        return created_at;
    }
    public int getCount() { return count; }
    public String getFormattedDate() {
        if (created_at == null || created_at.isEmpty()) {
            return "";
        }
        String[] parts = created_at.split(" ");
        if (parts.length > 0) {
            return parts[0];
        }
        return created_at;
    }
}
