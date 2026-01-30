package com.example.codepal.Models;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String id;
    private String username;
    private String password_text;
    private String created_at;
    private int account_status;
    private int count;
    public User(String id, String username, String created_at, String password_text, int account_status) {
        this.id = id;
        this.username = username;
        this.created_at = created_at;
        this.password_text = password_text;
        this.account_status = account_status;
    }
    public User(String id, String username, String created_at, int count, String password_text, int account_status) {
        this.id = id;
        this.username = username;
        this.created_at = created_at;
        this.count = count;
        this.password_text = password_text;
        this.account_status = account_status;
    }
    public String getId() {
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
    public String getPasswordText() {
        return password_text;
    }
    public int getAccountStatus() {
        return account_status;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("username", this.getUsername());
        data.put("password_text", this.getPasswordText());
        data.put("created_at", this.getFormattedDate());
        data.put("account_status", this.getAccountStatus());

        return data;
    }
}
