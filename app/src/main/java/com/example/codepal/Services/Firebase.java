package com.example.codepal.Services;

import android.content.ContentValues;
import android.content.Context;

import com.example.codepal.Models.Chat;
import com.example.codepal.Models.Note;
import com.example.codepal.Models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class Firebase {
    Context context;
    Database database;
    private static final String TAG = "Firebase";
    private static final int BATCH_SIZE = 10; // Process 10 items at a time
    private Handler handler = new Handler(Looper.getMainLooper());
    
    public Firebase(Context context, Database database) {
        this.context = context;
        this.database = database;
    }
    public static FirebaseFirestore db = FirebaseFirestore.getInstance();
    public void storeUser(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("username", user.getUsername());
        data.put("password_text", user.getPasswordText());
        data.put("created_at", user.getFormattedDate());
        data.put("account_status", user.getAccountStatus());
        db.collection("users")
                .document(user.getId())
                .set(data)
                .addOnSuccessListener(v -> {
                    database.userSync(user.getId());
                });
    }
    public void storeNote(Note note) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", note.getTitle());
        data.put("content", note.getContent());
        data.put("created_at", note.getFormattedDate());
        db.collection("users")
                .document(note.getUserId())
                .collection("notes")
                .document(note.getId())
                .set(data)
                .addOnSuccessListener(v -> {
                   database.noteSync(note.getId());
                });
    }
    public void storeChat(Chat chat, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", chat.getTitle());
        data.put("created_at", chat.getCreatedAt());
        db.collection("users")
                .document(userId)
                .collection("chats")
                .document(chat.getId())
                .set(data);
    }
    public void storeConvo(String userId, String chatId, String uid, String content, int type) {
        Map<String, Object> data = new HashMap<>();
        data.put("content", content);
        data.put("type", type);
        db.collection("users")
                .document(userId)
                .collection("chats")
                .document(chatId)
                .collection("conversations")
                .document(uid)
                .set(data);
    }

    // Bulk sync methods
    public void bulkSyncData() {
        bulkSyncData(null);
    }

    public void bulkSyncData(SyncCallback callback) {
        // Start syncing users first, then notes
        bulkSyncUsers(new SyncCallback() {
            @Override
            public void onComplete(boolean success, String message) {
                if (success) {
                    Log.d(TAG, "User sync completed, starting note sync");
                    bulkSyncNotes(callback);
                } else {
                    Log.e(TAG, "User sync failed: " + message);
                    if (callback != null) {
                        callback.onComplete(false, "User sync failed: " + message);
                    }
                }
            }

            @Override
            public void onProgress(int current, int total, String type) {
                if (callback != null) {
                    callback.onProgress(current, total, type);
                }
            }
        });
    }

    public void bulkSyncUsers(SyncCallback callback) {
        List<User> unSyncUsers = database.getUnSyncUsers();
        
        if (unSyncUsers.isEmpty()) {
            Log.d(TAG, "No users to sync");
            if (callback != null) {
                callback.onComplete(true, "No users to sync");
            }
            return;
        }

        Log.d(TAG, "Starting bulk user sync. Count: " + unSyncUsers.size());
        processBatchUsers(unSyncUsers, 0, callback);
    }

    public void bulkSyncNotes(SyncCallback callback) {
        List<Note> unSyncNotes = database.getUnSyncNotes();
        
        if (unSyncNotes.isEmpty()) {
            Log.d(TAG, "No notes to sync");
            if (callback != null) {
                callback.onComplete(true, "No notes to sync");
            }
            return;
        }

        Log.d(TAG, "Starting bulk note sync. Count: " + unSyncNotes.size());
        processBatchNotes(unSyncNotes, 0, callback);
    }

    private void processBatchUsers(List<User> users, int startIndex, SyncCallback callback) {
        if (startIndex >= users.size()) {
            Log.d(TAG, "All users synced successfully");
            if (callback != null) {
                callback.onComplete(true, "All users synced successfully");
            }
            return;
        }

        int endIndex = Math.min(startIndex + BATCH_SIZE, users.size());
        List<User> batch = users.subList(startIndex, endIndex);
        
        WriteBatch firestoreBatch = db.batch();
        
        for (User user : batch) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            data.put("password_text", user.getPasswordText());
            data.put("created_at", user.getFormattedDate());
            data.put("account_status", user.getAccountStatus());
            
            firestoreBatch.set(db.collection("users").document(user.getId()), data);
        }

        firestoreBatch.commit()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Batch users synced: " + startIndex + " to " + (endIndex - 1));
                
                // Update local database sync status
                for (User user : batch) {
                    database.userSync(user.getId());
                }
                
                if (callback != null) {
                    callback.onProgress(endIndex, users.size(), "users");
                }
                
                // Process next batch with delay to prevent overwhelming
                handler.postDelayed(() -> {
                    processBatchUsers(users, endIndex, callback);
                }, 500); // 500ms delay between batches
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to sync user batch: " + startIndex + " to " + (endIndex - 1), e);
                if (callback != null) {
                    callback.onComplete(false, "Failed to sync users: " + e.getMessage());
                }
            });
    }

    private void processBatchNotes(List<Note> notes, int startIndex, SyncCallback callback) {
        if (startIndex >= notes.size()) {
            Log.d(TAG, "All notes synced successfully");
            if (callback != null) {
                callback.onComplete(true, "All notes synced successfully");
            }
            return;
        }

        int endIndex = Math.min(startIndex + BATCH_SIZE, notes.size());
        List<Note> batch = notes.subList(startIndex, endIndex);
        
        WriteBatch firestoreBatch = db.batch();
        
        for (Note note : batch) {
            Map<String, Object> data = new HashMap<>();
            data.put("title", note.getTitle());
            data.put("content", note.getContent());
            data.put("created_at", note.getFormattedDate());
            
            firestoreBatch.set(db.collection("users")
                .document(note.getUserId())
                .collection("notes")
                .document(note.getId()), data);
        }

        firestoreBatch.commit()
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Batch notes synced: " + startIndex + " to " + (endIndex - 1));
                
                // Update local database sync status
                for (Note note : batch) {
                    database.noteSync(note.getId());
                }
                
                if (callback != null) {
                    callback.onProgress(endIndex, notes.size(), "notes");
                }
                
                // Process next batch with delay to prevent overwhelming
                handler.postDelayed(() -> {
                    processBatchNotes(notes, endIndex, callback);
                }, 500); // 500ms delay between batches
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to sync note batch: " + startIndex + " to " + (endIndex - 1), e);
                if (callback != null) {
                    callback.onComplete(false, "Failed to sync notes: " + e.getMessage());
                }
            });
    }

    // Callback interface for sync operations
    public interface SyncCallback {
        void onComplete(boolean success, String message);
        void onProgress(int current, int total, String type);
    }
}
