package com.example.codepal.Models;

public class Conversation {
    private String content;
    private int type;
    private String created_at;
    public static final int TYPE_USER = 1;
    public static final int TYPE_AI = 2;
    public static final int TYPE_CODE = 3;
    public Conversation(String content, int type, String created_at) {
        this.content = content;
        this.type = type;
        this.created_at = created_at;
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }
    public boolean isCode() {
        return type == TYPE_CODE;
    }

    public String getCreated_at() {
        return created_at;
    }
}
