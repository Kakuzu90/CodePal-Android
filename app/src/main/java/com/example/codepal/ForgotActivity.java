package com.example.codepal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotActivity extends AppCompatActivity {
    TextView login;
    AppCompatButton send;
    EditText emailAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        login = findViewById(R.id.loginLink);
        send = findViewById(R.id.btnSend);
        emailAddress = findViewById(R.id.email);

        login.setOnClickListener(v -> {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
            finish();
        });

        send.setOnClickListener(v -> {
            String email = emailAddress.getText().toString().trim();
            if (email.isEmpty()) {
                emailAddress.requestFocus();
                emailAddress.setError("Email address is required");
            } else {
                FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Password reset email sent", Toast.LENGTH_LONG).show();
                                emailAddress.setText(null);
                            } else {
                                Toast.makeText(this, "Error: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }

        });
    }
}