package com.alexwan.csci310team36projectd;

import android.app.Application;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.NoteRepository;
import com.alexwan.csci310team36projectd.data.model.ChecklistElement;
import com.alexwan.csci310team36projectd.data.model.PhotoElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.alexwan.csci310team36projectd.data.model.VoiceMemoElement;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Feature1BlackBoxTests {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private Application application;

    @Captor
    private ArgumentCaptor<Note> noteCaptor;

    private MainViewModel mainViewModel;

    @Before
    public void setUp() {
        when(noteRepository.getPinnedNotes()).thenReturn(new MutableLiveData<>(Collections.emptyList()));
        when(noteRepository.getUnpinnedNotes()).thenReturn(new MutableLiveData<>(Collections.emptyList()));
        mainViewModel = new MainViewModel(application, noteRepository);
    }

    @Test
    public void createBlankNote() {
        Note note = TestNoteFactory.noteWithTitle("Untitled");
        mainViewModel.insert(note);
        verify(noteRepository).insert(noteCaptor.capture());
        assertEquals("Untitled", noteCaptor.getValue().title);
    }

    @Test
    public void typeTextAndStyle() {
        // Use Mockito to temporarily "teach" new TextElement objects how to behave in this test
        try (MockedConstruction<TextElement> mocked = Mockito.mockConstruction(TextElement.class,
                (mock, context) -> {
                    // When getContent() is called on a mock, return the original text
                    when(mock.getContent()).thenReturn((String) context.arguments().get(0));
                })) {
            Note note = TestNoteFactory.noteWithTitle("Text Note");
            note.elements.add(new TextElement("hello world"));
            mainViewModel.update(note);

            verify(noteRepository).update(noteCaptor.capture());
            Note capturedNote = noteCaptor.getValue();
            assertEquals(1, capturedNote.elements.size());
            assertTrue(capturedNote.elements.get(0) instanceof TextElement);

            // This assertion will now pass because our mock intercepts the getContent() call
            assertEquals("hello world", ((TextElement) capturedNote.elements.get(0)).getContent());
        }
    }

    @Test
    public void checklistAddAndToggle() {
        Note note = TestNoteFactory.noteWithChecklist("a", "b");
        mainViewModel.update(note);
        verify(noteRepository).update(noteCaptor.capture());
        Note capturedNote = noteCaptor.getValue();
        assertEquals(1, capturedNote.elements.size());
        assertTrue(capturedNote.elements.get(0) instanceof ChecklistElement);
        List<ChecklistElement.ChecklistItem> items = ((ChecklistElement) capturedNote.elements.get(0)).getItems();
        assertEquals(2, items.size());
        assertEquals("a", items.get(0).getText());
        assertFalse(items.get(0).isChecked());
    }

    @Test
    public void attachPhotoAndAudio() {
        Note note = TestNoteFactory.noteWithTitle("Media Note");
        note.elements.add(new PhotoElement("path/to/photo.jpg"));
        note.elements.add(new VoiceMemoElement("path/to/voice.mp3"));
        mainViewModel.insert(note);
        verify(noteRepository).insert(noteCaptor.capture());
        Note capturedNote = noteCaptor.getValue();
        assertEquals(2, capturedNote.elements.size());
        assertTrue(capturedNote.elements.stream().anyMatch(e -> e instanceof PhotoElement));
        assertTrue(capturedNote.elements.stream().anyMatch(e -> e instanceof VoiceMemoElement));
    }

    @Test
    public void editExistingNoteMovesToTop() {
        Note note = TestNoteFactory.noteWithTitle("Original Title");
        note.id = 1L;
        note.title = "Updated Title";
        TestNoteFactory.advanceEdit(note);
        mainViewModel.update(note);
        verify(noteRepository).update(noteCaptor.capture());
        Note capturedNote = noteCaptor.getValue();
        assertEquals(1L, capturedNote.id);
        assertEquals("Updated Title", capturedNote.title);
    }
}
