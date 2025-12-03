package com.alexwan.csci310team36projectd;

import static org.junit.Assert.*;

import com.alexwan.csci310team36projectd.data.FilterState;
import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.model.PhotoElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.alexwan.csci310team36projectd.data.model.VoiceMemoElement;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Black-Box Tests for Feature 3: Fast Search and Filtering
 */
public class SearchFilterBlackBoxTest {

    // -------------------------------------------------------------
    // Black-Box Test 1: User can search notes by title
    // Verifies that searching by title returns the correct notes
    // -------------------------------------------------------------
    @Test
    public void test_userCanSearchNotesByTitle() {
        List<Note> notes = createTestNotes();

        FilterState searchState = new FilterState("Shopping", null, false, false, false);
        List<Note> matchingNotes = filterNotesByState(notes, searchState);
        
        assertTrue("Should find at least one note with 'Shopping' in title", 
                   matchingNotes.size() >= 1);
        
        // Verify all returned notes contain "Shopping" in title
        for (Note note : matchingNotes) {
            assertTrue("All returned notes should contain 'Shopping' in title",
                      note.title != null && note.title.toLowerCase().contains("shopping"));
        }
    }

    // -------------------------------------------------------------
    // Black-Box Test 2: User can search notes by body content
    // Verifies that searching by body text returns the correct notes
    // -------------------------------------------------------------
    @Test
    public void test_userCanSearchNotesByBodyContent() {
        List<Note> notes = createTestNotes();

        FilterState searchState = new FilterState("groceries", null, false, false, false);
        List<Note> matchingNotes = filterNotesByState(notes, searchState);

        // Should find notes where "groceries" appears in the body text
        assertTrue("Should find notes with 'groceries' in body content", 
                   matchingNotes.size() >= 1);

        // Verify at least one returned note has "groceries" in body
        boolean foundInBody = false;
        for (Note note : matchingNotes) {
            if (note.elements != null) {
                for (com.alexwan.csci310team36projectd.data.model.NoteElement element : note.elements) {
                    if (element instanceof TextElement) {
                        TextElement textElement = (TextElement) element;
                        if (textElement.getContent() != null && 
                            textElement.getContent().toLowerCase().contains("groceries")) {
                            foundInBody = true;
                            break;
                        }
                    }
                }
            }
        }
        assertTrue("Should find 'groceries' in note body content", foundInBody);
    }

    // -------------------------------------------------------------
    // Black-Box Test 3: User can filter notes by tags
    // Verifies that filtering by tags returns only notes with those tags
    // -------------------------------------------------------------
    @Test
    public void test_userCanFilterNotesByTags() {
        List<Note> notes = createTestNotes();

        FilterState filterState = new FilterState(null, Arrays.asList("work", "urgent"),
                                                   false, false, false);
        List<Note> matchingNotes = filterNotesByState(notes, filterState);

        // All returned notes must have BOTH "work" AND "urgent" tags
        for (Note note : matchingNotes) {
            assertNotNull("Note should have tags", note.tags);
            assertTrue("Note should contain 'work' tag", note.tags.contains("work"));
            assertTrue("Note should contain 'urgent' tag", note.tags.contains("urgent"));
        }

        // Verify notes without both tags are not included
        for (Note note : notes) {
            if (note.tags == null || 
                !note.tags.contains("work") || 
                !note.tags.contains("urgent")) {
                assertFalse("Notes without both tags should not be in results", 
                           matchingNotes.contains(note));
            }
        }
    }

    // -------------------------------------------------------------
    // Black-Box Test 4: User can filter notes by media type (has photo)
    // Verifies that filtering by "has photo" returns only notes with photos
    // -------------------------------------------------------------
    @Test
    public void test_userCanFilterNotesByHasPhoto() {
        List<Note> notes = createTestNotes();

        FilterState filterState = new FilterState(null, null, true, false, false);
        List<Note> matchingNotes = filterNotesByState(notes, filterState);

        // All returned notes must have at least one photo
        for (Note note : matchingNotes) {
            assertNotNull("Note should have elements", note.elements);
            boolean hasPhoto = false;
            for (com.alexwan.csci310team36projectd.data.model.NoteElement element : note.elements) {
                if (element instanceof PhotoElement) {
                    hasPhoto = true;
                    break;
                }
            }
            assertTrue("All returned notes should have a photo", hasPhoto);
        }

        // Verify notes without photos are not included
        for (Note note : notes) {
            boolean hasPhoto = false;
            if (note.elements != null) {
                for (com.alexwan.csci310team36projectd.data.model.NoteElement element : note.elements) {
                    if (element instanceof PhotoElement) {
                        hasPhoto = true;
                        break;
                    }
                }
            }
            if (!hasPhoto) {
                assertFalse("Notes without photos should not be in results", 
                           matchingNotes.contains(note));
            }
        }
    }

