package com.alexwan.csci310team36projectd.data.model;

import java.util.UUID;

public abstract class NoteElement {
    private final String id;

    public NoteElement() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public abstract String getType();
}
