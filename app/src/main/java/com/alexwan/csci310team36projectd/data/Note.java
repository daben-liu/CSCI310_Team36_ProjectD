package com.alexwan.csci310team36projectd.data;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.alexwan.csci310team36projectd.data.model.NoteElement;

import java.util.Date;
import java.util.List;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String title;

    public List<NoteElement> elements;

    public boolean isPinned;

    public List<String> tags;

    // Location tag for the note (e.g., where it was created)
    @Embedded(prefix = "tag_")
    public Location location;

    public Date dateCreated;

    public Date lastEdited;

    // --- Fields for Feature 4: Contextual Reminders ---

    // Type of reminder, e.g., "time" or "geo". Null if no reminder.
    public String reminderType;

    // Time for the reminder to trigger (for time-based reminders).
    public long reminderTime;

    // Location for a geofence reminder. Separate from the note's location tag.
    @Embedded(prefix = "reminder_")
    public Location reminderLocation;

    // --- DEPRECATED FIELDS ---
    // These are being replaced by the 'reminderLocation' embedded object.
    @Deprecated
    public String reminderLocationName;
    @Deprecated
    public double reminderLatitude;
    @Deprecated
    public double reminderLongitude;
    @Deprecated
    public int reminderRadius;


    // True if the reminder has been triggered and the note should be in the "Relevant Notes" section.
    public boolean isRelevant;

    // Timestamp indicating when the note should no longer be considered relevant.
    public long relevantUntil;

    public boolean isNoteRelevant(Location location) {
        long oneHourInMillis = 3600 * 1000;

        // time logic
        boolean isRelevant = (reminderTime > 0)
                && (System.currentTimeMillis() > reminderTime)
                && (System.currentTimeMillis() <= reminderTime + oneHourInMillis);

        // geo logic
        if (!isRelevant && location != null && "geo".equals(reminderType) && reminderLocation != null) {
            double distance = Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    reminderLocation.latitude,
                    reminderLocation.longitude);
            if (distance < 5000) {
                isRelevant = true;
            }
        }

        return isRelevant;
    }
}
