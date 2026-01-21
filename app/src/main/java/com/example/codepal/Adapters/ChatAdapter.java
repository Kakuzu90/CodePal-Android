package com.example.codepal.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepal.Holders.ChatHolder;
import com.example.codepal.Interfaces.ChatInterface;
import com.example.codepal.Models.Chat;
import com.example.codepal.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatHolder> {
    private List<Chat> chats;
    private ChatInterface listener;
    public ChatAdapter(List<Chat> chats, ChatInterface listener) {
        this.chats = chats;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat, listener);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }
    public void updateChats(List<Chat> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }
}
