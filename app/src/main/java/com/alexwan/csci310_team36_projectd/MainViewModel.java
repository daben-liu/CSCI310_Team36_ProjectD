package com.alexwan.csci310_team36_projectd;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.alexwan.csci310_team36_projectd.data.Note;
import com.alexwan.csci310_team36_projectd.data.NoteRepository;
import com.alexwan.csci310_team36_projectd.data.model.NoteElement;
import com.alexwan.csci310_team36_projectd.data.model.TextElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainViewModel extends AndroidViewModel {
    private NoteRepository mRepository;

    // Raw data sources from the repository
    private LiveData<List<Note>> mPinnedNotesSource;
    private LiveData<List<Note>> mUnpinnedNotesSource;
    private LiveData<List<Note>> mRelevantNotesSource;

    // Search query
    private final MutableLiveData<String> mSearchQuery = new MutableLiveData<>("");

    // De-duplicated LiveData lists for the UI
    private final MediatorLiveData<List<Note>> mFilteredRelevantNotes = new MediatorLiveData<>();
    private final MediatorLiveData<List<Note>> mFilteredOtherNotes = new MediatorLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        mRepository = new NoteRepository(application);
        mPinnedNotesSource = mRepository.getPinnedNotes();
        mUnpinnedNotesSource = mRepository.getUnpinnedNotes();

        // Get the relevant notes source directly. No city needed.
        long currentTime = System.currentTimeMillis();
        long oneHourAgo = currentTime - 3600 * 1000;
        mRelevantNotesSource = mRepository.getRelevantNotes(currentTime, oneHourAgo);

        // Set up the mediator for the "Relevant" list (notes that are relevant BUT NOT pinned).
        mFilteredRelevantNotes.addSource(mPinnedNotesSource, pinned -> filterRelevantNotes());
        mFilteredRelevantNotes.addSource(mRelevantNotesSource, relevant -> filterRelevantNotes());

        // Set up the mediator for the "Other" list (notes that are unpinned BUT NOT relevant).
        mFilteredOtherNotes.addSource(mUnpinnedNotesSource, unpinned -> filterOtherNotes());
        mFilteredOtherNotes.addSource(mFilteredRelevantNotes, relevant -> filterOtherNotes());
        mFilteredOtherNotes.addSource(mSearchQuery, query -> filterOtherNotes()); // Re-filter when search query changes
    }

    private void filterRelevantNotes() {
        List<Note> relevant = mRelevantNotesSource.getValue();
        List<Note> pinned = mPinnedNotesSource.getValue();
        if (relevant == null) {
            mFilteredRelevantNotes.setValue(new ArrayList<>());
            return;
        }
        if (pinned == null || pinned.isEmpty()) {
            mFilteredRelevantNotes.setValue(relevant);
            return;
        }
        List<Note> filtered = new ArrayList<>();
        Set<Long> pinnedIds = new HashSet<>();
        for (Note note : pinned) {
            pinnedIds.add(note.id);
        }
        for (Note note : relevant) {
            if (!pinnedIds.contains(note.id)) {
                filtered.add(note);
            }
        }
        mFilteredRelevantNotes.setValue(filtered);
    }

    private void filterOtherNotes() {
        List<Note> unpinned = mUnpinnedNotesSource.getValue();
        List<Note> relevantFiltered = mFilteredRelevantNotes.getValue();
        String query = mSearchQuery.getValue();

        if (unpinned == null) {
            mFilteredOtherNotes.setValue(new ArrayList<>());
            return;
        }

        // First, de-duplicate from the relevant list
        List<Note> deDuplicated = new ArrayList<>();
        if (relevantFiltered != null && !relevantFiltered.isEmpty()) {
            Set<Long> relevantIds = new HashSet<>();
            for (Note note : relevantFiltered) {
                relevantIds.add(note.id);
            }
            for (Note note : unpinned) {
                if (!relevantIds.contains(note.id)) {
                    deDuplicated.add(note);
                }
            }
        } else {
            deDuplicated.addAll(unpinned);
        }

        // Then, apply search filter
        if (query == null || query.trim().isEmpty()) {
            mFilteredOtherNotes.setValue(deDuplicated); // No search query, show all de-duplicated notes
            return;
        }

        List<Note> searchResults = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();
        for (Note note : deDuplicated) {
            if (note.title != null && note.title.toLowerCase().contains(lowerCaseQuery)) {
                searchResults.add(note);
                continue; // Found in title, no need to check body
            }
            if (note.elements != null) {
                for (NoteElement element : note.elements) {
                    if (element instanceof TextElement) {
                        TextElement textElement = (TextElement) element;
                        if (textElement.getContent() != null && textElement.getContent().toString().toLowerCase().contains(lowerCaseQuery)) {
                            searchResults.add(note);
                            break; // Found in body, no need to check other elements
                        }
                    }
                }
            }
        }
        mFilteredOtherNotes.setValue(searchResults);
    }


    // --- Public Getters for the UI ---

    public LiveData<List<Note>> getPinnedNotes() {
        return mPinnedNotesSource;
    }

    public LiveData<List<Note>> getRelevantNotes() {
        return mFilteredRelevantNotes;
    }

    public LiveData<List<Note>> getOtherNotes() {
        return mFilteredOtherNotes;
    }

    public void setSearchQuery(String query) {
        mSearchQuery.setValue(query);
    }

    public LiveData<Note> getNoteById(long noteId) {
        return mRepository.getNoteById(noteId);
    }

    public Note getNoteByIdBlocking(long noteId) {
        return mRepository.getNoteByIdBlocking(noteId);
    }

    public void insert(Note note) {
        mRepository.insert(note);
    }

    public void update(Note note) {
        mRepository.update(note);
    }

    public void delete(Note note) {
        mRepository.delete(note);
    }
}
