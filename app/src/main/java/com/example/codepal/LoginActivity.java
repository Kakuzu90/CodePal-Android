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

import com.example.codepal.Models.Auth;
import com.example.codepal.Services.Database;
import com.example.codepal.Services.Network;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

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
            if (Network.isConnected(this)) {
                String userId = shared.getString("userId", null);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(Objects.requireNonNull(userId))
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                int accountStatus = Objects.requireNonNull(doc.getLong("account_status")).intValue();
                                database.updateUserStatus(userId, accountStatus);
                                if (accountStatus == 0) {
                                    SharedPreferences.Editor editor = shared.edit();
                                    editor.clear();
                                    editor.apply();
                                    showSuspendedDialog();
                                } else {
                                    redirect();
                                }
                            }
                        });
            }
            else if (shared.getBoolean("is_suspended", false)) {
                SharedPreferences.Editor editor = shared.edit();
                editor.clear();
                editor.apply();
                showSuspendedDialog();
            } else {
                redirect();
            }
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

        Auth user = database.auth(getUsername, getPassword);
        if (user == null) {
            Toast.makeText(this, "Incorrect username or password. Please try again.", Toast.LENGTH_LONG).show();
        } else {
            if (Network.isConnected(this)) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                        .document(Objects.requireNonNull(user.getId()))
                        .get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                int accountStatus = Objects.requireNonNull(doc.getLong("account_status")).intValue();
                                database.updateUserStatus(user.getId(), accountStatus);
                                if (accountStatus == 1) {
                                    SharedPreferences.Editor editor = shared.edit();
                                    editor.putBoolean("auth", true);
                                    editor.putString("username", getUsername);
                                    editor.putString("userId", user.getId());
                                    editor.putBoolean("is_suspended", user.isSuspended());
                                    editor.apply();
                                    redirect();
                                } else {
                                    password.setText(null);
                                    showSuspendedDialog();
                                }
                            }
                        });
            }
            else if (user.isSuspended()) {
                password.setText(null);
                showSuspendedDialog();
            } else {
                SharedPreferences.Editor editor = shared.edit();
                editor.putBoolean("auth", true);
                editor.putString("username", getUsername);
                editor.putString("userId", user.getId());
                editor.putBoolean("is_suspended", user.isSuspended());
                editor.apply();
                redirect();
            }
        }
    }
    private void redirect() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void showSuspendedDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Account Suspended")
                .setMessage("Your account was suspended by the administrator, please contact your administrator.")
                .setNeutralButton("OK", null)
                .show();
    }
}