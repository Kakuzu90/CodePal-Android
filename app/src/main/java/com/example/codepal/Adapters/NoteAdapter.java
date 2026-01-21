package com.example.codepal.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepal.Holders.NoteHolder;
import com.example.codepal.Interfaces.NoteInterface;
import com.example.codepal.Models.Note;
import com.example.codepal.R;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteHolder> {
    private List<Note> notes;
    private NoteInterface listener;
    public NoteAdapter(List<Note> notes, NoteInterface listener) {
        this.notes = notes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        Note note = notes.get(position);
        holder.bind(note, listener);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void updateNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }
}
