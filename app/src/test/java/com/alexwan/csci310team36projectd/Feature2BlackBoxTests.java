package com.alexwan.csci310team36projectd;

import android.app.Application;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.NoteRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Feature2BlackBoxTests {

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
    public void addTag() {
        Note note = TestNoteFactory.noteWithTitle("Test Note");
        TestNoteFactory.addTag(note, "CSCI 170");
        mainViewModel.update(note);
        verify(noteRepository).update(noteCaptor.capture());
        assertTrue(noteCaptor.getValue().tags.contains("CSCI 170"));
    }

    @Test
    public void pinNote() {
        Note note = TestNoteFactory.noteWithTitle("Test Note");
        note.isPinned = true;
        mainViewModel.update(note);
        verify(noteRepository).update(noteCaptor.capture());
        assertTrue(noteCaptor.getValue().isPinned);
    }

    @Test
    public void unpinNote() {
        Note note = TestNoteFactory.noteWithTitle("Test Note");
        note.isPinned = true;
        note.isPinned = false;
        mainViewModel.update(note);
        verify(noteRepository).update(noteCaptor.capture());
        assertFalse(noteCaptor.getValue().isPinned);
    }

    @Test
    public void addReplaceAndRemoveLocation() {
        Note note = TestNoteFactory.noteWithTitle("Test Note");
        note.location = TestNoteFactory.sampleLocation("SAL Hall");
        mainViewModel.update(note);

        note.location = TestNoteFactory.sampleLocation("Leavey Library");
        mainViewModel.update(note);

        note.location = null;
        mainViewModel.update(note);

        verify(noteRepository, atLeast(1)).update(noteCaptor.capture());
        assertNull(noteCaptor.getValue().location);
    }

    @Test
    public void sectionsSortingHint() {
        Note pinnedNote = TestNoteFactory.noteWithTitle("Pinned");
        pinnedNote.isPinned = true;
        mainViewModel.update(pinnedNote);
        verify(noteRepository).update(noteCaptor.capture());
        assertTrue(noteCaptor.getValue().isPinned);
    }
}
