package com.example.codepal.Services;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.codepal.Models.Auth;
import com.example.codepal.Models.Chat;
import com.example.codepal.Models.Conversation;
import com.example.codepal.Models.Note;
import com.example.codepal.Models.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "database.db";
    private static final int VERSION = 1;
    public Database(Context context) {
        super(context, DB_NAME, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String user = "CREATE TABLE users (" +
                "id TEXT PRIMARY KEY, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "password_text TEXT NOT NULL, " +
                "created_at TEXT NOT NULL, " +
                "account_status INTEGER DEFAULT 1, " +
                "sync_status INTEGER DEFAULT 0" +
                ")";
        String note = "CREATE TABLE notes (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "content TEXT, " +
                "is_pinned INTEGER DEFAULT 0, " +
                "created_at TEXT NOT NULL, " +
                "updated_at TEXT NOT NULL, " +
                "sync_status INTEGER DEFAULT 0, " +
                "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE" +
                ")";
        String chat = "CREATE TABLE chats (" +
                "id TEXT PRIMARY KEY, " +
                "user_id TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "created_at INTEGER NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE" +
                ")";
        String conversation = "CREATE TABLE chat_conversations (" +
                "id TEXT PRIMARY KEY, " +
                "chat_id TEXT NOT NULL, " +
                "content TEXT NOT NULL, " +
                "type INTEGER DEFAULT 1, " +
                "created_at TEXT NOT NULL, " +
                "FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE" +
                ")";
        db.execSQL(user);
        db.execSQL(note);
        db.execSQL(chat);
        db.execSQL(conversation);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS notes");
        db.execSQL("DROP TABLE IF EXISTS chats");
        db.execSQL("DROP TABLE IF EXISTS chat_conversations");
        onCreate(db);
    }
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }
    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
    public String createUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uid = UUID.randomUUID().toString();
        ContentValues values = new ContentValues();
        values.put("id", uid);
        values.put("username", username);
        values.put("password", hashPassword(password));
        values.put("created_at", getCurrentTimestamp());
        values.put("password_text", password);

        long result = db.insert("users", null, values);
        if ( result == -1 ) {
            return null;
        }
        return uid;
    }
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users",
                new String[]{"id"},
                "username=?",
                new String[]{username},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    public User getUser(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", null, "id=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        int count = 0;
        User user = null;
        if (cursor.moveToFirst()) {
            String getUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
            String password_text = cursor.getString(cursor.getColumnIndexOrThrow("password_text"));
            int account_status = cursor.getInt(cursor.getColumnIndexOrThrow("account_status"));
            Cursor countCursor = db.query("notes", new String[]{"user_id"}, "user_id=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);
            count = countCursor.getCount();
            user = new User(id, getUsername, createdAt, count, password_text, account_status);
        }
        cursor.close();
        return user;
    }
    public boolean destroyUser(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("users", "id=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }
    public Auth auth(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id", "account_status"},
                "username=? AND password=?",
                new String[]{username, hashPassword(password)},
                null, null, null);
        Auth auth = null;
        if (cursor.moveToFirst()) {
            String userId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
            int account_status = cursor.getInt(cursor.getColumnIndexOrThrow("account_status"));
            auth = new Auth(userId, account_status);
        }
        cursor.close();
        return auth;
    }
    public Note getNote(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("notes", null, "id=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        Note note = null;
        if (cursor.moveToFirst()) {
            String user_id = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            String created_at = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"));
            note = new Note(id, user_id, title, content, created_at);
        }
        cursor.close();
        return note;
    }
    public String storeNote(String userId, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uid = UUID.randomUUID().toString();
        ContentValues form = new ContentValues();
        form.put("id", uid);
        form.put("user_id", userId);
        form.put("title", title);
        form.put("content", content);
        form.put("created_at", getCurrentTimestamp());
        form.put("updated_at", getCurrentTimestamp());
        long result = db.insert("notes", null, form);
        if (result == -1) {
            return null;
        }
        return uid;
    }
    public boolean updateNote(String id, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues form = new ContentValues();
        form.put("title", title);
        form.put("content", content);
        form.put("sync_status", 0);
        form.put("updated_at", getCurrentTimestamp());
        int result = db.update("notes", form, "id=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }
    public List<Note> getNotes(String userId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("notes", null,
                "user_id=?", new String[]{String.valueOf(userId)},
                null, null, "is_pinned DESC, updated_at DESC");
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String user_id = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                int is_pinned = cursor.getInt(cursor.getColumnIndexOrThrow("is_pinned"));
                String created_at = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                String updated_at = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"));
                Note note = new Note(
                        id, user_id, title, content, is_pinned, created_at, updated_at
                );
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }
    public List<Note> searchNotes(String userId, String query) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String condition = "user_id=? AND (title LIKE ? OR content LIKE ?)";
        String[] args = {String.valueOf(userId), "%"+query+"%", "%"+query+"%"};

        Cursor cursor = db.query("notes", null, condition, args,
                null, null, "is_pinned DESC, updated_at DESC");
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String user_id = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                int is_pinned = cursor.getInt(cursor.getColumnIndexOrThrow("is_pinned"));
                String created_at = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                String updated_at = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"));
                Note note = new Note(
                        id, user_id, title, content, is_pinned, created_at, updated_at
                );
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }
    public boolean updateNotePin(String id, boolean state) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues form = new ContentValues();
        form.put("is_pinned", state);
        form.put("updated_at", getCurrentTimestamp());

        int result = db.update("notes", form, "id=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }
    public boolean destroyNote(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("notes", "id=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }
    public Chat getChat(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("chats", null, "id=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        Chat chat = null;
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            long created_at = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));
            chat = new Chat(id, title, created_at);
        }
        cursor.close();
        return chat;
    }
    public List<Chat> getChats(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Chat> chats = new ArrayList<>();

        Cursor cursor = db.query("chats", null,
                "user_id=?", new String[]{String.valueOf(userId)},
                null, null, "created_at DESC");
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                long created_at = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));
                Chat chat = new Chat(id, title, created_at);
                chats.add(chat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return chats;
    }
    public String storeChat(String userId, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uid = UUID.randomUUID().toString();
        ContentValues form = new ContentValues();
        form.put("id", uid);
        form.put("user_id", userId);
        form.put("title", title);
        form.put("created_at", System.currentTimeMillis());

        long result = db.insert("chats", null, form);
        if ( result == -1 ) {
            return null;
        }
        return uid;
    }
    public void destroyChat(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long month = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        db.delete("chats", "user_id=? AND created_at < ?",
                new String[]{String.valueOf(userId), String.valueOf(month)});
    }
    public List<Conversation> getConversations(String chatId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Conversation> convos = new ArrayList<>();

        Cursor cursor = db.query("chat_conversations", null,
                "chat_id=?", new String[]{String.valueOf(chatId)},
                null, null, "created_at ASC");
        if (cursor.moveToFirst()) {
            do {
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                int type = cursor.getInt(cursor.getColumnIndexOrThrow("type"));
                String created_at = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                Conversation convo = new Conversation(content, type, created_at);
                convos.add(convo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return convos;
    }
    public String storeConvo(String chatId, String content, int type) {
        SQLiteDatabase db = this.getWritableDatabase();
        String uid = UUID.randomUUID().toString();
        ContentValues form = new ContentValues();
        form.put("id", uid);
        form.put("chat_id", chatId);
        form.put("content", content);
        form.put("type", type);
        form.put("created_at", getCurrentTimestamp());

        long result = db.insert("chat_conversations", null, form);
        if ( result == -1) {
            return null;
        }
        return uid;
    }
    public void userSync(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues form = new ContentValues();
        form.put("sync_status", 1);

        db.update("users", form, "id=?",
                new String[]{String.valueOf(id)});
    }
    public void noteSync(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues form = new ContentValues();
        form.put("sync_status", 1);

        db.update("notes", form, "id=?",
                new String[]{String.valueOf(id)});
    }
    public void updateUserStatus(String id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues form = new ContentValues();
        form.put("account_status", status);

        db.update("users", form, "id=?",
                new String[]{String.valueOf(id)});
    }
    public List<User> getUnSyncUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<User> users = new ArrayList<>();

        Cursor cursor = db.query("users", null,
                "sync_status=?", new String[]{String.valueOf(0)},
                null, null, "created_at ASC");
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String getUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
                String password_text = cursor.getString(cursor.getColumnIndexOrThrow("password_text"));
                int account_status = cursor.getInt(cursor.getColumnIndexOrThrow("account_status"));
                User user = new User(id, getUsername, createdAt, password_text, account_status);
                users.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return users;
    }
    public List<Note> getUnSyncNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Note> notes = new ArrayList<>();

        Cursor cursor = db.query("notes", null,
                "sync_status=?", new String[]{String.valueOf(0)},
                null, null, "created_at ASC");
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
                String updated_at = cursor.getString(cursor.getColumnIndexOrThrow("updated_at"));
                Note note = new Note(id, userId, title, content, updated_at);
                notes.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notes;
    }
}
