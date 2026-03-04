package com.example.codepal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    TextView create, forgot;
    EditText email, password;
    AppCompatButton loginBtn;
    MaterialCheckBox showPassword;
    private Database database;
    private SharedPreferences shared;
    private LinearLayout loaderLayout;
    private TextView loaderText;
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

        loaderLayout = findViewById(R.id.loaderLayout);
        loaderText = findViewById(R.id.loaderText);

        if (shared.getBoolean("auth", false)) {
            showLoader("Please wait...");
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
        email = findViewById(R.id.username);
        password = findViewById(R.id.password);
        showPassword = findViewById(R.id.showPassword);
        loginBtn = findViewById(R.id.btnLogin);
        forgot = findViewById(R.id.forgotLink);

        final Typeface typeface = password.getTypeface();

        create.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateActivity.class);
            startActivity(intent);
            finish();
        });

        forgot.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotActivity.class);
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
        String getEmailAddress = email.getText().toString().trim();
        String getPassword = password.getText().toString().trim();
        if (getEmailAddress.isEmpty()) {
            email.requestFocus();
            email.setError("Email address is required");
            return;
        }
        if (getPassword.isEmpty()) {
            password.requestFocus();
            password.setError("Password is required");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(getEmailAddress).matches()) {
            email.requestFocus();
            email.setError("Enter a valid email address");
            return;
        }

        showLoader("Please wait...");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // with connection
        if (Network.isConnected(this)) {
            auth.signInWithEmailAndPassword(getEmailAddress, getPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                user.reload().addOnCompleteListener(reloadTask -> {
                                   if (!user.isEmailVerified()) {
                                       password.setText(null);
                                       showUnverifiedEmail();
                                       auth.signOut();
                                       return;
                                   }
                                    db.collection("users")
                                            .document(Objects.requireNonNull(user.getUid()))
                                            .get()
                                            .addOnSuccessListener(doc -> {
                                                if (doc.exists()) {
                                                    int accountStatus = Objects.requireNonNull(doc.getLong("account_status")).intValue();
                                                    int newEmailFlg = Objects.requireNonNull(doc.getLong("new_email_flg")).intValue();
                                                    database.updateUserStatus(user.getUid(), accountStatus);

                                                    if (newEmailFlg == 1) {
                                                        HashMap<String, Object> payload = new HashMap<>();
                                                        payload.put("email", getEmailAddress);
                                                        payload.put("new_email_flg", 0);
                                                        database.updateUserEmail(user.getUid(), getEmailAddress);
                                                        db.collection("users")
                                                                .document(user.getUid())
                                                                .update(payload);
                                                    }

                                                    db.collection("users")
                                                                    .document(user.getUid())
                                                                    .update("password_text", getPassword);
                                                    database.updateUserPassword(user.getUid(), getPassword);

                                                    if (accountStatus == 2) {
                                                        db.collection("users")
                                                                .document(user.getUid())
                                                                .update("account_status", 1);
                                                    }
                                                    if (accountStatus == 2 || accountStatus == 1) {
                                                        SharedPreferences.Editor editor = shared.edit();
                                                        editor.putBoolean("auth", true);
                                                        editor.putString("username", getEmailAddress);
                                                        editor.putString("userId", user.getUid());
                                                        editor.putBoolean("is_suspended", false);
                                                        editor.apply();
                                                        redirect();
                                                    } else {
                                                        password.setText(null);
                                                        showSuspendedDialog();
                                                        auth.signOut();
                                                    }
                                                }
                                            });
                                });
                            }
                        } else {
                            Toast.makeText(this, "Login Failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // offline
            Auth user = database.auth(getEmailAddress, getPassword);
            if (user == null) {
                Toast.makeText(this, "Incorrect username or password. Please try again.", Toast.LENGTH_LONG).show();
            } else if (user.isSuspended()) {
                password.setText(null);
                showSuspendedDialog();
            } else if (user.isNotVerified()) {
                password.setText(null);
                showUnverifiedEmail();
            } else {
                SharedPreferences.Editor editor = shared.edit();
                editor.putBoolean("auth", true);
                editor.putString("username", getEmailAddress);
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
    private void showUnverifiedEmail() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Account Verification")
                .setMessage("Your email address has not been verified yet. Please check your inbox.")
                .setNeutralButton("OK", null)
                .show();
    }
    private void showLoader(String message) {
        loaderText.setText(message);
        loaderLayout.setAlpha(0f);
        loaderLayout.setVisibility(View.VISIBLE);
        loaderLayout.animate().alpha(1f).setDuration(200);
    }
    private void hideLoader() {
        loaderLayout.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> loaderLayout.setVisibility(View.GONE));
    }
}