package com.alexwan.csci310_team36_projectd.data.model;

import java.util.Date; // For default timestamp
import java.util.UUID;

public abstract class NoteElement {
    private final String id;
    private long timestamp; // Added timestamp field

    public NoteElement() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = new Date().getTime(); // Default to current time
    }

    // Modified constructor to accept timestamp
    public NoteElement(String id, long timestamp) {
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

    public abstract String getType();
}
