package com.example.codepal.Holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepal.Interfaces.NoteInterface;
import com.example.codepal.Models.Note;
import com.example.codepal.R;

public class NoteHolder extends RecyclerView.ViewHolder {
    private TextView title, preview, date, icon;
    public NoteHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        preview = itemView.findViewById(R.id.preview);
        date = itemView.findViewById(R.id.date);
        icon = itemView.findViewById(R.id.icon);
    }
    public void bind(final Note note, final NoteInterface listener) {
        title.setText(note.getTitle());
        preview.setText(note.getPreview());
        date.setText(note.getFormattedDate());

        if (note.isPinned()) {
            icon.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
        }

        itemView.setOnClickListener(v -> {
            listener.onClickNote(note);
        });

        itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onLongClickNote(note);
                return true;
            }
            return false;
        });
    }
}
