package com.example.codepal.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepal.Holders.CodeHolder;
import com.example.codepal.Holders.ConvoHolder;
import com.example.codepal.Models.Conversation;
import com.example.codepal.R;
import com.example.codepal.Services.Database;
import com.example.codepal.Services.Firebase;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConvoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Conversation> messages;
    private String id;
    private String userId;
    private Database database;
    private Context context;
    private String template;
    private Firebase firebase;
    public ConvoAdapter(Context context, String id, List<Conversation> messages, Database database, Firebase firebase, String userId) {
        this.context = context;
        this.id = id;
        this.messages = messages;
        this.database = database;
        this.firebase = firebase;
        this.userId = userId;
        template = loadAssets();
    }
    public void addMessage(Conversation message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    public void addAIMessage(String message, boolean store) {
        List<Conversation> parse = parseMessage(message);
        for (Conversation item : parse) {
            if (store && this.id != null) {
                String uid = database.storeConvo(this.id, item.getContent(), item.getType());
                firebase.storeConvo(userId, id, uid, item.getContent(), item.getType());
            }
            addMessage(item);
        }
        notifyItemRangeInserted(messages.size(), parse.size());
    }
    public void addUserMessage(String message) {
        if (this.id != null) {
            String uid = database.storeConvo(this.id, message, Conversation.TYPE_USER);
            firebase.storeConvo(userId, id, uid, message, Conversation.TYPE_USER);
        }
        addMessage(new Conversation(message, Conversation.TYPE_USER, ""));
    }
    public void setChatId(String chatId) {
        this.id = chatId;
    }
    public void removeLast() {
        if (!messages.isEmpty()) {
            messages.remove(messages.size() - 1);
            notifyItemRemoved(messages.size());
        }
    }
    public Conversation getLastMessage() {
        if (messages.isEmpty()) return null;
        return messages.get(messages.size() - 1);
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == Conversation.TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
            return new ConvoHolder(view);
        } else if (viewType == Conversation.TYPE_AI) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_ai, parent, false);
            return new ConvoHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_code, parent, false);
            return new CodeHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Conversation message = messages.get(position);
        if (message.isCode()) {
            CodeHolder h = (CodeHolder) holder;
            String html = this.template.replace("@CODE@", message.getContent());
            h.webView.loadDataWithBaseURL("file:///android_asset/prism/", html, "text/html", "utf-8", null);
        } else {
            ((ConvoHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }
    private String loadAssets() {
        try {
            String fileName = "prism/prism_template.html";
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();;
            return "<html><body>Error...</body></html>";
        }
    }
    private List<Conversation> parseMessage(String message) {
        List<Conversation> result = new ArrayList<>();

        String[] parts = message.split("```");

        for (int i = 0; i < parts.length; i++) {

            if (i % 2 == 0) {
                // normal text chunk
                if (!parts[i].trim().isEmpty()) {
                    result.add(new Conversation(parts[i].trim(), Conversation.TYPE_AI, ""));
                }
            } else {
                // code block chunk
                String block = parts[i];

                // Remove "python", "java", etc.
                int nl = block.indexOf("\n");
                if (nl != -1) {
                    block = block.substring(nl).trim();
                }

                result.add(new Conversation(block, Conversation.TYPE_CODE, ""));
            }
        }
        return result;
    }
}
