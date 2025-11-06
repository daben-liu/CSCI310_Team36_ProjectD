package com.alexwan.csci310team36projectd.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY lastEdited DESC")
    LiveData<List<Note>> getPinnedNotes();

    @Query("SELECT * FROM notes WHERE isPinned = 0 ORDER BY lastEdited DESC")
    LiveData<List<Note>> getUnpinnedNotes();

    @Query("SELECT * FROM notes WHERE id = :noteId")
    LiveData<Note> getNoteById(long noteId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);
}
