package com.example.codepal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.codepal.Services.Database;
import com.example.codepal.Services.Firebase;
import com.example.codepal.Services.Network;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

@SuppressLint("CustomSplashScreen")
public class SecondSplashActivity extends AppCompatActivity {
    private Firebase firebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Network.isConnected(this)) {
            // Initialize database and Firebase service
            Database database = new Database(this);
            firebase = new Firebase(this, database);

            // Start bulk sync
            startBulkSync();
        } else {
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(SecondSplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }, 2000);
        }
    }

    private void startBulkSync() {
        firebase.bulkSyncData(new Firebase.SyncCallback() {
            @Override
            public void onComplete(boolean success, String message) {
                Log.d("CLYDE", "Sync completed: " + message);
                runOnUiThread(() -> {
                    if (success) {
                        Log.d("CLYDE", "Sync completed: " + message);
                        // Navigate to LoginActivity after successful sync
                        proceedToLoginActivity();
                    } else {
                        Log.e("CLYDE", "Sync failed: " + message);
                        // Navigate to LoginActivity even if sync fails
                        proceedToLoginActivity();
                    }
                });
            }

            @Override
            public void onProgress(int current, int total, String type) {
                runOnUiThread(() -> {
                    Log.d("CLYDE", "Syncing " + type + ": " + current + "/" + total);
                    // You can add progress bar updates here if needed
                });
            }
        });
    }
    
    private void proceedToLoginActivity() {
        Intent intent = new Intent(SecondSplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}