package com.alexwan.csci310team36projectd.data.model;

import android.text.SpannableString;

public class TextElement extends NoteElement {

    private SpannableString content;

    public TextElement(String text) {
        this.content = new SpannableString(text);
    }

    // New constructor for use in the TypeConverter
    public TextElement(String id, long timestamp, String content) {
        super(id, timestamp);
        this.content = new SpannableString(content);
    }

    public void setContent(SpannableString content) {
        this.content = content;
    }

    public SpannableString getContentAsSpannable() {
        return content;
    }

    public String getContent() {
        return content != null ? content.toString() : null;
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }

    public boolean isEmpty() {
        return content == null || content.length() == 0;
    }
}
