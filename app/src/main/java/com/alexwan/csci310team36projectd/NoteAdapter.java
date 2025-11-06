package com.alexwan.csci310team36projectd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.model.NoteElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private List<Note> notes;
    private final OnNoteClickListener onClickListener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public NoteAdapter(List<Note> notes, OnNoteClickListener onClickListener) {
        this.notes = notes;
        this.onClickListener = onClickListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.bind(note);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView title, preview, location;
        ChipGroup tags;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.note_title);
            preview = itemView.findViewById(R.id.note_preview);
            tags = itemView.findViewById(R.id.note_tags);
            location = itemView.findViewById(R.id.note_location);
        }

        void bind(final Note note) {
            title.setText(note.title);

            // Content Preview
            StringBuilder previewBuilder = new StringBuilder();
            if (note.elements != null) {
                for (NoteElement element : note.elements) {
                    if (element instanceof TextElement) {
                        previewBuilder.append(((TextElement) element).getContent());
                        previewBuilder.append(" ");
                    }
                }
            }
            String previewText = previewBuilder.toString().trim();
            if (previewText.isEmpty()) {
                preview.setVisibility(View.GONE);
            } else {
                preview.setVisibility(View.VISIBLE);
                preview.setText(previewText);
            }

            // Tags
            tags.removeAllViews();
            if (note.tags != null && !note.tags.isEmpty()) {
                tags.setVisibility(View.VISIBLE);
                for (String tag : note.tags) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(tag);
                    tags.addView(chip);
                }
            } else {
                tags.setVisibility(View.GONE);
            }

            // Location
            if (note.location != null && note.location.name != null && !note.location.name.isEmpty()) {
                location.setText(note.location.name);
                location.setVisibility(View.VISIBLE);
            } else {
                location.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> onClickListener.onNoteClick(note));
        }
    }
}
