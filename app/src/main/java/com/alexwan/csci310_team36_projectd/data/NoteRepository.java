package com.alexwan.csci310_team36_projectd.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class NoteRepository {
    private NoteDao mNoteDao;
    private LiveData<List<Note>> mPinnedNotes;
    private LiveData<List<Note>> mUnpinnedNotes;

    public NoteRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mNoteDao = db.noteDao();
        mPinnedNotes = mNoteDao.getPinnedNotes();
        mUnpinnedNotes = mNoteDao.getUnpinnedNotes();
    }

    public LiveData<List<Note>> getPinnedNotes() {
        return mPinnedNotes;
    }

    public LiveData<List<Note>> getRelevantNotes(long currentTime, long oneHourAgo) {
        return mNoteDao.getRelevantNotes(currentTime, oneHourAgo);
    }

    public LiveData<List<Note>> getUnpinnedNotes() {
        return mUnpinnedNotes;
    }

    public LiveData<Note> getNoteById(long noteId) {
        return mNoteDao.getNoteById(noteId);
    }

    public Note getNoteByIdBlocking(long noteId) {
        return mNoteDao.getNoteByIdBlocking(noteId);
    }

    public void insert(Note note) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mNoteDao.insert(note);
        });
    }

    public void update(Note note) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mNoteDao.update(note);
        });
    }

    public void delete(Note note) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mNoteDao.delete(note);
        });
    }
}
