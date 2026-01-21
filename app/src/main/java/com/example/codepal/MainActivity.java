package com.example.codepal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepal.Adapters.NoteAdapter;
import com.example.codepal.Interfaces.NoteInterface;
import com.example.codepal.Models.Note;
import com.example.codepal.Services.Database;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteInterface {
    SharedPreferences shared;
    Database database;
    NoteAdapter adapter;
    List<Note> notes;
    TextView emptyState;
    ImageView profile;
    RecyclerView recyclerView;
    FloatingActionButton chatBtn, editorBtn;
    EditText search;
    private int USERID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = new Database(this);
        shared = getSharedPreferences("AuthSession", MODE_PRIVATE);
        USERID = shared.getInt("userId", -1);

        emptyState = findViewById(R.id.emptyState);
        profile = findViewById(R.id.profile);
        recyclerView = findViewById(R.id.recyclerView);
        chatBtn = findViewById(R.id.assistant);
        editorBtn = findViewById(R.id.newNote);
        search = findViewById(R.id.search);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        notes = new ArrayList<>();
        adapter = new NoteAdapter(notes, this);
        recyclerView.setAdapter(adapter);

        chatBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatHistoryActivity.class);
            startActivity(intent);
        });

        editorBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NoteEditorActivity.class);
            startActivity(intent);
        });

        profile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchNotes(s.toString());
            }
        });
    }
    public void loadNotes() {
        notes = database.getNotes(USERID);
        adapter.updateNotes(notes);
        if (notes.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
    public void searchNotes(String query) {
        if (query.isEmpty()) {
            adapter.updateNotes(notes);
        } else {
            List<Note> filteredNotes = database.searchNotes(USERID, query);
            adapter.updateNotes(filteredNotes);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }
    @Override
    public void onClickNote(Note note) {
        Intent intent = new Intent(MainActivity.this, NoteEditorActivity.class);
        intent.putExtra("id", note.getId());
        intent.putExtra("title", note.getTitle());
        intent.putExtra("content", note.getContent());
        startActivity(intent);
    }
    @Override
    public void onLongClickNote(Note note) {
        showNoteOptionsDialog(note);
    }
    private void showNoteOptionsDialog(final Note note) {
        String[] options;
        if (note.isPinned()) {
            options = new String[]{"Unpin Note", "Delete Note", "Share Note"};
        } else {
            options = new String[]{"Pin Note", "Delete Note", "Share Note"};
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(note.getTitle());
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Pin/Unpin
                        togglePinNote(note);
                        break;
                    case 1: // Delete
                        confirmDeleteNote(note);
                        break;
                    case 2: // Share
                        shareNote(note);
                        break;
                }
            }
        });
        builder.show();
    }
    private void togglePinNote(Note note) {
        boolean newPinStatus = !note.isPinned();
        if (database.updateNotePin(note.getId(), newPinStatus)) {
            String message = newPinStatus ? "Note pinned" : "Note unpinned";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            loadNotes();
        } else {
            Toast.makeText(this, "Failed to update note", Toast.LENGTH_SHORT).show();
        }
    }
    private void confirmDeleteNote(final Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Note");
        builder.setMessage("Are you sure you want to delete this note?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNote(note);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void deleteNote(Note note) {
        if (database.destroyNote(note.getId())) {
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
            loadNotes();
        } else {
            Toast.makeText(this, "Failed to delete note", Toast.LENGTH_SHORT).show();
        }
    }
    private void shareNote(Note note) {
        String shareText = note.getTitle() + "\n\n" + note.getContent();

        android.content.ClipboardManager clipboard = (android.content.ClipboardManager)
                getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Note", shareText);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Note copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
}