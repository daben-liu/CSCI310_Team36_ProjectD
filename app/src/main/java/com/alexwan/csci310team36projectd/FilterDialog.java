package com.alexwan.csci310team36projectd;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;

import java.util.*;

public class FilterDialog extends Dialog {

    public interface FilterListener {
        void onFilterApplied(String[] tags, Date startDate, Date endDate,
                             boolean hasPhoto, boolean hasVoice, boolean hasLocation);
    }

    private FilterListener listener;

    public FilterDialog(@NonNull Context context, FilterListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_filter);

        EditText tagsEdit = findViewById(R.id.tagsEditText);
        EditText startDateEdit = findViewById(R.id.startDateEditText);
        EditText endDateEdit = findViewById(R.id.endDateEditText);
        CheckBox photoBox = findViewById(R.id.photoCheckBox);
        CheckBox voiceBox = findViewById(R.id.voiceCheckBox);
        CheckBox locationBox = findViewById(R.id.locationCheckBox);
        Button apply = findViewById(R.id.applyFiltersButton);

        apply.setOnClickListener(v -> {
            String[] tags = tagsEdit.getText().toString().trim().split(",");
            Date startDate = null, endDate = null;
            try {
                if (!startDateEdit.getText().toString().isEmpty())
                    startDate = new java.text.SimpleDateFormat("yyyy-MM-dd")
                            .parse(startDateEdit.getText().toString());
                if (!endDateEdit.getText().toString().isEmpty())
                    endDate = new java.text.SimpleDateFormat("yyyy-MM-dd")
                            .parse(endDateEdit.getText().toString());
            } catch (Exception ignored) {}

            listener.onFilterApplied(tags, startDate, endDate,
                    photoBox.isChecked(), voiceBox.isChecked(), locationBox.isChecked());
            dismiss();
        });
    }
}
