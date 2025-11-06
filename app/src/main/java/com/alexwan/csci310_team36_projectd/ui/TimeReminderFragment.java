package com.alexwan.csci310_team36_projectd.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alexwan.csci310_team36_projectd.R;

import java.util.Calendar;

public class TimeReminderFragment extends Fragment {
    private DatePicker datePicker;
    private TimePicker timePicker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_reminder, container, false);
        datePicker = view.findViewById(R.id.datePicker);
        timePicker = view.findViewById(R.id.timePicker);
        return view;
    }

    public Bundle getReminderData() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(
                datePicker.getYear(),
                datePicker.getMonth(),
                datePicker.getDayOfMonth(),
                timePicker.getHour(),
                timePicker.getMinute(),
                0
        );

        Bundle bundle = new Bundle();
        bundle.putString("reminderType", "time");
        bundle.putLong("reminderTime", calendar.getTimeInMillis());
        return bundle;
    }
}
