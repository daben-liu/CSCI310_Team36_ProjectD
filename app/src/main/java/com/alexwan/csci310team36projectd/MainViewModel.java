package com.alexwan.csci310team36projectd;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.alexwan.csci310team36projectd.data.FilterState;
import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.NoteRepository;
import com.alexwan.csci310team36projectd.data.model.NoteElement;
import com.alexwan.csci310team36projectd.data.model.PhotoElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.alexwan.csci310team36projectd.data.model.VoiceMemoElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MainViewModel extends AndroidViewModel {
    private NoteRepository mRepository;

    // Raw data sources from the repository
    private LiveData<List<Note>> mPinnedNotesSource;
    private LiveData<List<Note>> mUnpinnedNotesSource;

    // New sources for dynamic relevance
    private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    private final MediatorLiveData<List<Note>> mDynamicRelevantNotes = new MediatorLiveData<>();

    // Filter state
    private final MutableLiveData<FilterState> mFilterState = new MutableLiveData<>(new FilterState(null, null, false, false, false));


    // De-duplicated LiveData lists for the UI
    private final MediatorLiveData<List<Note>> mFilteredRelevantNotes = new MediatorLiveData<>();
    private final MediatorLiveData<List<Note>> mFilteredOtherNotes = new MediatorLiveData<>();

    public MainViewModel(@NonNull Application application) {
        super(application);
        mRepository = new NoteRepository(application);
        mPinnedNotesSource = mRepository.getPinnedNotes();
        mUnpinnedNotesSource = mRepository.getUnpinnedNotes();

        // --- NEW DYNAMIC RELEVANCE LOGIC ---
        // The mDynamicRelevantNotes will recalculate whenever the list of unpinned notes changes or the location changes.
        mDynamicRelevantNotes.addSource(mUnpinnedNotesSource, notes -> calculateRelevantNotes(notes, currentLocation.getValue()));
        mDynamicRelevantNotes.addSource(currentLocation, location -> calculateRelevantNotes(mUnpinnedNotesSource.getValue(), location));
        // --- END NEW LOGIC ---

        // Set up the mediator for the "Relevant" list (notes that are relevant BUT NOT pinned).
        // It now uses mDynamicRelevantNotes as its source.
        mFilteredRelevantNotes.addSource(mPinnedNotesSource, pinned -> filterRelevantNotes());
        mFilteredRelevantNotes.addSource(mDynamicRelevantNotes, relevant -> filterRelevantNotes()); // Changed source here

        // Set up the mediator for the "Other" list (notes that are unpinned BUT NOT relevant).
        mFilteredOtherNotes.addSource(mUnpinnedNotesSource, unpinned -> filterOtherNotes());
        mFilteredOtherNotes.addSource(mFilteredRelevantNotes, relevant -> filterOtherNotes());
        mFilteredOtherNotes.addSource(mFilterState, filterState -> filterOtherNotes()); // Re-filter when filter state changes
    }

    // --- NEW METHOD TO CALCULATE RELEVANCE ---
    private void calculateRelevantNotes(List<Note> notes, Location location) {
        if (notes == null) {
            mDynamicRelevantNotes.setValue(Collections.emptyList());
            return;
        }

        List<Note> calculatedRelevantNotes = new ArrayList<>();
        long oneHourInMillis = 3600 * 1000;

        for (Note note : notes) {
            boolean isRelevant = false;
            // Rule 1: Upcoming reminders within the next hour
            if (note.reminderTime > 0 && note.reminderTime > System.currentTimeMillis() && note.reminderTime < System.currentTimeMillis() + oneHourInMillis) {
                isRelevant = true;
            }

            // Rule 2: Nearby geo-reminders (e.g., within 5km)
            if (!isRelevant && location != null && "geo".equals(note.reminderType) && note.reminderLocation != null) {
                float[] distanceResults = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), note.reminderLocation.latitude, note.reminderLocation.longitude, distanceResults);
                if (distanceResults[0] < 5000) { // 5 kilometers
                    isRelevant = true;
                }
            }

            if(isRelevant) {
                calculatedRelevantNotes.add(note);
            }
        }
        mDynamicRelevantNotes.setValue(calculatedRelevantNotes);
    }
    // --- END NEW METHOD ---


    private void filterRelevantNotes() {
        // This method now uses mDynamicRelevantNotes as its source of truth for relevance
        List<Note> relevant = mDynamicRelevantNotes.getValue();
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
        FilterState filterState = mFilterState.getValue();

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

        // Then, apply filters if any are active
        if (filterState == null || !filterState.areFiltersActive()) {
            mFilteredOtherNotes.setValue(deDuplicated); // No filters, show all de-duplicated notes
            return;
        }

        List<Note> searchResults = new ArrayList<>(deDuplicated);

        // Filter by text
        if (filterState.text != null && !filterState.text.trim().isEmpty()) {
            String lowerCaseQuery = filterState.text.toLowerCase();
            searchResults = searchResults.stream()
                .filter(note -> {
                    if (note.title != null && note.title.toLowerCase().contains(lowerCaseQuery)) {
                        return true;
                    }
                    if (note.elements != null) {
                        for (NoteElement element : note.elements) {
                            if (element instanceof TextElement) {
                                TextElement textElement = (TextElement) element;
                                if (textElement.getContent() != null && textElement.getContent().toString().toLowerCase().contains(lowerCaseQuery)) {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }

        // Filter by tags
        if (!filterState.tags.isEmpty()) {
            searchResults = searchResults.stream()
                .filter(note -> note.tags != null && new HashSet<>(note.tags).containsAll(filterState.tags))
                .collect(Collectors.toList());
        }

        // Filter by hasPhoto
        if (filterState.hasPhoto) {
            searchResults = searchResults.stream()
                .filter(note -> note.elements != null && note.elements.stream().anyMatch(e -> e instanceof PhotoElement))
                .collect(Collectors.toList());
        }

        // Filter by hasVoiceMemo
        if (filterState.hasVoiceMemo) {
            searchResults = searchResults.stream()
                .filter(note -> note.elements != null && note.elements.stream().anyMatch(e -> e instanceof VoiceMemoElement))
                .collect(Collectors.toList());
        }

        // Filter by hasLocation (This correctly refers to the location TAG)
        if (filterState.hasLocation) {
            searchResults = searchResults.stream()
                .filter(note -> note.location != null)
                .collect(Collectors.toList());
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

    // --- NEW PUBLIC METHOD ---
    public void updateCurrentLocation(Location location) {
        currentLocation.setValue(location);
    }
    // --- END NEW PUBLIC METHOD ---

    public void setFilterState(FilterState state) {
        mFilterState.setValue(state);
    }

    public LiveData<FilterState> getFilterState() {
        return mFilterState;
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
