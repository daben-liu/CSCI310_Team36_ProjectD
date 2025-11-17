package com.alexwan.csci310team36projectd;

import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.model.NoteElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class NoteAdapter extends ListAdapter<Note, NoteAdapter.NoteViewHolder> {

    private final OnNoteClickListener onClickListener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public NoteAdapter(OnNoteClickListener onClickListener) {
        super(DIFF_CALLBACK);
        this.onClickListener = onClickListener;
    }

    private static final DiffUtil.ItemCallback<Note> DIFF_CALLBACK = new DiffUtil.ItemCallback<Note>() {
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.equals(newItem);
        }
    };

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = getItem(position);
        holder.bind(note);
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
            SpannableStringBuilder spannablePreviewBuilder = new SpannableStringBuilder();
            if (note.elements != null) {
                for (NoteElement element : note.elements) {
                    if (element instanceof TextElement) {
                        spannablePreviewBuilder.append(((TextElement) element).getContentAsSpannable());
                        spannablePreviewBuilder.append(" "); // Add a space between elements
                    }
                    // You might want to handle other element types for preview as well
                }
            }
            
            // Truncate to 20 characters, preserving rich text
            CharSequence previewText;
            if (spannablePreviewBuilder.length() > 20) {
                previewText = spannablePreviewBuilder.subSequence(0, 20);
            } else {
                previewText = spannablePreviewBuilder;
            }

            if (previewText.length() == 0) {
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
