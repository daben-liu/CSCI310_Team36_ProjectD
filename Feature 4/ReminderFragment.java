package com.example.csci310_team36_projectd.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.csci310_team36_projectd.R;

public class ReminderFragment extends Fragment {
    private RadioGroup reminderTypeGroup;
    private RadioButton timeBtn, geoBtn;
    private Button saveReminderBtn;

    private Fragment activeSubFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);

        reminderTypeGroup = view.findViewById(R.id.reminderTypeGroup);
        timeBtn = view.findViewById(R.id.timeReminderBtn);
        geoBtn = view.findViewById(R.id.geoReminderBtn);
        saveReminderBtn = view.findViewById(R.id.saveReminderBtn);

        reminderTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            if (checkedId == R.id.timeReminderBtn) {
                activeSubFragment = new TimeReminderFragment();
            } else if (checkedId == R.id.geoReminderBtn) {
                activeSubFragment = new GeoReminderFragment();
            }
            transaction.replace(R.id.reminderContainer, activeSubFragment).commit();
        });

        saveReminderBtn.setOnClickListener(v -> {
            if (activeSubFragment instanceof TimeReminderFragment) {
                ((TimeReminderFragment) activeSubFragment).saveReminder();
            } else if (activeSubFragment instanceof GeoReminderFragment) {
                ((GeoReminderFragment) activeSubFragment).saveReminder();
            }
        });

        return view;
    }
}