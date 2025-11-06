package com.alexwan.csci310_team36_projectd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alexwan.csci310_team36_projectd.data.AppDatabase;
import com.alexwan.csci310_team36_projectd.data.model.ChecklistElement;
import com.alexwan.csci310_team36_projectd.data.Location;
import com.alexwan.csci310_team36_projectd.data.Note;
import com.alexwan.csci310_team36_projectd.data.model.NoteElement;
import com.alexwan.csci310_team36_projectd.data.model.PhotoElement;
import com.alexwan.csci310_team36_projectd.data.model.TextElement;
import com.alexwan.csci310_team36_projectd.data.model.VoiceMemoElement;
import com.alexwan.csci310_team36_projectd.ui.ReminderFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity implements NoteElementAdapter.OnTextElementFocusChangeListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 2;
    private static final int AUDIO_PERMISSION_REQUEST_CODE = 3;
    private static final float[] FONT_SIZES = {14f, 18f, 22f};

    private EditText titleEditText;
    private RecyclerView contentRecyclerView;
    private ImageButton backButton, saveButton, boldButton, italicButton, fontSizeButton, checklistButton, imageButton, voiceMemoButton, reminderButton;
    private Button addTagButton, locationButton;
    private ToggleButton pinToggleButton;
    private ChipGroup tagChipGroup;
    private TextView locationTextView;
    private LinearLayout reminderDisplayLayout;
    private TextView reminderTypeTextView;
    private TextView reminderDetailsTextView;

    private Note currentNote;
    private AppDatabase db;
    private NoteElementAdapter noteElementAdapter;
    private ArrayList<NoteElement> noteElements;
    private int currentFontSizeIndex = 0;

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> recordAudioLauncher;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onTextElementFocusChanged(EditText focusedEditText) {
        // This is the implementation for the interface.
        // We can use this to update the toolbar state based on the focused text element.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        db = AppDatabase.getDatabase(getApplicationContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        titleEditText = findViewById(R.id.edit_note_title);
        contentRecyclerView = findViewById(R.id.content_recycler_view);
        contentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        backButton = findViewById(R.id.back_button);
        saveButton = findViewById(R.id.save_button);
        reminderButton = findViewById(R.id.reminder_button);
        boldButton = findViewById(R.id.bold_button);
        italicButton = findViewById(R.id.italic_button);
        fontSizeButton = findViewById(R.id.font_size_button);
        checklistButton = findViewById(R.id.checklist_button);
        imageButton = findViewById(R.id.image_button);
        voiceMemoButton = findViewById(R.id.voice_memo_button);
        pinToggleButton = findViewById(R.id.pin_toggle_button);
        addTagButton = findViewById(R.id.add_tag_button);
        tagChipGroup = findViewById(R.id.tag_chip_group);
        locationButton = findViewById(R.id.location_button);
        locationTextView = findViewById(R.id.location_text_view);
        reminderDisplayLayout = findViewById(R.id.reminder_display_layout);
        reminderTypeTextView = findViewById(R.id.reminder_type_text_view);
        reminderDetailsTextView = findViewById(R.id.reminder_details_text_view);
        ImageButton deleteButton = findViewById(R.id.delete_button);

        long noteId = getIntent().getLongExtra("note_id", -1);

        if (noteId != -1) {
            db.noteDao().getNoteById(noteId).observe(this, note -> {
                if (note != null) {
                    currentNote = note;
                    if (currentNote.tags == null) currentNote.tags = new ArrayList<>();
                    if (currentNote.elements == null) currentNote.elements = new ArrayList<>();

                    titleEditText.setText(note.title);
                    pinToggleButton.setChecked(note.isPinned);
                    updateTagsUi();
                    updateLocationUi();
                    updateReminderUi();

                    noteElements = new ArrayList<>(currentNote.elements);
                    noteElementAdapter = new NoteElementAdapter(this, noteElements, this);
                    contentRecyclerView.setAdapter(noteElementAdapter);
                }
            });
        } else {
            currentNote = new Note();
            currentNote.tags = new ArrayList<>();
            currentNote.elements = new ArrayList<>();
            // Add an initial empty text element for new notes
            noteElements = new ArrayList<>();
            noteElements.add(new TextElement(""));
            noteElementAdapter = new NoteElementAdapter(this, noteElements, this);
            contentRecyclerView.setAdapter(noteElementAdapter);
        }

        backButton.setOnClickListener(v -> saveNote(this::finish));
        saveButton.setOnClickListener(v -> saveNote(this::finish));

        reminderButton.setOnClickListener(v -> {
            if (currentNote.id == 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Save Note")
                        .setMessage("Please save the note before adding a reminder.")
                        .setPositiveButton("OK", null)
                        .show();
                return;
            }

            ReminderFragment reminderFragment = new ReminderFragment();
            Bundle args = new Bundle();
            args.putLong("note_id", currentNote.id);
            reminderFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, reminderFragment)
                    .addToBackStack(null)
                    .commit();
        });

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        pinToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentNote != null) currentNote.isPinned = isChecked;
        });

        addTagButton.setOnClickListener(v -> showAddTagDialog());
        locationButton.setOnClickListener(v -> showLocationDialog());

        setupToolbarListeners();
        setupActivityResults();
    }

    private void setupToolbarListeners() {
        boldButton.setOnClickListener(v -> noteElementAdapter.toggleBold());
        italicButton.setOnClickListener(v -> noteElementAdapter.toggleItalic());
        fontSizeButton.setOnClickListener(v -> {
            currentFontSizeIndex = (currentFontSizeIndex + 1) % FONT_SIZES.length;
            noteElementAdapter.applyFontSize((int) FONT_SIZES[currentFontSizeIndex]);
        });
        checklistButton.setOnClickListener(v -> {
            noteElements.add(new ChecklistElement(""));
            noteElementAdapter.notifyItemInserted(noteElements.size() - 1);
        });
        imageButton.setOnClickListener(v -> pickImage());
        voiceMemoButton.setOnClickListener(v -> recordAudio());
    }

    private void setupActivityResults() {
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            noteElements.add(new PhotoElement(imageUri.toString()));
                            noteElementAdapter.notifyItemInserted(noteElements.size() - 1);
                        }
                    }
                });

        recordAudioLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri audioUri = result.getData().getData();
                        if (audioUri != null) {
                            noteElements.add(new VoiceMemoElement(audioUri.toString()));
                            noteElementAdapter.notifyItemInserted(noteElements.size() - 1);
                        }
                    }
                });
    }

    private void pickImage() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImageLauncher.launch(intent);
        }
    }

    private void recordAudio() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            recordAudioLauncher.launch(intent);
        }
    }


    private void updateTagsUi() {
        tagChipGroup.removeAllViews();
        if (currentNote != null && currentNote.tags != null) {
            for (String tag : currentNote.tags) {
                addChipToGroup(tag);
            }
        }
    }

    private void addChipToGroup(String tag) {
        Chip chip = new Chip(this);
        chip.setText(tag);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            if (currentNote != null && currentNote.tags != null) {
                currentNote.tags.remove(tag);
                updateTagsUi();
            }
        });
        tagChipGroup.addView(chip);
    }

    private void showAddTagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Tag");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String tag = input.getText().toString().trim();
            if (!tag.isEmpty() && currentNote != null && currentNote.tags != null && !currentNote.tags.contains(tag)) {
                currentNote.tags.add(tag);
                updateTagsUi();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showLocationDialog() {
        ArrayList<String> options = new ArrayList<>();
        options.add("Automatically Detect and Add Location");
        options.add("Add Manual Location");
        if (currentNote != null && currentNote.location != null) {
            options.add("Delete Location");
        }

        new AlertDialog.Builder(this)
                .setTitle("Location Options")
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    String selectedOption = options.get(which);
                    if (selectedOption.equals("Automatically Detect and Add Location")) {
                        requestLocationPermission();
                    } else if (selectedOption.equals("Add Manual Location")) {
                        showManualLocationDialog();
                    } else if (selectedOption.equals("Delete Location")) {
                        currentNote.location = null;
                        updateLocationUi();
                    }
                }).show();
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else if (requestCode == AUDIO_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            recordAudio();
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null && currentNote != null) {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        Geocoder geocoder = new Geocoder(EditNoteActivity.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            String locationName = "Current Location"; // Default
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                if (address.getLocality() != null) {
                                    locationName = address.getLocality();
                                } else if (address.getSubAdminArea() != null) {
                                    locationName = address.getSubAdminArea();
                                } else if (address.getAdminArea() != null) {
                                    locationName = address.getAdminArea();
                                }
                            }
                            currentNote.location = new Location(location.getLatitude(), location.getLongitude(), locationName);
                            runOnUiThread(this::updateLocationUi);
                        } catch (IOException e) {
                            currentNote.location = new Location(location.getLatitude(), location.getLongitude(), "Current Location");
                            runOnUiThread(this::updateLocationUi);
                        }
                    });
                }
            });
        }
    }

    private void showManualLocationDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Location Name");

        new AlertDialog.Builder(this)
                .setTitle("Add Manual Location")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty() && currentNote != null) {
                        currentNote.location = new Location(0, 0, name);
                        updateLocationUi();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .show();
    }

    private void updateLocationUi() {
        if (currentNote != null && currentNote.location != null && currentNote.location.getName() != null) {
            locationTextView.setText("Location: " + currentNote.location.getName());
            locationTextView.setVisibility(View.VISIBLE);
        } else {
            locationTextView.setVisibility(View.GONE);
        }
    }

    private void updateReminderUi() {
        if (currentNote != null && currentNote.reminderType != null && !currentNote.reminderType.isEmpty()) {
            reminderDisplayLayout.setVisibility(View.VISIBLE);
            String type = currentNote.reminderType.substring(0, 1).toUpperCase() + currentNote.reminderType.substring(1);
            reminderTypeTextView.setText("Reminder Type: " + type);

            if ("time".equalsIgnoreCase(currentNote.reminderType)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                reminderDetailsTextView.setText("At: " + sdf.format(new Date(currentNote.reminderTime)));
            } else if ("geo".equalsIgnoreCase(currentNote.reminderType) && currentNote.location != null) {
                reminderDetailsTextView.setText("At: " + currentNote.location.getName());
            }
        } else {
            reminderDisplayLayout.setVisibility(View.GONE);
        }
    }

    private void saveNote(Runnable onComplete) {
        if (currentNote == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        String title = titleEditText.getText().toString().trim();
        List<NoteElement> finalElements = noteElementAdapter.getElements();

        currentNote.title = title;
        currentNote.lastEdited = new Date();
        currentNote.elements = finalElements;

        AppDatabase.databaseWriteExecutor.execute(() -> {
            boolean isNewNote = currentNote.id == 0;
            boolean hasContent = !finalElements.isEmpty() && !(finalElements.size() == 1 && finalElements.get(0) instanceof TextElement && ((TextElement) finalElements.get(0)).isEmpty());
            boolean isEmpty = title.isEmpty() && !hasContent && (currentNote.tags == null || currentNote.tags.isEmpty());

            if (isEmpty) {
                if (!isNewNote) {
                    db.noteDao().delete(currentNote);
                }
            } else {
                if (isNewNote) {
                    currentNote.dateCreated = new Date();
                    db.noteDao().insert(currentNote);
                } else {
                    db.noteDao().update(currentNote);
                }
            }
            if (onComplete != null) {
                runOnUiThread(onComplete);
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note permanently?")
                .setPositiveButton("Delete", (dialog, which) -> deleteNote(this::finish))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNote(Runnable onComplete) {
        if (currentNote != null && currentNote.id != 0) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                db.noteDao().delete(currentNote);
                if (onComplete != null) {
                    runOnUiThread(onComplete);
                }
            });
        } else if (onComplete != null) {
            onComplete.run();
        }
    }
}
