package com.alexwan.csci310_team36_projectd.data.model;

import android.text.SpannableString;
import androidx.core.text.HtmlCompat;

import java.util.UUID; // Needed for UUID.randomUUID()

public class TextElement extends NoteElement {

    private String content; // Storing content as HTML String for persistence

    // No-arg constructor for Room/Gson
    public TextElement() {
        super(UUID.randomUUID().toString(), System.currentTimeMillis());
        this.content = ""; // Initialize with empty string
    }

    // Constructor for raw string content (e.g., initial creation from simple text)
    public TextElement(String text) {
        super(UUID.randomUUID().toString(), System.currentTimeMillis());
        // Convert plain text to HTML for internal storage
        this.content = HtmlCompat.toHtml(new SpannableString(text), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
    }

    // Constructor for SpannableString content (e.g., initial creation from rich text)
    public TextElement(SpannableString spannableContent) {
        super(UUID.randomUUID().toString(), System.currentTimeMillis());
        // Convert SpannableString to HTML for internal storage
        this.content = HtmlCompat.toHtml(spannableContent, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
    }

    // NEW CONSTRUCTOR: Used by Converters.java for deserialization and temporary serialization objects
    public TextElement(String id, long timestamp, String htmlContent) {
        super(id, timestamp);
        this.content = htmlContent; // Store the HTML string directly
    }

    // Returns the content as an HTML String (for persistence)
    public String getContent() {
        return content;
    }

    // Sets the content, converting SpannableString to HTML for internal storage
    public void setContent(SpannableString spannableContent) {
        this.content = HtmlCompat.toHtml(spannableContent, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
    }

    // Helper method to get content as SpannableString for UI display
    public SpannableString getContentAsSpannable() {
        return new SpannableString(HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    @Override
    public String getType() {
        return "text";
    }

    public boolean isEmpty() {
        return content == null || content.isEmpty(); // Check String content for emptiness
    }
}
