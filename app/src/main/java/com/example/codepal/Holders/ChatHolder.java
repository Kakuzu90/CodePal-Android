package com.example.codepal.Holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepal.Interfaces.ChatInterface;
import com.example.codepal.Models.Chat;
import com.example.codepal.R;

public class ChatHolder extends RecyclerView.ViewHolder {
    TextView titleText, dateText;
    public ChatHolder(@NonNull View itemView) {
        super(itemView);
        titleText = itemView.findViewById(R.id.title);
        dateText = itemView.findViewById(R.id.date);
    }
    public void bind(Chat chat, ChatInterface listener) {
        titleText.setText(chat.getPreviewTitle());
        dateText.setText(chat.getCreatedAt());

        itemView.setOnClickListener(v -> {
            listener.onClickChat(chat);
        });
    }
}
