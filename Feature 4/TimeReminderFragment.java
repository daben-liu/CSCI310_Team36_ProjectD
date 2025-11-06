package com.example.csci310_team36_projectd.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.csci310_team36_projectd.R;
import com.example.csci310_team36_projectd.data.TimeReminder;

import java.util.Calendar;

public class TimeReminderFragment extends Fragment {
    private DatePicker datePicker;
    private TimePicker timePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_reminder, container, false);
        datePicker = view.findViewById(R.id.datePicker);
        timePicker = view.findViewById(R.id.timePicker);
        return view;
    }

    public void saveReminder() {
        Context context = getContext();
        if (context == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                timePicker.getHour(),
                timePicker.getMinute());

        TimeReminder reminder = new TimeReminder(context, calendar, calendar, 1); // noteId=1 (for example)
        reminder.setReminder();

        Toast.makeText(context, "Time reminder set!", Toast.LENGTH_SHORT).show();
    }
}