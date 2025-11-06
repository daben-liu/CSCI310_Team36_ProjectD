package com.alexwan.csci310_team36_projectd.data.model;

import java.util.ArrayList;
import java.util.List;

public class ChecklistElement extends NoteElement {
    private boolean isChecked;
    private String text;

    public ChecklistElement(String text) {
        this.isChecked = false;
        this.text = text;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getText() {
        return text;
    }
    public void setText(String newText) { text = newText; }
    @Override
    public String getType() {
        return "checklist";
    }
}
