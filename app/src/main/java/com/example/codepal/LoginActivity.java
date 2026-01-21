package com.example.codepal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.codepal.Services.Database;
import com.google.android.material.checkbox.MaterialCheckBox;

public class LoginActivity extends AppCompatActivity {
    TextView create;
    EditText username, password;
    AppCompatButton loginBtn;
    MaterialCheckBox showPassword;
    private Database database;
    private SharedPreferences shared;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = new Database(this);
        shared = getSharedPreferences("AuthSession", MODE_PRIVATE);

        if (shared.getBoolean("auth", false)) {
            redirect();
            return;
        }

        create = findViewById(R.id.createLink);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        showPassword = findViewById(R.id.showPassword);
        loginBtn = findViewById(R.id.btnLogin);

        final Typeface typeface = password.getTypeface();

        create.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateActivity.class);
            startActivity(intent);
            finish();
        });

        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                password.setTypeface(typeface);
                password.setSelection(password.getText().length());
            }
        });

        loginBtn.setOnClickListener(v -> {
            store();
        });
    }
    private void store() {
        String getUsername = username.getText().toString().trim();
        String getPassword = password.getText().toString().trim();
        if (getUsername.isEmpty()) {
            username.requestFocus();
            username.setError("Username is required");
            return;
        }
        if (getPassword.isEmpty()) {
            password.requestFocus();
            password.setError("Password is required");
            return;
        }

        int userId = database.auth(getUsername, getPassword);
        if (userId == 0) {
            Toast.makeText(this, "Incorrect username or password. Please try again.", Toast.LENGTH_LONG).show();
        } else {
            SharedPreferences.Editor editor = shared.edit();
            editor.putBoolean("auth", true);
            editor.putString("username", getUsername);
            editor.putInt("userId", userId);
            editor.apply();
            redirect();
        }
    }
    private void redirect() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}