    // -------------------------------------------------------------
    // Black-Box Test 5: User can combine multiple filters
    // Verifies that combining text search, tags, and media filters works correctly
    // -------------------------------------------------------------
    @Test
    public void test_userCanCombineMultipleFilters() {
        List<Note> notes = createTestNotes();

        FilterState filterState = new FilterState("Shopping", Arrays.asList("work"),
                                                   true, false, false);
        List<Note> matchingNotes = filterNotesByState(notes, filterState);

        // All returned notes must satisfy ALL conditions:
        // 1. Title or body contains "Shopping"
        // 2. Has "work" tag
        // 3. Has at least one photo
        for (Note note : matchingNotes) {
            // Check text match
            boolean matchesText = false;
            if (note.title != null && note.title.toLowerCase().contains("shopping")) {
                matchesText = true;
            }
            if (!matchesText && note.elements != null) {
                for (com.alexwan.csci310team36projectd.data.model.NoteElement element : note.elements) {
                    if (element instanceof TextElement) {
                        TextElement textElement = (TextElement) element;
                        if (textElement.getContent() != null && 
                            textElement.getContent().toLowerCase().contains("shopping")) {
                            matchesText = true;
                            break;
                        }
                    }
                }
            }
            assertTrue("Note should match text search", matchesText);

            // Check tag match
            assertNotNull("Note should have tags", note.tags);
            assertTrue("Note should have 'work' tag", note.tags.contains("work"));

            // Check photo presence
            boolean hasPhoto = false;
            if (note.elements != null) {
                for (com.alexwan.csci310team36projectd.data.model.NoteElement element : note.elements) {
                    if (element instanceof PhotoElement) {
                        hasPhoto = true;
                        break;
                    }
                }
            }
            assertTrue("Note should have a photo", hasPhoto);
        }
    }

