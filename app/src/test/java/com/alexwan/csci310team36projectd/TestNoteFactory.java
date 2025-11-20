package com.alexwan.csci310team36projectd;

import com.alexwan.csci310team36projectd.data.Location;
import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.model.ChecklistElement;
import com.alexwan.csci310team36projectd.data.model.NoteElement;
import com.alexwan.csci310team36projectd.data.model.PhotoElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TestNoteFactory {

    public static Note noteWithTitle(String t) {
        Note note = new Note();
        note.title = t;
        note.elements = new ArrayList<>();
        note.dateCreated = new Date();
        note.lastEdited = new Date();
        return note;
    }

    public static Note noteWithText(String t) {
        Note note = noteWithTitle("Note with text");
        TextElement textElement = new TextElement(t);
        note.elements.add(textElement);
        return note;
    }

    public static Note noteWithChecklist(String... items) {
        Note note = noteWithTitle("Note with checklist");
        List<ChecklistElement.ChecklistItem> checklistItems = new ArrayList<>();
        for (String item : items) {
            checklistItems.add(new ChecklistElement.ChecklistItem(item));
        }
        ChecklistElement checklistElement = new ChecklistElement(checklistItems);
        note.elements.add(checklistElement);
        return note;
    }

    public static Note noteWithPhoto() {
        Note note = noteWithTitle("Note with photo");
        PhotoElement photoElement = new PhotoElement("path/to/photo.jpg");
        note.elements.add(photoElement);
        return note;
    }

    public static void advanceEdit(Note n) {
        n.lastEdited = new Date(System.currentTimeMillis() + 1000); // Advance time
    }

    public static void addTag(Note n, String tag) {
        if (n.tags == null) {
            n.tags = new ArrayList<>();
        }
        n.tags.add(tag);
    }

    public static Location sampleLocation(String name) {
        return new Location(34.0224, -118.2851, name); // Sample USC location
    }
}
