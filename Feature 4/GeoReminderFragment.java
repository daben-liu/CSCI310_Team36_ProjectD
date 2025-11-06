package com.example.csci310_team36_projectd.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.csci310_team36_projectd.R;
import com.example.csci310_team36_projectd.data.GeoReminder;
import com.example.csci310_team36_projectd.data.Location;

public class GeoReminderFragment extends Fragment {
    private EditText nameInput, latInput, lonInput, radiusInput;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geo_reminder, container, false);
        nameInput = view.findViewById(R.id.locationNameInput);
        latInput = view.findViewById(R.id.latitudeInput);
        lonInput = view.findViewById(R.id.longitudeInput);
        radiusInput = view.findViewById(R.id.radiusInput);
        return view;
    }

    public void saveReminder() {
        Context context = getContext();
        if (context == null) return;

        String name = nameInput.getText().toString();
        String latStr = latInput.getText().toString();
        String lonStr = lonInput.getText().toString();
        String radiusStr = radiusInput.getText().toString();

        if (TextUtils.isEmpty(latStr) || TextUtils.isEmpty(lonStr) || TextUtils.isEmpty(radiusStr)) {
            Toast.makeText(context, "Please enter all location fields", Toast.LENGTH_SHORT).show();
            return;
        }

        float lat = Float.parseFloat(latStr);
        float lon = Float.parseFloat(lonStr);
        int radius = Integer.parseInt(radiusStr);

        Location location = new Location(lat, lon, name);
        GeoReminder reminder = new GeoReminder(context, location, radius, 1);
        reminder.setReminder();

        Toast.makeText(context, "Geo reminder set!", Toast.LENGTH_SHORT).show();
    }
}