package com.alexwan.csci310_team36_projectd.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alexwan.csci310_team36_projectd.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class GeoReminderFragment extends Fragment {
    private Button setCurrentLocationBtn;
    private FusedLocationProviderClient fusedLocationClient;
    private android.location.Location lastLocation;
    private final int GEOFENCE_RADIUS_METERS = 50;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geo_reminder, container, false);
        view.setBackgroundResource(android.R.color.white);

        setCurrentLocationBtn = view.findViewById(R.id.setCurrentLocationBtn);
        setCurrentLocationBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });

        return view;
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; // Should not happen if we check before calling
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                this.lastLocation = location;
                setCurrentLocationBtn.setText("Location Set!");
                Toast.makeText(getContext(), "Current location captured", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Bundle getReminderData() {
        if (lastLocation == null) {
            Toast.makeText(getContext(), "Please capture the current location first", Toast.LENGTH_SHORT).show();
            return null;
        }

        Bundle bundle = new Bundle();
        bundle.putString("reminderType", "geo");
        bundle.putDouble("latitude", lastLocation.getLatitude());
        bundle.putDouble("longitude", lastLocation.getLongitude());
        bundle.putString("locationName", "Current Location");
        return bundle;
    }
}
