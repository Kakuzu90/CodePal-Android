package com.example.codepal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepal.Adapters.ChatAdapter;
import com.example.codepal.Interfaces.ChatInterface;
import com.example.codepal.Models.Chat;
import com.example.codepal.Services.Database;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ChatHistoryActivity extends AppCompatActivity implements ChatInterface {
    ImageView backImage;
    FloatingActionButton button;
    TextView empty, warning;
    RecyclerView recyclerView;
    Database database;
    List<Chat> chats;
    ChatAdapter adapter;
    private String USERID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        database = new Database(this);
        chats = new ArrayList<>();
        SharedPreferences shared = getSharedPreferences("AuthSession", MODE_PRIVATE);
        USERID = shared.getString("userId", null);

        backImage = findViewById(R.id.back);
        button = findViewById(R.id.newChat);
        empty = findViewById(R.id.emptyState);
        recyclerView = findViewById(R.id.recyclerView);
        warning = findViewById(R.id.warningText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(chats, this);
        recyclerView.setAdapter(adapter);

        loadChats();

        backImage.setOnClickListener(v -> {
            if (getIntent().hasExtra("id")) {
                int id = getIntent().getIntExtra("id", -1);
                String getTitle = getIntent().getStringExtra("title");
                String getContent = getIntent().getStringExtra("content");

                Intent intent = new Intent(ChatHistoryActivity.this, NoteEditorActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("title", getTitle);
                intent.putExtra("content", getContent);

                startActivity(intent);
                finish();
            } else {
                finish();
            }
        });

        button.setOnClickListener(v -> {
            Intent intent = new Intent(ChatHistoryActivity.this, ConversationActivity.class);
            startActivity(intent);
        });
    }
    private void loadChats() {
        database.destroyChat(USERID);
        chats = database.getChats(USERID);
        adapter.updateChats(chats);
        if (chats.isEmpty()) {
            empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            warning.setVisibility(View.GONE);
        } else {
            empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            warning.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onClickChat(Chat chat) {
        Intent intent = new Intent(ChatHistoryActivity.this, ConversationActivity.class);
        intent.putExtra("id", chat.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChats();
    }
}