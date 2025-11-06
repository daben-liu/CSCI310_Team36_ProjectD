package com.alexwan.csci310_team36_projectd;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexwan.csci310_team36_projectd.data.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    private MainViewModel mainViewModel;
    private RecyclerView pinnedNotesRecyclerView;
    private RecyclerView relevantNotesRecyclerView;
    private RecyclerView notesRecyclerView;
    private NoteAdapter pinnedNoteAdapter;
    private NoteAdapter relevantNoteAdapter;
    private NoteAdapter noteAdapter;
    private TextView pinnedHeader;
    private TextView relevantHeader;
    private TextView notesHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Headers
        pinnedHeader = findViewById(R.id.pinned_header);
        relevantHeader = findViewById(R.id.relevant_header);
        notesHeader = findViewById(R.id.notes_header);

        // Pinned Notes RecyclerView
        pinnedNotesRecyclerView = findViewById(R.id.pinned_notes_recycler_view);
        pinnedNotesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pinnedNoteAdapter = new NoteAdapter(this);
        pinnedNotesRecyclerView.setAdapter(pinnedNoteAdapter);

        // Relevant Notes RecyclerView
        relevantNotesRecyclerView = findViewById(R.id.relevant_notes_recycler_view);
        relevantNotesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        relevantNoteAdapter = new NoteAdapter(this);
        relevantNotesRecyclerView.setAdapter(relevantNoteAdapter);

        // Regular Notes RecyclerView
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(this);
        notesRecyclerView.setAdapter(noteAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);
        });

        observeNotes();
    }

    private void observeNotes() {
        // Observer for Pinned Notes
        mainViewModel.getPinnedNotes().observe(this, pinnedNotes -> {
            pinnedHeader.setVisibility(pinnedNotes.isEmpty() ? View.GONE : View.VISIBLE);
            pinnedNoteAdapter.submitList(pinnedNotes);
        });

        // Observer for Relevant Notes
        mainViewModel.getRelevantNotes().observe(this, relevantNotes -> {
            relevantHeader.setVisibility(relevantNotes.isEmpty() ? View.GONE : View.VISIBLE);
            relevantNoteAdapter.submitList(relevantNotes);
        });

        // Observer for all other notes (using the new de-duplicated stream)
        mainViewModel.getOtherNotes().observe(this, otherNotes -> {
            notesHeader.setVisibility(otherNotes.isEmpty() ? View.GONE : View.VISIBLE);
            noteAdapter.submitList(otherNotes);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mainViewModel.setSearchQuery(query);
                searchView.clearFocus(); // Dismiss the keyboard
                return true; // We've handled the event
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mainViewModel.setSearchQuery(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
        intent.putExtra("note_id", note.id);
        startActivity(intent);
    }

    @Override
    public void onPinClick(Note note) {
        note.isPinned = !note.isPinned;
        mainViewModel.update(note);
    }
}
