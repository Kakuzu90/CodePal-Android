package com.example.codepal.Holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepal.Models.Conversation;
import com.example.codepal.R;

public class ConvoHolder extends RecyclerView.ViewHolder {
    TextView message;
    public ConvoHolder(@NonNull View itemView) {
        super(itemView);
        message = itemView.findViewById(R.id.message_text);
    }
    public void bind(Conversation conversation) {
        message.setText(conversation.getContent());
    }
}
