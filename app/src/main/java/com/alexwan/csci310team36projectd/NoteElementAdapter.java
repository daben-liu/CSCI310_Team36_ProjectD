package com.alexwan.csci310team36projectd;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexwan.csci310team36projectd.data.model.ChecklistElement;
import com.alexwan.csci310team36projectd.data.model.NoteElement;
import com.alexwan.csci310team36projectd.data.model.PhotoElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.alexwan.csci310team36projectd.data.model.VoiceMemoElement;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NoteElementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnTextElementFocusChangeListener {
        void onTextElementFocusChanged(EditText focusedEditText);
    }

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_CHECKLIST = 1;
    private static final int TYPE_PHOTO = 2;
    private static final int TYPE_VOICE_MEMO = 3;

    private final Context context;
    private final List<NoteElement> elements;
    private final OnTextElementFocusChangeListener focusChangeListener;
    private EditText focusedEditText = null;
    private enum PlayerState {IDLE, PREPARING, PLAYING, PAUSED}

    public NoteElementAdapter(Context context, List<NoteElement> elements, OnTextElementFocusChangeListener listener) {
        this.context = context;
        this.elements = elements;
        this.focusChangeListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        NoteElement element = elements.get(position);
        if (element instanceof TextElement) return TYPE_TEXT;
        if (element instanceof ChecklistElement) return TYPE_CHECKLIST;
        if (element instanceof PhotoElement) return TYPE_PHOTO;
        if (element instanceof VoiceMemoElement) return TYPE_VOICE_MEMO;
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_TEXT:
                return new TextElementViewHolder(inflater.inflate(R.layout.item_text_element, parent, false));
            case TYPE_PHOTO:
                return new PhotoElementViewHolder(inflater.inflate(R.layout.item_photo_element, parent, false));
            case TYPE_CHECKLIST:
                return new ChecklistElementViewHolder(inflater.inflate(R.layout.item_checklist, parent, false));
            case TYPE_VOICE_MEMO:
                return new VoiceMemoElementViewHolder(inflater.inflate(R.layout.item_voice_memo_element, parent, false));
            default:
                return new TextElementViewHolder(inflater.inflate(R.layout.item_text_element, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_TEXT:
                ((TextElementViewHolder) holder).bind((TextElement) elements.get(position));
                break;
            case TYPE_PHOTO:
                ((PhotoElementViewHolder) holder).bind((PhotoElement) elements.get(position));
                break;
            case TYPE_CHECKLIST:
                ((ChecklistElementViewHolder) holder).bind((ChecklistElement) elements.get(position));
                break;
            case TYPE_VOICE_MEMO:
                ((VoiceMemoElementViewHolder) holder).bind((VoiceMemoElement) elements.get(position));
                break;
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof VoiceMemoElementViewHolder) {
            ((VoiceMemoElementViewHolder) holder).releasePlayer();
        }
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    public List<NoteElement> getElements() {
        return elements;
    }

    public void setFocusedEditText(EditText editText) {
        this.focusedEditText = editText;
    }

    // Methods for toolbar actions
    public void toggleBold() {
        if (focusedEditText != null) {
            int start = focusedEditText.getSelectionStart();
            int end = focusedEditText.getSelectionEnd();
            Spannable spannable = focusedEditText.getText();
            StyleSpan[] styleSpans = spannable.getSpans(start, end, StyleSpan.class);
            boolean isBold = false;
            for (StyleSpan span : styleSpans) {
                if (span.getStyle() == Typeface.BOLD) {
                    isBold = true;
                    break;
                }
            }
            if (isBold) {
                for (StyleSpan span : styleSpans) {
                    if (span.getStyle() == Typeface.BOLD) {
                        spannable.removeSpan(span);
                    }
                }
            } else {
                spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            TextElement textElement = (TextElement) focusedEditText.getTag();
            if (textElement != null) {
                textElement.setContent(new SpannableString(spannable));
            }
        }
    }

    public void toggleItalic() {
        if (focusedEditText != null) {
            int start = focusedEditText.getSelectionStart();
            int end = focusedEditText.getSelectionEnd();
            Spannable spannable = focusedEditText.getText();
            StyleSpan[] styleSpans = spannable.getSpans(start, end, StyleSpan.class);
            boolean isItalic = false;
            for (StyleSpan span : styleSpans) {
                if (span.getStyle() == Typeface.ITALIC) {
                    isItalic = true;
                    break;
                }
            }
            if (isItalic) {
                for (StyleSpan span : styleSpans) {
                    if (span.getStyle() == Typeface.ITALIC) {
                        spannable.removeSpan(span);
                    }
                }
            } else {
                spannable.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            TextElement textElement = (TextElement) focusedEditText.getTag();
            if (textElement != null) {
                textElement.setContent(new SpannableString(spannable));
            }
        }
    }

    public void applyFontSize(int size) {
        if (focusedEditText != null) {
            int start = focusedEditText.getSelectionStart();
            int end = focusedEditText.getSelectionEnd();
            Spannable spannable = focusedEditText.getText();
            AbsoluteSizeSpan[] oldSizeSpans = spannable.getSpans(start, end, AbsoluteSizeSpan.class);
            for (AbsoluteSizeSpan span : oldSizeSpans) {
                spannable.removeSpan(span);
            }
            spannable.setSpan(new AbsoluteSizeSpan(size, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextElement textElement = (TextElement) focusedEditText.getTag();
            if (textElement != null) {
                textElement.setContent(new SpannableString(spannable));
            }
        }
    }

    public void addChecklistItemOrChecklist() {
        ChecklistElement checklistToUpdate = null;
        int checklistPosition = -1;
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i) instanceof ChecklistElement) {
                checklistToUpdate = (ChecklistElement) elements.get(i);
                checklistPosition = i;
                break;
            }
        }

        if (checklistToUpdate != null) {
            checklistToUpdate.getItems().add(new ChecklistElement.ChecklistItem(""));
            notifyItemChanged(checklistPosition);
        } else {
            elements.add(new ChecklistElement());
            notifyItemInserted(elements.size() - 1);
        }
    }


    class TextElementViewHolder extends RecyclerView.ViewHolder {
        EditText editText;
        private CustomTextWatcher textWatcher;

        TextElementViewHolder(@NonNull View itemView) {
            super(itemView);
            editText = itemView.findViewById(R.id.text_element_content);
            this.textWatcher = new CustomTextWatcher();
            this.editText.addTextChangedListener(textWatcher);

            editText.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    setFocusedEditText(editText);
                    if (focusChangeListener != null) {
                        focusChangeListener.onTextElementFocusChanged(editText);
                    }
                }
            });
        }

        void bind(TextElement element) {
            editText.setTag(element);
            textWatcher.setElement(element);
            editText.setText(element.getContentAsSpannable());
        }
    }

    class PhotoElementViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton;

        PhotoElementViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo_element_image);
            deleteButton = itemView.findViewById(R.id.delete_photo_button);
        }

        void bind(PhotoElement element) {
            Glide.with(context)
                    .load(element.getFilePath())
                    .into(imageView);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            imageView.getLayoutParams().width = width / 2;
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    PhotoElement photoElement = (PhotoElement) elements.get(position);
                    File file = new File(photoElement.getFilePath());
                    if (file.exists()) {
                        file.delete();
                    }
                    elements.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }
    }

    class ChecklistElementViewHolder extends RecyclerView.ViewHolder {
        RecyclerView itemsRecyclerView;
        private ChecklistItemAdapter itemAdapter;

        ChecklistElementViewHolder(@NonNull View itemView) {
            super(itemView);
            itemsRecyclerView = itemView.findViewById(R.id.checklist_items_recycler_view);
            itemsRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        }

        void bind(ChecklistElement element) {
            itemAdapter = new ChecklistItemAdapter(element.getItems());
            itemsRecyclerView.setAdapter(itemAdapter);
        }
    }

    class VoiceMemoElementViewHolder extends RecyclerView.ViewHolder {
        ImageButton playButton;
        ImageButton deleteButton;
        TextView durationTextView;
        MediaPlayer mediaPlayer;

        private PlayerState playerState = PlayerState.IDLE;
        private String audioPath;

        VoiceMemoElementViewHolder(@NonNull View itemView) {
            super(itemView);
            playButton = itemView.findViewById(R.id.play_memo_button);
            deleteButton = itemView.findViewById(R.id.delete_memo_button);
            durationTextView = itemView.findViewById(R.id.memo_duration_text);
            playButton.setOnClickListener(v -> handlePlayClick());
            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    releasePlayer(); // Stop playback before deleting
                    VoiceMemoElement element = (VoiceMemoElement) elements.get(position);
                    File file = new File(element.getFilePath());
                    if (file.exists()) {
                        file.delete();
                    }
                    elements.remove(position);
                    notifyItemRemoved(position);
                }
            });
        }

        void bind(VoiceMemoElement element) {
            this.audioPath = element.getFilePath();
            releasePlayer(); // Resets player state and UI to default

            if (audioPath != null) {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                try {
                    retriever.setDataSource(audioPath);
                    String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    if (durationStr != null) {
                        long durationMs = Long.parseLong(durationStr);
                        long minutes = (durationMs / 1000) / 60;
                        long seconds = (durationMs / 1000) % 60;
                        durationTextView.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                    }
                } catch (Exception e) {
                    durationTextView.setText("??:??"); // Indicate error
                } finally {
                    try {
                        retriever.release();
                    } catch (IOException e) {
                        // Should not happen with setDataSource(String) but good practice
                        e.printStackTrace();
                    }
                }
            }
        }

        private void handlePlayClick() {
            switch (playerState) {
                case IDLE:
                    startPlaying();
                    break;
                case PREPARING:
                    Toast.makeText(context, "Loading audio...", Toast.LENGTH_SHORT).show();
                    break;
                case PLAYING:
                    pausePlaying();
                    break;
                case PAUSED:
                    resumePlaying();
                    break;
            }
        }

        private void startPlaying() {
            if (audioPath == null) return;

            playerState = PlayerState.PREPARING;
            playButton.setEnabled(false);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
            try {
                mediaPlayer.setDataSource(audioPath);
                mediaPlayer.setOnPreparedListener(mp -> {
                    playerState = PlayerState.PLAYING;
                    playButton.setEnabled(true);
                    playButton.setImageResource(android.R.drawable.ic_media_pause);
                    mp.start();
                });
                mediaPlayer.setOnCompletionListener(mp -> releasePlayer());
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    releasePlayer();
                    Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show();
                    return true;
                });
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                releasePlayer();
            }
        }

        private void pausePlaying() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playerState = PlayerState.PAUSED;
                playButton.setImageResource(android.R.drawable.ic_media_play);
            }
        }

        private void resumePlaying() {
            if (mediaPlayer != null) {
                mediaPlayer.start();
                playerState = PlayerState.PLAYING;
                playButton.setImageResource(android.R.drawable.ic_media_pause);
            }
        }

        public void releasePlayer() {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }
            playerState = PlayerState.IDLE;
            playButton.setEnabled(true);
            playButton.setImageResource(android.R.drawable.ic_media_play);
            durationTextView.setText("--:--");
        }
    }

    private static class CustomTextWatcher implements TextWatcher {
        private TextElement element;

        public void setElement(TextElement element) {
            this.element = element;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (element != null) {
                element.setContent(new SpannableString(s));
            }
        }
    }
}
