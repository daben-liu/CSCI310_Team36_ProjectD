package com.alexwan.csci310team36projectd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexwan.csci310team36projectd.data.AppDatabase;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private RecyclerView pinnedNotesRecyclerView;
    private RecyclerView notesRecyclerView;
    private NoteAdapter pinnedNoteAdapter;
    private NoteAdapter noteAdapter;
    private TextView pinnedHeader;
    private TextView notesHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AppDatabase.getDatabase(getApplicationContext());

        // Headers
        pinnedHeader = findViewById(R.id.pinned_header);
        notesHeader = findViewById(R.id.notes_header);

        // Click listener for both adapters
        NoteAdapter.OnNoteClickListener onNoteClickListener = note -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            intent.putExtra("note_id", note.id);
            startActivity(intent);
        };

        // Pinned Notes RecyclerView
        pinnedNotesRecyclerView = findViewById(R.id.pinned_notes_recycler_view);
        pinnedNotesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pinnedNoteAdapter = new NoteAdapter(new ArrayList<>(), onNoteClickListener);
        pinnedNotesRecyclerView.setAdapter(pinnedNoteAdapter);

        // Regular Notes RecyclerView
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(new ArrayList<>(), onNoteClickListener);
        notesRecyclerView.setAdapter(noteAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);
        });

        observeNotes();
    }

    private void observeNotes() {
        db.noteDao().getPinnedNotes().observe(this, pinnedNotes -> {
            if (pinnedNotes != null && !pinnedNotes.isEmpty()) {
                pinnedHeader.setVisibility(View.VISIBLE);
                pinnedNoteAdapter.setNotes(pinnedNotes);
            } else {
                pinnedHeader.setVisibility(View.GONE);
                pinnedNoteAdapter.setNotes(new ArrayList<>());
            }
        });

        db.noteDao().getUnpinnedNotes().observe(this, unpinnedNotes -> {
            if (unpinnedNotes != null && !unpinnedNotes.isEmpty()) {
                notesHeader.setVisibility(View.VISIBLE);
                noteAdapter.setNotes(unpinnedNotes);
            } else {
                notesHeader.setVisibility(View.GONE);
                noteAdapter.setNotes(new ArrayList<>());
            }
        });
    }
}
