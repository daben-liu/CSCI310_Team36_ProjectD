package com.alexwan.csci310_team36_projectd;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.TextWatcher;
import android.graphics.Typeface;
import android.text.style.AbsoluteSizeSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alexwan.csci310_team36_projectd.data.model.ChecklistElement;
import com.alexwan.csci310_team36_projectd.data.model.NoteElement;
import com.alexwan.csci310_team36_projectd.data.model.PhotoElement;
import com.alexwan.csci310_team36_projectd.data.model.TextElement;
import com.alexwan.csci310_team36_projectd.data.model.VoiceMemoElement;
import com.bumptech.glide.Glide;
import java.util.List;

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
                 return new ChecklistElementViewHolder(inflater.inflate(R.layout.item_checklist_element, parent, false));
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

            focusedEditText.setText(spannable);
            focusedEditText.setSelection(start, end);
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

            focusedEditText.setText(spannable);
            focusedEditText.setSelection(start, end);
        }
    }

    public void applyFontSize(int size) {
        if (focusedEditText != null) {
            int start = focusedEditText.getSelectionStart();
            int end = focusedEditText.getSelectionEnd();
            Spannable spannable = focusedEditText.getText();
            spannable.setSpan(new AbsoluteSizeSpan(size, true), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            focusedEditText.setText(spannable);
            focusedEditText.setSelection(start, end);
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
                    if (focusChangeListener != null) {
                        focusChangeListener.onTextElementFocusChanged(editText);
                    }
                }
            });
        }

        void bind(TextElement element) {
            textWatcher.setElement(element);
            editText.setText(element.getContent());
        }
    }

    class PhotoElementViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        PhotoElementViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo_element_image);
        }

        void bind(PhotoElement element) {
            Glide.with(context)
                 .load(element.getFilePath())
                 .into(imageView);
        }
    }

    class ChecklistElementViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        EditText editText;

        ChecklistElementViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checklist_item_checkbox);
            editText = itemView.findViewById(R.id.checklist_item_text);
        }

        void bind(ChecklistElement element) {
            editText.setText(element.getText());
            checkBox.setChecked(element.isChecked());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                element.setChecked(isChecked);
            });

            editText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) {
                    element.setText(s.toString());
                }
            });

            editText.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (editText.getText().toString().isEmpty()) {
                        int position = getAbsoluteAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            elements.remove(position);
                            notifyItemRemoved(position);
                            // Optionally move focus to previous item
                        }
                        return true;
                    }
                }
                return false;
            });
        }
    }

    class VoiceMemoElementViewHolder extends RecyclerView.ViewHolder {
        VoiceMemoElementViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void bind(VoiceMemoElement element) {
        }
    }

    private static class CustomTextWatcher implements TextWatcher {
        private TextElement element;

        public void setElement(TextElement element) {
            this.element = element;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if (element != null) {
                // Create a SpannableStringBuilder from the Editable to preserve spans
                // Then convert it back to SpannableString to match TextElement's content type
                element.setContent(new SpannableString(new SpannableStringBuilder(s)));
            }
        }
    }
}
