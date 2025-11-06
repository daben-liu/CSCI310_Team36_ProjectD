package com.alexwan.csci310_team36_projectd.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.alexwan.csci310_team36_projectd.data.model.NoteElement;

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

    public Location location;

    public Date dateCreated;

    public Date lastEdited;

    // --- Fields for Feature 4: Contextual Reminders ---

    // Type of reminder, e.g., "time" or "geo". Null if no reminder.
    public String reminderType;

    // Time for the reminder to trigger (for time-based reminders).
    public long reminderTime;

    // Name of the location for a geofence reminder.
    public String reminderLocationName;

    // Latitude for the geofence reminder.
    public double reminderLatitude;

    // Longitude for the geofence reminder.
    public double reminderLongitude;

    // Radius in meters for the geofence reminder.
    public int reminderRadius;

    // True if the reminder has been triggered and the note should be in the "Relevant Notes" section.
    public boolean isRelevant;

    // Timestamp indicating when the note should no longer be considered relevant.
    public long relevantUntil;
}