    // -------------------------------------------------------------
    // Black-Box Test 5: User can filter by date range
    // Verifies that filtering by date range returns only notes within the specified range
    // -------------------------------------------------------------
    @Test
    public void test_userCanFilterByDateRange() {
        List<Note> notes = createTestNotes();

        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 10, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        
        cal.set(2024, Calendar.JANUARY, 20, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();

        notes.get(0).lastEdited = createDate(2024, Calendar.JANUARY, 15);
        notes.get(1).lastEdited = createDate(2024, Calendar.JANUARY, 5);
        notes.get(2).lastEdited = createDate(2024, Calendar.JANUARY, 25);
        notes.get(3).lastEdited = createDate(2024, Calendar.JANUARY, 10);
        notes.get(4).lastEdited = createDate(2024, Calendar.JANUARY, 20);

        FilterState filterState = new FilterState(null, null, false, false, false, startDate, endDate);
        List<Note> matchingNotes = filterNotesByState(notes, filterState);

        // All returned notes must have lastEdited date within the range
        for (Note note : matchingNotes) {
            assertNotNull("Note should have lastEdited date", note.lastEdited);
            long noteTime = note.lastEdited.getTime();
            assertTrue("Note should be on or after start date", 
                      noteTime >= startDate.getTime());
            assertTrue("Note should be on or before end date", 
                      noteTime <= endDate.getTime());
        }

        // Verify notes outside range are not included
        for (Note note : notes) {
            if (note.lastEdited != null) {
                long noteTime = note.lastEdited.getTime();
                boolean inRange = noteTime >= startDate.getTime() && noteTime <= endDate.getTime();
                if (!inRange) {
                    assertFalse("Notes outside date range should not be in results", 
                               matchingNotes.contains(note));
                }
            }
        }
    }

    @Test
    public void test_userCanFilterByStartDateOnly() {
        List<Note> notes = createTestNotes();

        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        notes.get(0).lastEdited = createDate(2024, Calendar.JANUARY, 10);
        notes.get(1).lastEdited = createDate(2024, Calendar.JANUARY, 15);
        notes.get(2).lastEdited = createDate(2024, Calendar.JANUARY, 20);

        FilterState filterState = new FilterState(null, null, false, false, false, startDate, null);
        List<Note> matchingNotes = filterNotesByState(notes, filterState);

        // All returned notes should be on or after start date
        for (Note note : matchingNotes) {
            if (note.lastEdited != null) {
                assertTrue("Note should be on or after start date", 
                          note.lastEdited.getTime() >= startDate.getTime());
            }
        }
    }

    @Test
    public void test_userCanFilterByEndDateOnly() {
        List<Note> notes = createTestNotes();

        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();

        notes.get(0).lastEdited = createDate(2024, Calendar.JANUARY, 10);
        notes.get(1).lastEdited = createDate(2024, Calendar.JANUARY, 15);
        notes.get(2).lastEdited = createDate(2024, Calendar.JANUARY, 20);

        FilterState filterState = new FilterState(null, null, false, false, false, null, endDate);
        List<Note> matchingNotes = filterNotesByState(notes, filterState);

        // All returned notes should be on or before end date
        for (Note note : matchingNotes) {
            if (note.lastEdited != null) {
                assertTrue("Note should be on or before end date", 
                          note.lastEdited.getTime() <= endDate.getTime());
            }
        }
    }

    @Test
    public void test_userCanCombineDateRangeWithOtherFilters() {
        List<Note> notes = createTestNotes();

        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 10, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        
        cal.set(2024, Calendar.JANUARY, 20, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();

        notes.get(0).lastEdited = createDate(2024, Calendar.JANUARY, 15);
        notes.get(0).title = "Shopping List";
        notes.get(0).tags = Arrays.asList("work");
        notes.get(0).elements.add(new PhotoElement("/path/to/photo.jpg"));

        notes.get(1).lastEdited = createDate(2024, Calendar.JANUARY, 5);
        notes.get(1).title = "Shopping List";
        notes.get(1).tags = Arrays.asList("work");

        FilterState filterState = new FilterState("Shopping", Arrays.asList("work"), 
                                                   true, false, false, startDate, endDate);
        List<Note> matchingNotes = filterNotesByState(notes, filterState);

        // All returned notes must satisfy ALL conditions including date range
        for (Note note : matchingNotes) {
            // Check text match
            boolean matchesText = note.title != null && 
                                  note.title.toLowerCase().contains("shopping");
            assertTrue("Note should match text search", matchesText);

            // Check tag match
            assertNotNull("Note should have tags", note.tags);
            assertTrue("Note should have 'work' tag", note.tags.contains("work"));

            // Check photo presence
            boolean hasPhoto = false;
            if (note.elements != null) {
                for (com.alexwan.csci310team36projectd.data.model.NoteElement element : note.elements) {
                    if (element instanceof PhotoElement) {
                        hasPhoto = true;
                        break;
                    }
                }
            }
            assertTrue("Note should have a photo", hasPhoto);

            // Check date range
            assertNotNull("Note should have lastEdited date", note.lastEdited);
            long noteTime = note.lastEdited.getTime();
            assertTrue("Note should be within date range", 
                      noteTime >= startDate.getTime() && noteTime <= endDate.getTime());
        }
    }

    @Test
    public void test_userCanFilterNotesWithoutDateExcluded() {
        List<Note> notes = createTestNotes();

        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 10, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        
        cal.set(2024, Calendar.JANUARY, 20, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endDate = cal.getTime();

        notes.get(0).lastEdited = createDate(2024, Calendar.JANUARY, 15);
        notes.get(1).lastEdited = null;
        notes.get(2).lastEdited = createDate(2024, Calendar.JANUARY, 15);

        FilterState filterState = new FilterState(null, null, false, false, false, startDate, endDate);
        List<Note> matchingNotes = filterNotesByState(notes, filterState);

        // Notes without lastEdited date should be excluded
        for (Note note : matchingNotes) {
            assertNotNull("All returned notes should have lastEdited date", note.lastEdited);
        }

        // Verify note without date is not included
        assertFalse("Note without date should not be in results", 
                   matchingNotes.contains(notes.get(1)));
    }

    // Helper method to simulate filtering behavior
    private List<Note> filterNotesByState(List<Note> notes, FilterState state) {
        if (state == null || !state.areFiltersActive()) {
            return new ArrayList<>(notes);
        }

        List<Note> filtered = new ArrayList<>(notes);

        // Filter by text
        if (state.text != null && !state.text.trim().isEmpty()) {
            String lowerCaseQuery = state.text.toLowerCase();
            filtered.removeIf(note -> {
                boolean matches = false;
                if (note.title != null && note.title.toLowerCase().contains(lowerCaseQuery)) {
                    matches = true;
                }
                if (!matches && note.elements != null) {
                    for (com.alexwan.csci310team36projectd.data.model.NoteElement element : note.elements) {
                        if (element instanceof TextElement) {
                            TextElement textElement = (TextElement) element;
                            if (textElement.getContent() != null && 
                                textElement.getContent().toLowerCase().contains(lowerCaseQuery)) {
                                matches = true;
                                break;
                            }
                        }
                    }
                }
                return !matches;
            });
        }

        // Filter by tags
        if (state.tags != null && !state.tags.isEmpty()) {
            filtered.removeIf(note -> 
                note.tags == null || 
                !new java.util.HashSet<>(note.tags).containsAll(state.tags));
        }

        // Filter by hasPhoto
        if (state.hasPhoto) {
            filtered.removeIf(note -> {
                if (note.elements == null) return true;
                return note.elements.stream().noneMatch(e -> e instanceof PhotoElement);
            });
        }

        // Filter by hasVoiceMemo
        if (state.hasVoiceMemo) {
            filtered.removeIf(note -> {
                if (note.elements == null) return true;
                return note.elements.stream().noneMatch(e -> e instanceof VoiceMemoElement);
            });
        }

        // Filter by hasLocation
        if (state.hasLocation) {
            filtered.removeIf(note -> note.location == null);
        }

        // Filter by date range (based on lastEdited date)
        if (state.startDate != null || state.endDate != null) {
            filtered.removeIf(note -> {
                if (note.lastEdited == null) {
                    return true; // Exclude notes without lastEdited date
                }
                long noteTime = note.lastEdited.getTime();
                boolean afterStart = state.startDate == null || noteTime >= state.startDate.getTime();
                boolean beforeEnd = state.endDate == null || noteTime <= state.endDate.getTime();
                return !(afterStart && beforeEnd);
            });
        }

        return filtered;
    }

    private Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private List<Note> createTestNotes() {
        List<Note> notes = new ArrayList<>();

        Note note1 = new Note();
        note1.id = 1;
        note1.title = "Shopping List";
        note1.tags = Arrays.asList("work", "urgent");
        note1.elements = new ArrayList<>();
        note1.elements.add(new TextElement("Buy groceries tomorrow"));
        note1.elements.add(new PhotoElement("/path/to/photo1.jpg"));
        note1.isPinned = false;
        note1.dateCreated = new Date();
        note1.lastEdited = new Date();
        notes.add(note1);

        Note note2 = new Note();
        note2.id = 2;
        note2.title = "Work Tasks";
        note2.tags = Arrays.asList("work");
        note2.elements = new ArrayList<>();
        note2.elements.add(new TextElement("Complete project"));
        note2.isPinned = false;
        note2.dateCreated = new Date();
        note2.lastEdited = new Date();
        notes.add(note2);

        Note note3 = new Note();
        note3.id = 3;
        note3.title = "Personal Reminder";
        note3.tags = Arrays.asList("personal");
        note3.elements = new ArrayList<>();
        note3.elements.add(new TextElement("Call dentist"));
        note3.location = new com.alexwan.csci310team36projectd.data.Location();
        note3.location.latitude = 34.0;
        note3.location.longitude = -118.0;
        note3.location.setName("Home");
        note3.isPinned = false;
        note3.dateCreated = new Date();
        note3.lastEdited = new Date();
        notes.add(note3);

        Note note4 = new Note();
        note4.id = 4;
        note4.title = "Shopping for groceries";
        note4.tags = Arrays.asList("personal", "shopping");
        note4.elements = new ArrayList<>();
        note4.elements.add(new TextElement("Remember to buy milk"));
        note4.elements.add(new VoiceMemoElement("/path/to/voice.3gp"));
        note4.isPinned = false;
        note4.dateCreated = new Date();
        note4.lastEdited = new Date();
        notes.add(note4);

        Note note5 = new Note();
        note5.id = 5;
        note5.title = "Shopping Meeting Notes";
        note5.tags = Arrays.asList("work", "urgent");
        note5.elements = new ArrayList<>();
        note5.elements.add(new TextElement("Discuss shopping strategy"));
        note5.elements.add(new PhotoElement("/path/to/photo2.jpg"));
        note5.location = new com.alexwan.csci310team36projectd.data.Location();
        note5.location.latitude = 35.0;
        note5.location.longitude = -119.0;
        note5.location.setName("Office");
        note5.isPinned = false;
        note5.dateCreated = new Date();
        note5.lastEdited = new Date();
        notes.add(note5);

        return notes;
    }
}

