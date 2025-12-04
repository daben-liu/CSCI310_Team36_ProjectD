package com.alexwan.csci310team36projectd.data.model;

import android.text.SpannableString;

public class TextElement extends NoteElement {

    private SpannableString content;
    private String contentString;

    public TextElement(String text) {
        this.content = new SpannableString(text);
        this.contentString = text;
    }

    // New constructor for use in the TypeConverter
    public TextElement(String id, long timestamp, String content) {
        super(id, timestamp);
        this.content = new SpannableString(content);
        this.contentString = content;
    }

    public void setContent(SpannableString content) {
        this.content = content;
        this.contentString = content != null ? content.toString() : null;;
    }

    public SpannableString getContentAsSpannable() {
        return content;
    }

    public String getContent() {
        return contentString;
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }

    public boolean isEmpty() {
        return content == null || content.length() == 0;
    }
}
