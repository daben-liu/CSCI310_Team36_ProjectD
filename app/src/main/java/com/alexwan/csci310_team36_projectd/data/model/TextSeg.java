package com.alexwan.csci310_team36_projectd.data.model;

import android.graphics.Typeface;

public class TextSeg {
    private String text;
    private boolean isBold;
    private boolean isItalics;
    private int fontSize;

    // Full constructor
    public TextSeg(String text, boolean isBold, boolean isItalics, int fontSize) {
        this.text = text;
        this.isBold = isBold;
        this.isItalics = isItalics;
        this.fontSize = fontSize;
    }

    // Simplified constructor for plain text, providing default values.
    public TextSeg(String text) {
        this(text, false, false, 14); // Default style: not bold, not italic, size 14.
    }

    // --- Getters ---
    public String getText() {
        return text;
    }

    public boolean isBold() {
        return isBold;
    }

    public boolean isItalics() {
        return isItalics;
    }

    public int getFontSize() {
        return fontSize;
    }

    // --- Setters ---
    public void setText(String text) {
        this.text = text;
    }

    public void setBold(boolean bold) {
        isBold = bold;
    }

    public void setItalics(boolean italics) {
        isItalics = italics;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    // --- Helper method to calculate the Android Typeface style ---
    public int getStyle() {
        if (isBold && isItalics) {
            return Typeface.BOLD_ITALIC;
        } else if (isBold) {
            return Typeface.BOLD;
        } else if (isItalics) {
            return Typeface.ITALIC;
        } else {
            return Typeface.NORMAL;
        }
    }
}
