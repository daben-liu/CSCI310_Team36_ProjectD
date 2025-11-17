package com.alexwan.csci310team36projectd.data;

import java.util.Collections;
import java.util.List;

public class FilterState {
    public final String text;
    public final List<String> tags;
    public final boolean hasPhoto;
    public final boolean hasVoiceMemo;
    public final boolean hasLocation;

    public FilterState(String text, List<String> tags, boolean hasPhoto, boolean hasVoiceMemo, boolean hasLocation) {
        this.text = text;
        this.tags = tags != null ? tags : Collections.emptyList();
        this.hasPhoto = hasPhoto;
        this.hasVoiceMemo = hasVoiceMemo;
        this.hasLocation = hasLocation;
    }

    public boolean areFiltersActive() {
        return (text != null && !text.isEmpty()) || !tags.isEmpty() || hasPhoto || hasVoiceMemo || hasLocation;
    }
}
