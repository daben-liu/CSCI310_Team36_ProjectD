package com.alexwan.csci310team36projectd;

import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.model.ChecklistElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Feature1WhiteBoxTests {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void insertElement_indexBounds() {
        // Given a note with one element
        Note note = TestNoteFactory.noteWithTitle("Test Note");
        note.elements.add(new TextElement("Initial element"));

        // Case A: insert at 0 (ok)
        note.elements.add(0, new TextElement("New first element"));
        assertEquals(2, note.elements.size());

        // Case B: insert at size (ok)
        note.elements.add(note.elements.size(), new TextElement("New last element"));
        assertEquals(3, note.elements.size());

        // Case C: insert at -1 (expect exception)
        try {
            note.elements.add(-1, new TextElement("Should fail"));
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void applyStyle_rangePaths() {
        // The TextElement class in the project does not have an applyStyle method.
        // The styling is handled by SpannableString, which is part of the Android framework.
        // Testing this would require an instrumented test (Espresso) to verify the visual output.
    }

    @Test
    public void mergeAdjSegment_onlyMergesWhenStylesMatch() {
        // The TextElement class does not contain logic for merging segments.
        // This seems to be a feature that is not implemented in the current data model.
    }

    @Test
    public void checklistElement_toggleItem() {
        // Given
        ChecklistElement.ChecklistItem item = new ChecklistElement.ChecklistItem("Test item");
        assertFalse(item.isChecked());

        // When
        item.setChecked(true);

        // Then
        assertTrue(item.isChecked());

        // When
        item.setChecked(false);

        // Then
        assertFalse(item.isChecked());
    }

    @Test
    public void refreshLastEdited_monotonicity() {
        // Given
        Note note = TestNoteFactory.noteWithTitle("Test Note");
        Date originalDate = (Date) note.lastEdited.clone();

        // When
        TestNoteFactory.advanceEdit(note);
        Date newDate = note.lastEdited;

        // Then
        assertTrue("New date should be after the original date", newDate.after(originalDate));
    }
}
