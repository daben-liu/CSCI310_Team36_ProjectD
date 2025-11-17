package com.alexwan.csci310team36projectd.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM notes WHERE isPinned = 1 ORDER BY lastEdited DESC")
    LiveData<List<Note>> getPinnedNotes();

    @Query("SELECT * FROM notes WHERE isPinned = 0 ORDER BY lastEdited DESC")
    LiveData<List<Note>> getUnpinnedNotes();

    @Query("SELECT * FROM notes WHERE id = :noteId")
    LiveData<Note> getNoteById(long noteId);

    @Query("SELECT * FROM notes WHERE id = :noteId")
    Note getNoteByIdBlocking(long noteId);

    @Query("SELECT * FROM notes WHERE (reminderType = 'time' AND reminderTime >= :oneHourAgo AND reminderTime < :currentTime) OR (reminderType = 'geo')")
    LiveData<List<Note>> getRelevantNotes(long currentTime, long oneHourAgo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    // --- New Dynamic Filtering Logic ---

    @RawQuery(observedEntities = Note.class)
    LiveData<List<Note>> getFilteredNotes(SupportSQLiteQuery query);

    /**
     * This is the main query builder. It takes a FilterState object and constructs
     * a dynamic SQL query to filter notes based on the active criteria.
     *
     * @param filterState The current state of all user-selected filters.
     * @return LiveData containing the filtered list of notes, ordered by last edited.
     */
    default LiveData<List<Note>> getNotes(FilterState filterState) {
        if (filterState == null || !filterState.areFiltersActive()) {
            // If no filters are active, return all unpinned notes.
            return getUnpinnedNotes();
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM notes WHERE isPinned = 0");
        List<Object> args = new ArrayList<>();

        // Text Query: Searches for each keyword as a substring in title or elements.
        if (filterState.text != null && !filterState.text.trim().isEmpty()) {
            String[] keywords = filterState.text.trim().split("\\s+");
            for (String keyword : keywords) {
                if (!keyword.isEmpty()) {
                    sql.append(" AND (LOWER(title) LIKE LOWER(?) OR LOWER(elements) LIKE LOWER(?))");
                    String arg = "%" + keyword + "%";
                    args.add(arg);
                    args.add(arg);
                }
            }
        }

        // Tags Query: Searches for notes containing ALL specified tags.
        if (filterState.tags != null && !filterState.tags.isEmpty()) {
            for (String tag : filterState.tags) {
                if (!tag.trim().isEmpty()) {
                    // Ensures we match the whole tag within the comma-separated string.
                    sql.append(" AND (',' || tags || ',' LIKE ?)");
                    args.add("%," + tag.trim() + ",%");
                }
            }
        }

        // Media Presence Query
        if (filterState.hasPhoto) {
            sql.append(" AND elements LIKE '%\"type\":\"photo\"%'");
        }
        if (filterState.hasVoiceMemo) {
            sql.append(" AND elements LIKE '%\"type\":\"voice_memo\"%'");
        }
        if (filterState.hasLocation) {
            sql.append(" AND location IS NOT NULL AND location != ''");
        }

        sql.append(" ORDER BY lastEdited DESC");

        return getFilteredNotes(new SimpleSQLiteQuery(sql.toString(), args.toArray()));
    }
}
