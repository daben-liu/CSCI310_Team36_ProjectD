package com.alexwan.csci310team36projectd;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
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
import com.alexwan.csci310team36projectd.data.model.NoteElement;
import com.alexwan.csci310team36projectd.data.model.PhotoElement;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.alexwan.csci310team36projectd.data.model.VoiceMemoElement;
import com.alexwan.csci310team36projectd.ui.ReminderFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditNoteActivity extends AppCompatActivity implements NoteElementAdapter.OnTextElementFocusChangeListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int AUDIO_PERMISSION_REQUEST_CODE = 3;
    private static final float[] FONT_SIZES = {14f, 18f, 22f};

    private EditText titleEditText;
    private RecyclerView contentRecyclerView;
    private ImageButton backButton, saveButton, boldButton, italicButton, fontSizeButton, checklistButton, imageButton, voiceMemoButton, reminderButton, pinToggleButton;
    private Button addTagButton, locationButton;
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

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    private FusedLocationProviderClient fusedLocationClient;

    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;
    private String currentAudioPath = null;

    // Flag to determine if location is being fetched for the tag or for the reminder
    private boolean isFetchingForReminder = false;

    @Override
    public void onTextElementFocusChanged(EditText focusedEditText) {
        // This is the implementation for the interface.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        db = AppDatabase.getDatabase(getApplicationContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // --- View Initialization ---
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
                    updatePinButtonIcon();
                    updateTagsUi();
                    updateLocationUi();
                    updateReminderUi();

                    if (noteElementAdapter == null || !noteElements.equals(currentNote.elements)) {
                        noteElements = new ArrayList<>(currentNote.elements);
                        noteElementAdapter = new NoteElementAdapter(this, noteElements, this);
                        contentRecyclerView.setAdapter(noteElementAdapter);
                    }
                }
            });
        } else {
            currentNote = new Note();
            currentNote.tags = new ArrayList<>();
            currentNote.elements = new ArrayList<>();
            noteElements = new ArrayList<>();
            noteElements.add(new TextElement(""));
            noteElementAdapter = new NoteElementAdapter(this, noteElements, this);
            contentRecyclerView.setAdapter(noteElementAdapter);
        }

        // --- Listeners ---
        backButton.setOnClickListener(v -> saveNote(this::finish));
        saveButton.setOnClickListener(v -> saveNote(this::finish));
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
        pinToggleButton.setOnClickListener(v -> {
            if (currentNote != null) {
                currentNote.isPinned = !currentNote.isPinned;
                updatePinButtonIcon();
            }
        });

        // Feature-specific listeners
        addTagButton.setOnClickListener(v -> showAddTagDialog());
        locationButton.setOnClickListener(v -> showLocationDialog()); // This button is for the note's location TAG
        reminderButton.setOnClickListener(v -> openReminderFragment());

        // Toolbar listeners
        boldButton.setOnClickListener(v -> noteElementAdapter.toggleBold());
        italicButton.setOnClickListener(v -> noteElementAdapter.toggleItalic());
        fontSizeButton.setOnClickListener(v -> {
            currentFontSizeIndex = (currentFontSizeIndex + 1) % FONT_SIZES.length;
            noteElementAdapter.applyFontSize((int) FONT_SIZES[currentFontSizeIndex]);
        });
        checklistButton.setOnClickListener(v -> noteElementAdapter.addChecklistItemOrChecklist());
        imageButton.setOnClickListener(v -> pickImage());
        voiceMemoButton.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            } else {
                startRecording();
            }
        });

        setupActivityResults();
    }

    private void openReminderFragment() {
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
    }

    // --- Location Tag Logic ---
    private void showLocationDialog() {
        ArrayList<String> options = new ArrayList<>();
        options.add("Automatically Detect and Add Location Tag");
        options.add("Add Manual Location Tag");
        if (currentNote != null && currentNote.location != null) {
            options.add("Remove Location Tag");
        }

        new AlertDialog.Builder(this)
                .setTitle("Location Tag Options")
                .setItems(options.toArray(new String[0]), (dialog, which) -> {
                    String selectedOption = options.get(which);
                    if (selectedOption.contains("Automatically Detect")) {
                        isFetchingForReminder = false; // Set purpose to TAG
                        requestLocationPermission();
                    } else if (selectedOption.contains("Manual")) {
                        showManualLocationDialog();
                    } else if (selectedOption.contains("Remove")) {
                        currentNote.location = null;
                        updateLocationUi();
                    }
                }).show();
    }

    private void showManualLocationDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Location Name");

        new AlertDialog.Builder(this)
                .setTitle("Add Manual Location Tag")
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

    private void fetchLocationForTag() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        Toast.makeText(this, "Fetching location for tag...", Toast.LENGTH_SHORT).show();
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
            .addOnSuccessListener(this, location -> {
                if (location != null && currentNote != null) {
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        String locationName = geocodeLocation(location);
                        currentNote.location = new Location(location.getLatitude(), location.getLongitude(), locationName);
                        // No need to update db here, will be saved with the note
                        runOnUiThread(this::updateLocationUi);
                    });
                }
            });
    }

    private void updateLocationUi() {
        if (currentNote != null && currentNote.location != null && currentNote.location.getName() != null) {
            locationTextView.setText("Location Tag: " + currentNote.location.getName());
            locationTextView.setVisibility(View.VISIBLE);
        } else {
            locationTextView.setVisibility(View.GONE);
        }
    }

    // --- Reminder Location Logic ---
    private void fetchLocationForReminder() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        Toast.makeText(this, "Fetching location for reminder...", Toast.LENGTH_SHORT).show();
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location != null && currentNote != null) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            String locationName = geocodeLocation(location);
                            if (currentNote.reminderLocation == null) {
                                currentNote.reminderLocation = new Location();
                            }
                            currentNote.reminderLocation.latitude = location.getLatitude();
                            currentNote.reminderLocation.longitude = location.getLongitude();
                            currentNote.reminderLocation.setName(locationName);

                            // Save the change immediately since this is an automatic background action
                            if (currentNote.id != 0) {
                                db.noteDao().update(currentNote);
                            }
                            runOnUiThread(this::updateReminderUi);
                        });
                    }
                });
    }

    private void updateReminderUi() {
        if (currentNote != null && currentNote.reminderType != null && !currentNote.reminderType.isEmpty()) {
            reminderDisplayLayout.setVisibility(View.VISIBLE);
            String type = currentNote.reminderType.substring(0, 1).toUpperCase() + currentNote.reminderType.substring(1);
            reminderTypeTextView.setText("Reminder: " + type);

            if ("time".equalsIgnoreCase(currentNote.reminderType)) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                reminderDetailsTextView.setText("At: " + sdf.format(new Date(currentNote.reminderTime)));
                reminderDetailsTextView.setVisibility(View.VISIBLE);
            } else if ("geo".equalsIgnoreCase(currentNote.reminderType)) {
                if (currentNote.reminderLocation != null && currentNote.reminderLocation.getName() != null && !currentNote.reminderLocation.getName().isEmpty()) {
                    reminderDetailsTextView.setText("At: " + currentNote.reminderLocation.getName());
                    reminderDetailsTextView.setVisibility(View.VISIBLE);
                } else {
                    reminderDetailsTextView.setText("At: (Fetching location...)");
                    reminderDetailsTextView.setVisibility(View.VISIBLE);
                    isFetchingForReminder = true; // Set purpose to REMINDER
                    requestLocationPermission();
                }
            } else {
                reminderDetailsTextView.setVisibility(View.GONE);
            }
        } else {
            reminderDisplayLayout.setVisibility(View.GONE);
        }
    }

    // --- Shared Permission & Geocoding Logic ---
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            if (isFetchingForReminder) {
                fetchLocationForReminder();
            } else {
                fetchLocationForTag();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isFetchingForReminder) {
                    fetchLocationForReminder();
                } else {
                    fetchLocationForTag();
                }
            } else {
                Toast.makeText(this, "Location permission is required for this feature.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == AUDIO_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, "Audio permission is required for voice memos.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String geocodeLocation(android.location.Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (address.getLocality() != null) return address.getLocality();
                if (address.getSubAdminArea() != null) return address.getSubAdminArea();
                if (address.getAdminArea() != null) return address.getAdminArea();
            }
        } catch (IOException e) {
            Log.e("EditNoteActivity", "Geocoding failed", e);
        }
        return String.format(Locale.getDefault(), "Lat: %.4f, Lon: %.4f", location.getLatitude(), location.getLongitude());
    }
    
    // --- Other Methods (Save, Delete, Tags, Media) ---
    // [This section contains the rest of the original methods like saveNote, deleteNote, setupActivityResults, etc.]
    // [No changes were made to these methods in this refactoring]

    private void updatePinButtonIcon() {
        if (currentNote != null && currentNote.isPinned) {
            pinToggleButton.setImageResource(R.drawable.ic_pin_filled);
        } else {
            pinToggleButton.setImageResource(R.drawable.ic_pin);
        }
    }

    private void setupActivityResults() {
        pickMediaLauncher = registerForActivityResult(new PickVisualMedia(), uri -> {
            if (uri != null) {
                String filePath = saveImageToInternalStorage(uri);
                if (filePath != null) {
                    noteElements.add(new PhotoElement(filePath));
                    noteElementAdapter.notifyItemInserted(noteElements.size() - 1);
                }
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File directory = new File(getFilesDir(), "images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);

            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("EditNoteActivity", "Failed to save image", e);
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    private void pickImage() {
        pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_REQUEST_CODE);
            return;
        }

        File audioDir = new File(getFilesDir(), "voicerecorder");
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        File audioFile = new File(audioDir, "recording_" + System.currentTimeMillis() + ".3gp");
        currentAudioPath = audioFile.getAbsolutePath();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mediaRecorder = new MediaRecorder(this);
        } else {
            mediaRecorder = new MediaRecorder();
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(currentAudioPath);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Toast.makeText(this, "Recording started...", Toast.LENGTH_SHORT).show();
            voiceMemoButton.setImageResource(R.drawable.ic_stop); // Change icon to stop
        } catch (IOException e) {
            Log.e("EditNoteActivity", "prepare() failed", e);
            Toast.makeText(this, "Recording failed to start", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            isRecording = false;
            Toast.makeText(this, "Recording stopped.", Toast.LENGTH_SHORT).show();
            voiceMemoButton.setImageResource(R.drawable.ic_mic); // Change icon back to mic

            if (currentAudioPath != null) {
                noteElements.add(new VoiceMemoElement(currentAudioPath));
                noteElementAdapter.notifyItemInserted(noteElements.size() - 1);
                currentAudioPath = null;
            }
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
                    long newId = db.noteDao().insert(currentNote);
                    currentNote.id = newId;
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
