package com.alexwan.csci310team36projectd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import com.alexwan.csci310team36projectd.data.AppDatabase;
import com.alexwan.csci310team36projectd.data.Location;
import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.model.ChecklistElement;
import com.alexwan.csci310team36projectd.data.model.NoteElement;
import com.alexwan.csci310team36projectd.data.model.PhotoElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.alexwan.csci310team36projectd.data.model.VoiceMemoElement;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditNoteActivity extends AppCompatActivity implements NoteElementAdapter.OnTextElementFocusChangeListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int AUDIO_PERMISSION_REQUEST_CODE = 2;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 3;

    private EditText titleEditText;
    private ToggleButton pinToggleButton;
    private ChipGroup tagChipGroup;
    private Button addTagButton;
    private Button locationButton;
    private TextView locationTextView;

    private RecyclerView contentRecyclerView;
    private NoteElementAdapter noteElementAdapter;
    private List<NoteElement> noteElements;

    private ImageButton boldButton, italicButton, fontSizeButton, checklistButton, imageButton, voiceMemoButton, deleteButton;

    private AppDatabase db;
    private Note currentNote;
    private FusedLocationProviderClient fusedLocationClient;

    private int currentFontSizeIndex = 0;
    private final int[] FONT_SIZES = {14, 18, 22}; // Small, Medium, Large

    private ActivityResultLauncher<Intent> pickImageLauncher;
    private ActivityResultLauncher<Intent> recordAudioLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        db = AppDatabase.getDatabase(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize UI elements
        ImageButton backButton = findViewById(R.id.back_button);
        ImageButton saveButton = findViewById(R.id.save_button);
        titleEditText = findViewById(R.id.edit_note_title);
        pinToggleButton = findViewById(R.id.pin_toggle_button);
        tagChipGroup = findViewById(R.id.tag_chip_group);
        addTagButton = findViewById(R.id.add_tag_button);
        locationButton = findViewById(R.id.location_button);
        locationTextView = findViewById(R.id.location_text_view);

        contentRecyclerView = findViewById(R.id.content_recycler_view);
        contentRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        boldButton = findViewById(R.id.bold_button);
        italicButton = findViewById(R.id.italic_button);
        fontSizeButton = findViewById(R.id.font_size_button);
        checklistButton = findViewById(R.id.checklist_button);
        imageButton = findViewById(R.id.image_button);
        voiceMemoButton = findViewById(R.id.voice_memo_button);
        deleteButton = findViewById(R.id.delete_button);

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

        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> {
            saveNote();
            finish();
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
            noteElementAdapter.applyFontSize(FONT_SIZES[currentFontSizeIndex]);
        });
        checklistButton.setOnClickListener(v -> {
            noteElements.add(new ChecklistElement(new ArrayList<>()));
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
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
                    currentNote.location = new Location(location.getLatitude(), location.getLongitude(), "Current Location");
                    updateLocationUi();
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
        if (currentNote != null && currentNote.location != null && currentNote.location.name != null) {
            locationTextView.setText("Location: " + currentNote.location.name);
            locationTextView.setVisibility(View.VISIBLE);
        } else {
            locationTextView.setVisibility(View.GONE);
        }
    }

    private void saveNote() {
        if (currentNote == null) {
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
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note permanently?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteNote();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNote() {
        if (currentNote != null && currentNote.id != 0) {
            AppDatabase.databaseWriteExecutor.execute(() -> db.noteDao().delete(currentNote));
        }
    }

    @Override
    public void onTextElementFocusChanged(EditText focusedEditText) {
        if (noteElementAdapter != null) {
            noteElementAdapter.setFocusedEditText(focusedEditText);
        }
    }
}
