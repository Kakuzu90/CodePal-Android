package com.example.codepal;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.CheckBox;
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

public class CreateActivity extends AppCompatActivity {
    TextView loginLink;
    EditText username, password, confirm_password;
    CheckBox showPassword;
    AppCompatButton createBtn;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = new Database(this);

        loginLink = findViewById(R.id.loginLink);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);
        showPassword = findViewById(R.id.showPassword);
        createBtn = findViewById(R.id.btnCreate);

        final Typeface typeface = password.getTypeface();

        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    confirm_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    confirm_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                password.setTypeface(typeface);
                confirm_password.setTypeface(typeface);
                password.setSelection(password.getText().length());
                confirm_password.setSelection(confirm_password.getText().length());
            }
        });

        loginLink.setOnClickListener(v -> {
            Intent login = new Intent(CreateActivity.this, LoginActivity.class);
            startActivity(login);
            finish();
        });

        createBtn.setOnClickListener(v -> {
            store();
        });
    }
    private void store() {
        String getUsername = username.getText().toString().trim();
        String getPassword = password.getText().toString().trim();
        String getConfirmPassword = confirm_password.getText().toString().trim();

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
        if (getConfirmPassword.isEmpty()) {
            confirm_password.requestFocus();
            confirm_password.setError("Password is required");
            return;
        }
        if (getPassword.length() < 6) {
            password.requestFocus();
            password.setError("Password must be at least 6 characters");
            return;
        }
        if (!getPassword.equals(getConfirmPassword)) {
            Toast.makeText(this, "Password do not match", Toast.LENGTH_LONG).show();
            return;
        }
        if (database.checkUsername(getUsername)) {
            Toast.makeText(this, getUsername + " already exists. Please choose another", Toast.LENGTH_LONG).show();
            return;
        }
        if (database.createUser(getUsername, getPassword)) {
            Toast.makeText(this, "Account created successfully! Please login.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(CreateActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to create an account. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
}