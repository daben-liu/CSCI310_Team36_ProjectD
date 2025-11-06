package com.alexwan.csci310_team36_projectd.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.alexwan.csci310_team36_projectd.MainViewModel;
import com.alexwan.csci310_team36_projectd.R;
import com.alexwan.csci310_team36_projectd.data.AppDatabase;
import com.alexwan.csci310_team36_projectd.data.Note;

public class ReminderFragment extends Fragment {
    private RadioGroup reminderTypeGroup;
    private Button saveReminderBtn;
    private Button cancelBtn;
    private Fragment activeSubFragment;
    private MainViewModel mainViewModel;
    private long noteId = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            noteId = getArguments().getLong("note_id", -1);
        }
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);
        view.setBackgroundResource(android.R.color.white);

        reminderTypeGroup = view.findViewById(R.id.reminderTypeGroup);
        saveReminderBtn = view.findViewById(R.id.saveReminderBtn);
        cancelBtn = view.findViewById(R.id.cancelBtn);

        if (savedInstanceState == null) {
            activeSubFragment = new TimeReminderFragment();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.reminderContainer, activeSubFragment)
                    .commit();
        }

        reminderTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.timeReminderBtn) {
                activeSubFragment = new TimeReminderFragment();
            } else if (checkedId == R.id.geoReminderBtn) {
                activeSubFragment = new GeoReminderFragment();
            }
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.reminderContainer, activeSubFragment)
                    .commit();
        });

        saveReminderBtn.setOnClickListener(v -> saveReminderAndExit());

        cancelBtn.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().beginTransaction().remove(ReminderFragment.this).commit();
            }
        });

        return view;
    }

    private void saveReminderAndExit() {
        if (noteId == -1) {
            Toast.makeText(getContext(), "Error: Could not save reminder. Invalid note.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle reminderData = null;
        if (activeSubFragment instanceof TimeReminderFragment) {
            reminderData = ((TimeReminderFragment) activeSubFragment).getReminderData();
        } else if (activeSubFragment instanceof GeoReminderFragment) {
            reminderData = ((GeoReminderFragment) activeSubFragment).getReminderData();
        }

        if (reminderData == null) {
            // The sub-fragment is responsible for showing a toast if its data is invalid
            return;
        }

        final Bundle finalReminderData = reminderData;

        // Run database operations on a background thread for safety.
        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Fetch the note using the blocking method.
            Note note = mainViewModel.getNoteByIdBlocking(noteId);

            if (note != null) {
                // Update the note object with the new reminder details.
                String reminderType = finalReminderData.getString("reminderType");
                note.reminderType = reminderType;

                if ("time".equals(reminderType)) {
                    note.reminderTime = finalReminderData.getLong("reminderTime");
                } else if ("geo".equals(reminderType)) {
                    note.reminderLocationName = finalReminderData.getString("locationName");
                }

                // Save the updated note back to the database.
                mainViewModel.update(note);

                // Switch back to the main thread to show UI feedback and close the fragment.
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Reminder saved!", Toast.LENGTH_SHORT).show();
                        // Safely remove the fragment
                        if (getParentFragmentManager() != null) {
                            getParentFragmentManager().beginTransaction().remove(ReminderFragment.this).commit();
                        }
                    });
                }
            }
        });
    }
}
