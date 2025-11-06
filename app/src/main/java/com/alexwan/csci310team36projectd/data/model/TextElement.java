package com.alexwan.csci310team36projectd.data.model;

import android.text.SpannableString;

public class TextElement extends NoteElement {

    private SpannableString content;

    public TextElement(String text) {
        this.content = new SpannableString(text);
    }

    public TextElement(SpannableString content) {
        this.content = content;
    }

    public SpannableString getContent() {
        return content;
    }

    public void setContent(SpannableString content) {
        this.content = content;
    }

    @Override
    public String getType() {
        return "text";
    }

    public boolean isEmpty() {
        return content == null || content.length() == 0;
    }
}
