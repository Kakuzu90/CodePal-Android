package com.example.codepal.Models;

import java.util.HashMap;
import java.util.Map;

public class Note {
    private String id;
    private String user_id;
    private String title;
    private String content;
    private int is_pinned;
    private String created_at;
    private String updated_at;
    public Note(String id, String user_id, String title, String content, String updated_at) {
        this.id = id;
        this.user_id = user_id;
        this.title = title;
        this.content = content;
        this.updated_at = updated_at;
    }
    public Note(String id, String user_id, String title, String content, int is_pinned, String created_at, String updated_at) {
        this.id = id;
        this.user_id = user_id;
        this.title = title;
        this.content = content;
        this.is_pinned = is_pinned;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return user_id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public boolean isPinned() {
        return is_pinned == 1;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public String getPreview() {
        if (content == null || content.isEmpty()) {
            return "No content";
        }
        if (content.length() <= 100) {
            return content;
        }
        return content.substring(0, 100) + "...";
    }
    public String getFormattedDate() {
        if (updated_at == null || updated_at.isEmpty()) {
            return "";
        }
        String[] parts = updated_at.split(" ");
        if (parts.length > 0) {
            return parts[0];
        }
        return updated_at;
    }
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("title", this.getTitle());
        data.put("content", this.getContent());
        data.put("created_at", this.getFormattedDate());

        return data;
    }
}
