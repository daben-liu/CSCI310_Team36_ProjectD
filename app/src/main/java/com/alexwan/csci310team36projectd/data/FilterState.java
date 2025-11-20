package com.alexwan.csci310team36projectd.data;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FilterState {
    public final String text;
    public final List<String> tags;
    public final boolean hasPhoto;
    public final boolean hasVoiceMemo;
    public final boolean hasLocation;
    public final Date startDate;
    public final Date endDate;

    public FilterState(String text, List<String> tags, boolean hasPhoto, boolean hasVoiceMemo, boolean hasLocation) {
        this(text, tags, hasPhoto, hasVoiceMemo, hasLocation, null, null);
    }

    public FilterState(String text, List<String> tags, boolean hasPhoto, boolean hasVoiceMemo, boolean hasLocation, Date startDate, Date endDate) {
        this.text = text;
        this.tags = tags != null ? tags : Collections.emptyList();
        this.hasPhoto = hasPhoto;
        this.hasVoiceMemo = hasVoiceMemo;
        this.hasLocation = hasLocation;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean areFiltersActive() {
        return (text != null && !text.isEmpty()) || !tags.isEmpty() || hasPhoto || hasVoiceMemo || hasLocation || startDate != null || endDate != null;
    }
}
