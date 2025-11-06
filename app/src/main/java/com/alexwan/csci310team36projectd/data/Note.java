package com.alexwan.csci310team36projectd.data;

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

    public Location location;

    public Date dateCreated;

    public Date lastEdited;
}
