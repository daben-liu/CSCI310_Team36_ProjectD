package com.alexwan.csci310team36projectd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexwan.csci310team36projectd.data.FilterState;
import com.alexwan.csci310team36projectd.data.Note;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    private static final int PERMISSIONS_REQUEST_CODE = 101;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        // Headers
        pinnedHeader = findViewById(R.id.pinned_header);
        relevantHeader = findViewById(R.id.relevant_header);
        notesHeader = findViewById(R.id.notes_header);

        // Pinned Notes RecyclerView
        pinnedNotesRecyclerView = findViewById(R.id.pinned_notes_recycler_view);
        pinnedNotesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pinnedNoteAdapter = new NoteAdapter(this::onNoteClick);
        pinnedNotesRecyclerView.setAdapter(pinnedNoteAdapter);

        // Relevant Notes RecyclerView
        relevantNotesRecyclerView = findViewById(R.id.relevant_notes_recycler_view);
        relevantNotesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        relevantNoteAdapter = new NoteAdapter(this::onNoteClick);
        relevantNotesRecyclerView.setAdapter(relevantNoteAdapter);

        // Regular Notes RecyclerView
        notesRecyclerView = findViewById(R.id.notes_recycler_view);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(this::onNoteClick);
        notesRecyclerView.setAdapter(noteAdapter);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
            startActivity(intent);
        });

        setupLocationCallback();
        observeNotes();
        requestRequiredPermissions();
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        mainViewModel.updateCurrentLocation(location);
                        break; // We only need one good location
                    }
                }
            }
        };
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

        // Restore search query if it exists
        FilterState currentState = mainViewModel.getFilterState().getValue();
        if (currentState != null && !TextUtils.isEmpty(currentState.text)) {
            searchItem.expandActionView();
            searchView.setQuery(currentState.text, false); // false to not submit
            searchView.clearFocus();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handled by onQueryTextChange
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Update filter state with new text query
                FilterState currentState = mainViewModel.getFilterState().getValue();
                List<String> tags = (currentState != null) ? currentState.tags : Collections.emptyList();
                boolean hasPhoto = currentState != null && currentState.hasPhoto;
                boolean hasVoiceMemo = currentState != null && currentState.hasVoiceMemo;
                boolean hasLocation = currentState != null && currentState.hasLocation;

                mainViewModel.setFilterState(new FilterState(newText, tags, hasPhoto, hasVoiceMemo, hasLocation));
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter, null);
        builder.setView(dialogView);

        final EditText filterTags = dialogView.findViewById(R.id.filter_tags);
        final CheckBox filterHasPhoto = dialogView.findViewById(R.id.filter_has_photo);
        final CheckBox filterHasVoiceMemo = dialogView.findViewById(R.id.filter_has_voice_memo);
        final CheckBox filterHasLocation = dialogView.findViewById(R.id.filter_has_location);

        // Populate dialog with current filter state
        FilterState currentState = mainViewModel.getFilterState().getValue();
        if (currentState != null) {
            filterTags.setText(TextUtils.join(",", currentState.tags));
            filterHasPhoto.setChecked(currentState.hasPhoto);
            filterHasVoiceMemo.setChecked(currentState.hasVoiceMemo);
            filterHasLocation.setChecked(currentState.hasLocation);
        }

        builder.setTitle("Filter Notes")
                .setPositiveButton("Apply", (dialog, which) -> {
                    // Preserve the current text search term
                    FilterState finalCurrentState = mainViewModel.getFilterState().getValue();
                    String text = (finalCurrentState != null) ? finalCurrentState.text : null;

                    String tagsRaw = filterTags.getText().toString().trim();
                    List<String> tags = tagsRaw.isEmpty() ? Collections.emptyList() : Arrays.asList(tagsRaw.split("\\s*,\\s*"));

                    boolean hasPhoto = filterHasPhoto.isChecked();
                    boolean hasVoiceMemo = filterHasVoiceMemo.isChecked();
                    boolean hasLocation = filterHasLocation.isChecked();

                    FilterState newState = new FilterState(text, tags, hasPhoto, hasVoiceMemo, hasLocation);
                    mainViewModel.setFilterState(newState);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("Clear", (dialog, which) -> {
                    // Clear only the dialog's filters, preserve text search
                    FilterState finalCurrentState = mainViewModel.getFilterState().getValue();
                    String text = (finalCurrentState != null) ? finalCurrentState.text : null;
                    mainViewModel.setFilterState(new FilterState(text, null, false, false, false));
                });

        builder.create().show();
    }


    @Override
    public void onNoteClick(Note note) {
        Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
        intent.putExtra("note_id", note.id);
        startActivity(intent);
    }

    private void requestRequiredPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Check if all requested permissions were granted
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                // Permissions were granted, so we can start location updates
                startLocationUpdates();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Permissions are not granted.
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60000) // 60 seconds
                .setMinUpdateIntervalMillis(30000) // 30 seconds
                .build();

        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
}
