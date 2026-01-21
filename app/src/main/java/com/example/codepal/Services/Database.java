package com.example.codepal.Services;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

public class Database extends SQLiteOpenHelper {
    private static final String DB_NAME = "database.db";
    private static final int VERSION = 1;
    public Database(Context context) {
        super(context, DB_NAME, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String user = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "created_at TEXT NOT NULL" +
                ")";
        String note = "CREATE TABLE notes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "content TEXT, " +
                "is_pinned INTEGER DEFAULT 0, " +
                "created_at TEXT NOT NULL, " +
                "updated_at TEXT NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE" +
                ")";
        String chat = "CREATE TABLE chats (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL, " +
                "title TEXT NOT NULL, " +
                "created_at INTEGER NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE" +
                ")";
        String conversation = "CREATE TABLE chat_conversations (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "chat_id INTEGER NOT NULL, " +
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
    public boolean createUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", hashPassword(password));
        values.put("created_at", getCurrentTimestamp());

        long result = db.insert("users", null, values);
        return result != -1;
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
    public User getUser(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", null, "id=?",
                new String[]{String.valueOf(id)},
                null, null, null);
        int count = 0;
        User user = null;
        if (cursor.moveToFirst()) {
            String getUsername = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"));
            Cursor countCursor = db.query("notes", new String[]{"user_id"}, "user_id=?",
                    new String[]{String.valueOf(id)},
                    null, null, null);
            count = countCursor.getCount();
            user = new User(id, getUsername, createdAt, count);
        }
        cursor.close();
        return user;
    }
    public boolean destroyUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("users", "id=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }
    @SuppressLint("Range")
    public int auth(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id"},
                "username=? AND password=?",
                new String[]{username, hashPassword(password)},
                null, null, null);
        int user_id = 0;
        if (cursor.moveToFirst()) {
            user_id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        return user_id;
    }
    public boolean storeNote(int userId, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues form = new ContentValues();
        form.put("user_id", userId);
        form.put("title", title);
        form.put("content", content);
        form.put("created_at", getCurrentTimestamp());
        form.put("updated_at", getCurrentTimestamp());
        long result = db.insert("notes", null, form);
        return result != -1;
    }
    public boolean updateNote(int id, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues form = new ContentValues();
        form.put("title", title);
        form.put("content", content);
        form.put("updated_at", getCurrentTimestamp());
        int result = db.update("notes", form, "id=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }
    public List<Note> getNotes(int userId) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("notes", null,
                "user_id=?", new String[]{String.valueOf(userId)},
                null, null, "is_pinned DESC, updated_at DESC");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
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
    public List<Note> searchNotes(int userId, String query) {
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String condition = "user_id=? AND (title LIKE ? OR content LIKE ?)";
        String[] args = {String.valueOf(userId), "%"+query+"%", "%"+query+"%"};

        Cursor cursor = db.query("notes", null, condition, args,
                null, null, "is_pinned DESC, updated_at DESC");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int user_id = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
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
    public boolean updateNotePin(int id, boolean state) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues form = new ContentValues();
        form.put("is_pinned", state);
        form.put("updated_at", getCurrentTimestamp());

        int result = db.update("notes", form, "id=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }
    public boolean destroyNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("notes", "id=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }
    public List<Chat> getChats(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Chat> chats = new ArrayList<>();

        Cursor cursor = db.query("chats", null,
                "user_id=?", new String[]{String.valueOf(userId)},
                null, null, "created_at DESC");
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                long created_at = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));
                Chat chat = new Chat(id, title, created_at);
                chats.add(chat);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return chats;
    }
    public long storeChat(int userId, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues form = new ContentValues();
        form.put("user_id", userId);
        form.put("title", title);
        form.put("created_at", System.currentTimeMillis());

        return db.insert("chats", null, form);
    }
    public void destroyChat(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        long month = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);

        db.delete("chats", "user_id=? AND created_at < ?",
                new String[]{String.valueOf(userId), String.valueOf(month)});
    }
    public List<Conversation> getConversations(int chatId) {
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
    public boolean storeConvo(int chatId, String content, int type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues form = new ContentValues();
        form.put("chat_id", chatId);
        form.put("content", content);
        form.put("type", type);
        form.put("created_at", getCurrentTimestamp());

        long result = db.insert("chat_conversations", null, form);
        return result != -1;
    }
}
