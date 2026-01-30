package com.example.codepal.Models;

public class Auth {
    private String id;
    private int account_status;
    public Auth(String id, int account_status) {
        this.id = id;
        this.account_status = account_status;
    }
    public String getId() {
        return id;
    }
    public boolean isSuspended() {
        return account_status == 0;
    }
}
