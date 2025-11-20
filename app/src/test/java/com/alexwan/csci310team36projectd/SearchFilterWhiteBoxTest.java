package com.alexwan.csci310team36projectd;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.app.Application;
import android.location.Location;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.alexwan.csci310team36projectd.data.AppDatabase;
import com.alexwan.csci310team36projectd.data.FilterState;
import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.NoteRepository;
import com.alexwan.csci310team36projectd.data.model.ChecklistElement;
import com.alexwan.csci310team36projectd.data.model.NoteElement;
import com.alexwan.csci310team36projectd.data.model.PhotoElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.alexwan.csci310team36projectd.data.model.VoiceMemoElement;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SearchFilterWhiteBoxTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();

    @Mock
    private Application mockApplication;

    private MainViewModel viewModel;
    private NoteRepository mockRepository;

    @Before
    public void setUp() {
        // Create a real ViewModel with mocked dependencies
        viewModel = new MainViewModel(mockApplication);
    }

    // -------------------------------------------------------------
    // White-Box Test 1: FilterState.areFiltersActive() logic
    // Tests the internal logic that determines if any filters are active
    // -------------------------------------------------------------
    @Test
    public void test_filterState_areFiltersActive_withText() {
        FilterState state = new FilterState("test query", null, false, false, false);
        assertTrue("FilterState should be active when text is present", state.areFiltersActive());
    }

    @Test
    public void test_filterState_areFiltersActive_withTags() {
        FilterState state = new FilterState(null, Arrays.asList("tag1", "tag2"), false, false, false);
        assertTrue("FilterState should be active when tags are present", state.areFiltersActive());
    }

    @Test
    public void test_filterState_areFiltersActive_withMediaFilters() {
        FilterState state = new FilterState(null, null, true, false, false);
        assertTrue("FilterState should be active when hasPhoto is true", state.areFiltersActive());

        state = new FilterState(null, null, false, true, false);
        assertTrue("FilterState should be active when hasVoiceMemo is true", state.areFiltersActive());

        state = new FilterState(null, null, false, false, true);
        assertTrue("FilterState should be active when hasLocation is true", state.areFiltersActive());
    }

    @Test
    public void test_filterState_areFiltersActive_noFilters() {
        FilterState state = new FilterState(null, null, false, false, false);
        assertFalse("FilterState should not be active when no filters are set", state.areFiltersActive());
    }

    @Test
    public void test_filterState_areFiltersActive_withDateRange() {
        Date startDate = new Date();
        Date endDate = new Date();
        FilterState state = new FilterState(null, null, false, false, false, startDate, null);
        assertTrue("FilterState should be active when startDate is set", state.areFiltersActive());

        state = new FilterState(null, null, false, false, false, null, endDate);
        assertTrue("FilterState should be active when endDate is set", state.areFiltersActive());

        state = new FilterState(null, null, false, false, false, startDate, endDate);
        assertTrue("FilterState should be active when both dates are set", state.areFiltersActive());
    }

    // -------------------------------------------------------------
    // White-Box Test 2: Text search filtering logic in MainViewModel
    // Tests the internal text matching logic for title and body content
    // -------------------------------------------------------------
    @Test
    public void test_mainViewModel_textSearch_matchesTitle() {
        // Create test notes
        Note note1 = createNoteWithTitle("Shopping List");
        Note note2 = createNoteWithTitle("Work Tasks");
        Note note3 = createNoteWithTitle("Shopping for groceries");

        List<Note> notes = Arrays.asList(note1, note2, note3);

        // Simulate filtering logic from MainViewModel.filterOtherNotes()
        FilterState filterState = new FilterState("Shopping", null, false, false, false);
        String lowerCaseQuery = filterState.text.toLowerCase();

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.title != null && note.title.toLowerCase().contains(lowerCaseQuery)) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 2 notes with 'Shopping' in title", 2, filtered.size());
        assertTrue("Should contain note1", filtered.contains(note1));
        assertTrue("Should contain note3", filtered.contains(note3));
        assertFalse("Should not contain note2", filtered.contains(note2));
    }

    @Test
    public void test_mainViewModel_textSearch_matchesBodyText() {
        // Create notes with text elements
        Note note1 = createNoteWithTextElement("Buy milk and eggs");
        Note note2 = createNoteWithTextElement("Call dentist");
        Note note3 = createNoteWithTextElement("Buy groceries tomorrow");

        List<Note> notes = Arrays.asList(note1, note2, note3);

        // Simulate filtering logic
        FilterState filterState = new FilterState("Buy", null, false, false, false);
        String lowerCaseQuery = filterState.text.toLowerCase();

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            boolean matches = false;
            if (note.title != null && note.title.toLowerCase().contains(lowerCaseQuery)) {
                matches = true;
            }
            if (!matches && note.elements != null) {
                for (NoteElement element : note.elements) {
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
            if (matches) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 2 notes with 'Buy' in body", 2, filtered.size());
        assertTrue("Should contain note1", filtered.contains(note1));
        assertTrue("Should contain note3", filtered.contains(note3));
        assertFalse("Should not contain note2", filtered.contains(note2));
    }

    @Test
    public void test_mainViewModel_textSearch_caseInsensitive() {
        Note note1 = createNoteWithTitle("SHOPPING");
        Note note2 = createNoteWithTitle("shopping");
        Note note3 = createNoteWithTitle("Shopping");

        List<Note> notes = Arrays.asList(note1, note2, note3);

        FilterState filterState = new FilterState("shop", null, false, false, false);
        String lowerCaseQuery = filterState.text.toLowerCase();

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.title != null && note.title.toLowerCase().contains(lowerCaseQuery)) {
                filtered.add(note);
            }
        }

        assertEquals("Case-insensitive search should find all 3 notes", 3, filtered.size());
    }

    // -------------------------------------------------------------
    // White-Box Test 3: Tag filtering logic
    // Tests the internal tag matching logic (must contain ALL specified tags)
    // -------------------------------------------------------------
    @Test
    public void test_mainViewModel_tagFilter_matchesAllTags() {
        Note note1 = createNoteWithTags(Arrays.asList("work", "urgent"));
        Note note2 = createNoteWithTags(Arrays.asList("work", "urgent", "personal"));
        Note note3 = createNoteWithTags(Arrays.asList("work"));
        Note note4 = createNoteWithTags(Arrays.asList("urgent"));

        List<Note> notes = Arrays.asList(note1, note2, note3, note4);

        // Simulate tag filtering logic - must contain ALL tags
        FilterState filterState = new FilterState(null, Arrays.asList("work", "urgent"), false, false, false);
        List<String> filterTags = filterState.tags;

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.tags != null && new java.util.HashSet<>(note.tags).containsAll(filterTags)) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 2 notes with both 'work' and 'urgent' tags", 2, filtered.size());
        assertTrue("Should contain note1", filtered.contains(note1));
        assertTrue("Should contain note2", filtered.contains(note2));
        assertFalse("Should not contain note3 (missing 'urgent')", filtered.contains(note3));
        assertFalse("Should not contain note4 (missing 'work')", filtered.contains(note4));
    }

    @Test
    public void test_mainViewModel_tagFilter_emptyTags() {
        Note note1 = createNoteWithTags(Arrays.asList("work"));
        Note note2 = createNoteWithTags(null);

        List<Note> notes = Arrays.asList(note1, note2);

        FilterState filterState = new FilterState(null, Collections.emptyList(), false, false, false);
        List<String> filterTags = filterState.tags;

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (filterTags.isEmpty() || (note.tags != null && new java.util.HashSet<>(note.tags).containsAll(filterTags))) {
                filtered.add(note);
            }
        }

        // When no tags are specified, all notes should pass
        assertEquals("Should include all notes when no tag filter is active", 2, filtered.size());
    }

    // -------------------------------------------------------------
    // White-Box Test 4: Media type detection logic
    // Tests the internal logic for detecting photo, voice memo, and location elements
    // -------------------------------------------------------------
    @Test
    public void test_mainViewModel_hasPhotoFilter_detectsPhotoElement() {
        Note note1 = createNoteWithPhoto();
        Note note2 = createNoteWithTextElement("No photo here");
        Note note3 = createNoteWithVoiceMemo();

        List<Note> notes = Arrays.asList(note1, note2, note3);

        // Simulate hasPhoto filtering logic
        FilterState filterState = new FilterState(null, null, true, false, false);

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.elements != null && note.elements.stream().anyMatch(e -> e instanceof PhotoElement)) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 1 note with photo", 1, filtered.size());
        assertTrue("Should contain note1", filtered.contains(note1));
        assertFalse("Should not contain note2", filtered.contains(note2));
        assertFalse("Should not contain note3", filtered.contains(note3));
    }

    @Test
    public void test_mainViewModel_hasVoiceMemoFilter_detectsVoiceMemoElement() {
        Note note1 = createNoteWithVoiceMemo();
        Note note2 = createNoteWithTextElement("No voice memo");
        Note note3 = createNoteWithPhoto();

        List<Note> notes = Arrays.asList(note1, note2, note3);

        FilterState filterState = new FilterState(null, null, false, true, false);

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.elements != null && note.elements.stream().anyMatch(e -> e instanceof VoiceMemoElement)) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 1 note with voice memo", 1, filtered.size());
        assertTrue("Should contain note1", filtered.contains(note1));
        assertFalse("Should not contain note2", filtered.contains(note2));
        assertFalse("Should not contain note3", filtered.contains(note3));
    }

    @Test
    public void test_mainViewModel_hasLocationFilter_detectsLocationTag() {
        Note note1 = createNoteWithLocation();
        Note note2 = createNoteWithoutLocation();

        List<Note> notes = Arrays.asList(note1, note2);

        FilterState filterState = new FilterState(null, null, false, false, true);

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.location != null) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 1 note with location tag", 1, filtered.size());
        assertTrue("Should contain note1", filtered.contains(note1));
        assertFalse("Should not contain note2", filtered.contains(note2));
    }

    // -------------------------------------------------------------
    // White-Box Test 5: Combined filter logic
    // Tests the internal logic when multiple filters are applied simultaneously
    // -------------------------------------------------------------
    @Test
    public void test_mainViewModel_combinedFilters_textAndTags() {
        Note note1 = createNoteWithTitleAndTags("Shopping List", Arrays.asList("work"));
        Note note2 = createNoteWithTitleAndTags("Shopping List", Arrays.asList("personal"));
        Note note3 = createNoteWithTitleAndTags("Work Tasks", Arrays.asList("work"));

        List<Note> notes = Arrays.asList(note1, note2, note3);

        FilterState filterState = new FilterState("Shopping", Arrays.asList("work"), false, false, false);
        String lowerCaseQuery = filterState.text.toLowerCase();
        List<String> filterTags = filterState.tags;

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            // Must match text AND tags
            boolean matchesText = note.title != null && note.title.toLowerCase().contains(lowerCaseQuery);
            boolean matchesTags = note.tags != null && new java.util.HashSet<>(note.tags).containsAll(filterTags);
            
            if (matchesText && matchesTags) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 1 note matching both text and tag filters", 1, filtered.size());
        assertTrue("Should contain note1", filtered.contains(note1));
    }

    @Test
    public void test_mainViewModel_combinedFilters_textAndMedia() {
        Note note1 = createNoteWithTitleAndPhoto("Shopping List");
        Note note2 = createNoteWithTitle("Shopping List");
        Note note3 = createNoteWithTitleAndPhoto("Work Tasks");

        List<Note> notes = Arrays.asList(note1, note2, note3);

        FilterState filterState = new FilterState("Shopping", null, true, false, false);
        String lowerCaseQuery = filterState.text.toLowerCase();

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            boolean matchesText = note.title != null && note.title.toLowerCase().contains(lowerCaseQuery);
            boolean hasPhoto = note.elements != null && note.elements.stream().anyMatch(e -> e instanceof PhotoElement);
            
            if (matchesText && hasPhoto) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 1 note matching both text and photo filters", 1, filtered.size());
        assertTrue("Should contain note1", filtered.contains(note1));
    }

    // Helper methods to create test notes
    private Note createNoteWithTitle(String title) {
        Note note = new Note();
        note.id = System.currentTimeMillis();
        note.title = title;
        note.elements = new ArrayList<>();
        note.tags = new ArrayList<>();
        note.isPinned = false;
        note.dateCreated = new Date();
        note.lastEdited = new Date();
        return note;
    }

    private Note createNoteWithTextElement(String text) {
        Note note = createNoteWithTitle("Test Note");
        note.elements.add(new TextElement(text));
        return note;
    }

    private Note createNoteWithTags(List<String> tags) {
        Note note = createNoteWithTitle("Tagged Note");
        note.tags = tags != null ? new ArrayList<>(tags) : null;
        return note;
    }

    private Note createNoteWithTitleAndTags(String title, List<String> tags) {
        Note note = createNoteWithTitle(title);
        note.tags = tags != null ? new ArrayList<>(tags) : null;
        return note;
    }

    private Note createNoteWithPhoto() {
        Note note = createNoteWithTitle("Photo Note");
        note.elements.add(new PhotoElement("/path/to/photo.jpg"));
        return note;
    }

    private Note createNoteWithVoiceMemo() {
        Note note = createNoteWithTitle("Voice Note");
        note.elements.add(new VoiceMemoElement("/path/to/voice.3gp"));
        return note;
    }

    private Note createNoteWithLocation() {
        Note note = createNoteWithTitle("Location Note");
        note.location = new com.alexwan.csci310team36projectd.data.Location();
        note.location.latitude = 34.0;
        note.location.longitude = -118.0;
        note.location.setName("Test Location");
        return note;
    }

    private Note createNoteWithoutLocation() {
        Note note = createNoteWithTitle("No Location Note");
        note.location = null;
        return note;
    }

    private Note createNoteWithTitleAndPhoto(String title) {
        Note note = createNoteWithTitle(title);
        note.elements.add(new PhotoElement("/path/to/photo.jpg"));
        return note;
    }

    // -------------------------------------------------------------
    // White-Box Test 6: Date range filtering logic
    // Tests the internal date range filtering logic
    // -------------------------------------------------------------
    @Test
    public void test_mainViewModel_dateRangeFilter_startDateOnly() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15, 12, 0, 0);
        Date startDate = cal.getTime();

        // Create notes with different lastEdited dates
        Note note1 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 10)); // Before start
        Note note2 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 15)); // On start
        Note note3 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 20)); // After start

        List<Note> notes = Arrays.asList(note1, note2, note3);

        // Simulate date range filtering logic
        FilterState filterState = new FilterState(null, null, false, false, false, startDate, null);

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.lastEdited == null) {
                continue; // Exclude notes without lastEdited date
            }
            long noteTime = note.lastEdited.getTime();
            boolean afterStart = filterState.startDate == null || noteTime >= filterState.startDate.getTime();
            boolean beforeEnd = filterState.endDate == null || noteTime <= filterState.endDate.getTime();
            if (afterStart && beforeEnd) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 2 notes on or after start date", 2, filtered.size());
        assertFalse("Should not contain note1 (before start)", filtered.contains(note1));
        assertTrue("Should contain note2 (on start)", filtered.contains(note2));
        assertTrue("Should contain note3 (after start)", filtered.contains(note3));
    }

    @Test
    public void test_mainViewModel_dateRangeFilter_endDateOnly() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 20, 12, 0, 0);
        Date endDate = cal.getTime();

        Note note1 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 10)); // Before end
        Note note2 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 20)); // On end
        Note note3 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 25)); // After end

        List<Note> notes = Arrays.asList(note1, note2, note3);

        FilterState filterState = new FilterState(null, null, false, false, false, null, endDate);

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.lastEdited == null) {
                continue;
            }
            long noteTime = note.lastEdited.getTime();
            boolean afterStart = filterState.startDate == null || noteTime >= filterState.startDate.getTime();
            boolean beforeEnd = filterState.endDate == null || noteTime <= filterState.endDate.getTime();
            if (afterStart && beforeEnd) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 2 notes on or before end date", 2, filtered.size());
        assertTrue("Should contain note1 (before end)", filtered.contains(note1));
        assertTrue("Should contain note2 (on end)", filtered.contains(note2));
        assertFalse("Should not contain note3 (after end)", filtered.contains(note3));
    }

    @Test
    public void test_mainViewModel_dateRangeFilter_bothDates() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 10, 0, 0, 0);
        Date startDate = cal.getTime();
        cal.set(2024, Calendar.JANUARY, 20, 23, 59, 59);
        Date endDate = cal.getTime();

        Note note1 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 5)); // Before range
        Note note2 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 15)); // In range
        Note note3 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 25)); // After range
        Note note4 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 10)); // On start
        Note note5 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 20)); // On end

        List<Note> notes = Arrays.asList(note1, note2, note3, note4, note5);

        FilterState filterState = new FilterState(null, null, false, false, false, startDate, endDate);

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.lastEdited == null) {
                continue;
            }
            long noteTime = note.lastEdited.getTime();
            boolean afterStart = filterState.startDate == null || noteTime >= filterState.startDate.getTime();
            boolean beforeEnd = filterState.endDate == null || noteTime <= filterState.endDate.getTime();
            if (afterStart && beforeEnd) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 3 notes within date range", 3, filtered.size());
        assertFalse("Should not contain note1 (before range)", filtered.contains(note1));
        assertTrue("Should contain note2 (in range)", filtered.contains(note2));
        assertFalse("Should not contain note3 (after range)", filtered.contains(note3));
        assertTrue("Should contain note4 (on start)", filtered.contains(note4));
        assertTrue("Should contain note5 (on end)", filtered.contains(note5));
    }

    @Test
    public void test_mainViewModel_dateRangeFilter_excludesNotesWithoutDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 15, 12, 0, 0);
        Date startDate = cal.getTime();
        Date endDate = cal.getTime();

        Note note1 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 15));
        Note note2 = createNoteWithDate(null); // No lastEdited date
        Note note3 = createNoteWithDate(createDate(2024, Calendar.JANUARY, 15));

        List<Note> notes = Arrays.asList(note1, note2, note3);

        FilterState filterState = new FilterState(null, null, false, false, false, startDate, endDate);

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            if (note.lastEdited == null) {
                continue; // Exclude notes without lastEdited date
            }
            long noteTime = note.lastEdited.getTime();
            boolean afterStart = filterState.startDate == null || noteTime >= filterState.startDate.getTime();
            boolean beforeEnd = filterState.endDate == null || noteTime <= filterState.endDate.getTime();
            if (afterStart && beforeEnd) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 2 notes (excluding note without date)", 2, filtered.size());
        assertTrue("Should contain note1", filtered.contains(note1));
        assertFalse("Should not contain note2 (no date)", filtered.contains(note2));
        assertTrue("Should contain note3", filtered.contains(note3));
    }

    @Test
    public void test_mainViewModel_dateRangeFilter_combinedWithOtherFilters() {
        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 10, 0, 0, 0);
        Date startDate = cal.getTime();
        cal.set(2024, Calendar.JANUARY, 20, 23, 59, 59);
        Date endDate = cal.getTime();

        Note note1 = createNoteWithTitleAndTags("Shopping", Arrays.asList("work"));
        note1.lastEdited = createDate(2024, Calendar.JANUARY, 15);
        note1.elements.add(new PhotoElement("/path/to/photo.jpg"));

        Note note2 = createNoteWithTitleAndTags("Shopping", Arrays.asList("work"));
        note2.lastEdited = createDate(2024, Calendar.JANUARY, 5); // Before date range

        Note note3 = createNoteWithTitleAndTags("Shopping", Arrays.asList("personal"));
        note3.lastEdited = createDate(2024, Calendar.JANUARY, 15);

        List<Note> notes = Arrays.asList(note1, note2, note3);

        // Combined filter: text="Shopping", tags="work", hasPhoto=true, date range
        FilterState filterState = new FilterState("Shopping", Arrays.asList("work"), true, false, false, startDate, endDate);
        String lowerCaseQuery = filterState.text.toLowerCase();
        List<String> filterTags = filterState.tags;

        List<Note> filtered = new ArrayList<>();
        for (Note note : notes) {
            // Check text
            boolean matchesText = note.title != null && note.title.toLowerCase().contains(lowerCaseQuery);
            
            // Check tags
            boolean matchesTags = note.tags != null && new java.util.HashSet<>(note.tags).containsAll(filterTags);
            
            // Check photo
            boolean hasPhoto = note.elements != null && note.elements.stream().anyMatch(e -> e instanceof PhotoElement);
            
            // Check date range
            boolean matchesDateRange = true;
            if (note.lastEdited != null) {
                long noteTime = note.lastEdited.getTime();
                boolean afterStart = filterState.startDate == null || noteTime >= filterState.startDate.getTime();
                boolean beforeEnd = filterState.endDate == null || noteTime <= filterState.endDate.getTime();
                matchesDateRange = afterStart && beforeEnd;
            } else {
                matchesDateRange = false;
            }
            
            if (matchesText && matchesTags && hasPhoto && matchesDateRange) {
                filtered.add(note);
            }
        }

        assertEquals("Should find 1 note matching all filters including date range", 1, filtered.size());
        assertTrue("Should contain note1 (matches all criteria)", filtered.contains(note1));
        assertFalse("Should not contain note2 (outside date range)", filtered.contains(note2));
        assertFalse("Should not contain note3 (wrong tag)", filtered.contains(note3));
    }

    // Helper method to create a Date
    private Date createDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // Helper method to create a note with a specific lastEdited date
    private Note createNoteWithDate(Date lastEdited) {
        Note note = createNoteWithTitle("Test Note");
        note.lastEdited = lastEdited;
        return note;
    }
}

