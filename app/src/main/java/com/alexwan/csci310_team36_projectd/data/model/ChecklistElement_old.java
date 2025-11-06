package com.alexwan.csci310_team36_projectd.data.model;

import java.util.ArrayList;
import java.util.List;

public class ChecklistElement_old extends NoteElement {
    private List<ChecklistItem> items;

    public ChecklistElement_old() {
        this.items = new ArrayList<>();
    }

    public ChecklistElement_old(List<ChecklistItem> items) {
        this.items = items;
    }

    public List<ChecklistItem> getItems() {
        return items;
    }

    @Override
    public String getType() {
        return "checklist";
    }

    public static class ChecklistItem {
        private boolean isChecked;
        private String text;

        public ChecklistItem(String text) {
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
    }
}
