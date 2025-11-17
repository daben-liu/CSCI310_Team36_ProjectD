package com.alexwan.csci310team36projectd.data.model;

import java.util.Date; // For default timestamp
import java.util.UUID;

public abstract class NoteElement {
    private final String id;
    private long timestamp; // Added timestamp field

    // Define the enum for element types
    public enum Type {
        PHOTO,
        TEXT,
        CHECKLIST,
        VOICE_MEMO
    }

    public NoteElement() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date().getTime(); // Default to current time
    }

    // Modified constructor to accept timestamp
    protected NoteElement(String id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    // Added getTimestamp() method
    public long getTimestamp() {
        return timestamp;
    }

    // Updated getType() to return the enum Type
    public abstract Type getType();
}
