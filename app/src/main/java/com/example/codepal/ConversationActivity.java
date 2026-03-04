package com.example.codepal;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepal.Adapters.ConvoAdapter;
import com.example.codepal.Models.Chat;
import com.example.codepal.Models.Conversation;
import com.example.codepal.Services.CodePalAssistant;
import com.example.codepal.Services.Database;
import com.example.codepal.Services.Firebase;
import com.example.codepal.Services.Network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConversationActivity extends AppCompatActivity {
    ImageView backImage;
    EditText input;
    AppCompatButton button;
    RecyclerView recyclerView;
    Database database;
    Firebase firebase;
    ConvoAdapter adapter;
    List<Conversation> convos;
    OkHttpClient client = new OkHttpClient();
    Call running;
    private String USERID;
    private String CHATID = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conversation);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getIntent().hasExtra("id")) {
            CHATID = getIntent().getStringExtra("id");
        }

        database = new Database(this);
        firebase = new Firebase(this, database);
        convos = new ArrayList<>();

        SharedPreferences shared = getSharedPreferences("AuthSession", MODE_PRIVATE);
        USERID = shared.getString("userId", null);
        adapter = new ConvoAdapter(this, CHATID, convos, database, firebase, USERID);

        backImage = findViewById(R.id.back);
        input = findViewById(R.id.prompt);
        button = findViewById(R.id.btnSend);
        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        loadConversation();
        recyclerView.setAdapter(adapter);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(adapter.getItemCount());
            }
        });

        input.requestFocus();
        backImage.setOnClickListener(v -> {
            finish();
        });
        button.setOnClickListener(v -> {
            sendMessage();
        });

        if (CHATID == null) {
            setupWelcomeMessage();
        }
    }
    private void sendMessage() {
        String message = input.getText().toString().trim();

        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        if (CHATID == null) {
            setupChatTitle(message);
        }

        if (!Network.isConnected(this)) {
            Toast.makeText(this, "No internet Connection!", Toast.LENGTH_LONG).show();
            return;
        }

        input.setText("");
        adapter.addUserMessage(message);

        if (!CodePalAssistant.isPythonRelated(message)) {
            String rejectionMessage = CodePalAssistant.getRandomRejectionMessage();
            adapter.addAIMessage(rejectionMessage, true);
            return;
        }

        setLoadingState(true);
        sendToOpenAI(message);
    }
    private void sendToOpenAI(String prompt) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "gpt-5-nano");
            JSONArray messages = new JSONArray();

            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", CodePalAssistant.createPythonFocusedSystemMessage())
            );

            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", prompt)
            );
            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(CodePalAssistant.OPENAI_URL)
                    .addHeader("Authorization", CodePalAssistant.getAuthorization())
                    .post(body)
                    .build();

            running = client.newCall(request);
            running.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        setLoadingState(false);
                        adapter.addAIMessage(e.getMessage(), true);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            setLoadingState(false);
                            adapter.addAIMessage("Oops! Something went wrong!", true);
                        });
                    }
                    String responseBody = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String result = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        runOnUiThread(() -> {
                            setLoadingState(false);
                            adapter.addAIMessage(result, true);
                        });
                        //Log.d("OPENAI", result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void setupWelcomeMessage() {
        String welcomeMessage = "Hello! I'm CodePal, your Python programming assistant. " +
                "I can help you with Python syntax, libraries, code examples, debugging, and best practices. " +
                "What would you like to know about Python?";
        adapter.addAIMessage(welcomeMessage, false);
    }
    private void setLoadingState(boolean loading) {
        runOnUiThread(() -> {
            if (loading) {
                button.setEnabled(false);
                input.setEnabled(false);
                adapter.addAIMessage("Thinking...", false);
            } else {
                button.setEnabled(true);
                input.setEnabled(true);

                Conversation last = adapter.getLastMessage();
                if (last != null && last.getContent().equals("Thinking...")) {
                    adapter.removeLast();
                }
            }
        });
    }
    private void setupChatTitle(String prompt) {
        CHATID = database.storeChat(USERID, prompt);
        if (Network.isConnected(this)) {
            Chat chat = database.getChat(CHATID);
            firebase.storeChat(chat, USERID);
        }
        adapter.setChatId(CHATID);
    }
    private void loadConversation() {
        if (CHATID != null) {
            List<Conversation> loadedConversations = database.getConversations(CHATID);
            Log.d("DB_DEBUG", "Loaded " + loadedConversations.size() + " conversations");

            convos.clear();
            convos.addAll(loadedConversations);
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (running != null && !running.isCanceled()) {
            running.cancel();
        }
    }
}