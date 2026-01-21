package com.example.codepal.Models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Chat {
    private int id;
    private String title;
    private long created_at;
    public Chat(int id, String title, long created_at) {
        this.id = id;
        this.title = title;
        this.created_at = created_at;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
    public String getPreviewTitle() {
        int maxLength = 50;
        if (title.length() > maxLength) {
            return title.substring(0, maxLength) + "...";
        }
        return title;
    }
    public String getCreatedAt() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(created_at));
    }
}
