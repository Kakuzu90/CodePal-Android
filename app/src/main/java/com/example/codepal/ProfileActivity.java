package com.example.codepal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.codepal.Models.User;
import com.example.codepal.Services.Database;

public class ProfileActivity extends AppCompatActivity {
    Database database;
    SharedPreferences shared;
    private String USERID;
    ImageView backBtn, logoutBtn;
    TextView usernameText, dateText, noteText;
    AppCompatButton delete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = new Database(this);
        shared = getSharedPreferences("AuthSession", MODE_PRIVATE);
        USERID = shared.getString("userId", null);

        backBtn = findViewById(R.id.back);
        logoutBtn = findViewById(R.id.logout);
        usernameText = findViewById(R.id.username);
        dateText = findViewById(R.id.date);
        noteText = findViewById(R.id.note);
        delete = findViewById(R.id.deleteBtn);

        User user = database.getUser(USERID);

        usernameText.setText("Username: " + user.getUsername());
        dateText.setText("Joined: " + user.getFormattedDate());
        noteText.setText("Number of Notes: " + user.getCount());

        backBtn.setOnClickListener(v -> {
            finish();
        });

        logoutBtn.setOnClickListener(v -> {
            logout();
        });

        delete.setOnClickListener(v -> {
            confirmDelete();
        });
    }
    private void logout() {
        SharedPreferences.Editor editor = shared.edit();
        editor.clear();
        editor.apply();

        // Navigate to login
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void confirmDelete() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account?")
                .setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        database.destroyUser(USERID);
                        logout();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}