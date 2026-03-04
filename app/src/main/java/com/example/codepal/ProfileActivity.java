package com.example.codepal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.codepal.Models.User;
import com.example.codepal.Services.Database;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    Database database;
    SharedPreferences shared;
    private String USERID;
    ImageView backBtn, logoutBtn;
    TextView usernameText, dateText, noteText, emailText;
    EditText editText;
    AppCompatButton changeEmail;
    User user;
    @SuppressLint("SetTextI18n")
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
        emailText = findViewById(R.id.email);
        editText = findViewById(R.id.editEmail);
        changeEmail = findViewById(R.id.btnSend);

        user = database.getUser(USERID);

        usernameText.setText("Username: " + user.getUsername());
        emailText.setText("Email Address: " + user.getEmail());
        dateText.setText("Joined: " + user.getFormattedDate());
        noteText.setText("Number of Notes: " + user.getCount());

        backBtn.setOnClickListener(v -> {
            finish();
        });

        logoutBtn.setOnClickListener(v -> {
            logout();
        });

        changeEmail.setOnClickListener(v -> {
            confirmChange();
        });
    }
    private void logout() {
        SharedPreferences.Editor editor = shared.edit();
        editor.clear();
        editor.apply();

        FirebaseAuth.getInstance().signOut();

        // Navigate to login
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void confirmChange() {
        String getEmailAddress = editText.getText().toString().trim();
        if (getEmailAddress.isEmpty()) {
            editText.requestFocus();
            editText.setError("Email address is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(getEmailAddress).matches()) {
            editText.requestFocus();
            editText.setError("Enter a valid email address");
            return;
        }
        new android.app.AlertDialog.Builder(this)
                .setTitle("Verification Popup")
                .setMessage("Are you sure you want to change your email address?")
                .setPositiveButton("Yes", new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface dialog, int which) {
                        proceedChange();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
    private void proceedChange() {
        String getEmailAddress = editText.getText().toString().trim();
        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), user.getPasswordText());

        user1.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       user1.verifyBeforeUpdateEmail(getEmailAddress)
                               .addOnCompleteListener(ctask -> {
                                    if (ctask.isSuccessful()) {
                                        FirebaseFirestore.getInstance().collection("users")
                                                .document(user.getId())
                                                .update("new_email_flg", 1);
                                        Toast.makeText(this,
                                                "Verification sent to new email. Please verify to complete update.",
                                                Toast.LENGTH_LONG).show();
                                        logout();
                                    } else {
                                        Toast.makeText(this,
                                                Objects.requireNonNull(ctask.getException()).getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                               });

                   }
                });
    }
}