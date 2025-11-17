package com.alexwan.csci310team36projectd.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alexwan.csci310team36projectd.R;
import com.alexwan.csci310team36projectd.data.AppDatabase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeoReminderFragment extends Fragment {
    private Button setCurrentLocationBtn;
    private TextView locationDisplay;
    private FusedLocationProviderClient fusedLocationClient;
    private android.location.Location lastLocation;
    private String locationName;

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
        locationDisplay = view.findViewById(R.id.location_display);

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
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Toast.makeText(getContext(), "Fetching current location...", Toast.LENGTH_SHORT).show();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        this.lastLocation = location;
                        Toast.makeText(getContext(), "Location found!", Toast.LENGTH_SHORT).show();

                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                            String foundLocationName = null;

                            if (Geocoder.isPresent()) {
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses != null && !addresses.isEmpty()) {
                                        Address address = addresses.get(0);
                                        if (address.getLocality() != null) {
                                            foundLocationName = address.getLocality();
                                        } else if (address.getSubAdminArea() != null) {
                                            foundLocationName = address.getSubAdminArea();
                                        } else if (address.getAdminArea() != null) {
                                            foundLocationName = address.getAdminArea();
                                        }
                                    }
                                } catch (IOException e) {
                                    // Geocoder failed, likely due to no internet.
                                }
                            }

                            if (foundLocationName == null) {
                                foundLocationName = String.format(Locale.getDefault(), "%.2f, %.2f", location.getLatitude(), location.getLongitude());
                                requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Could not find city name. Using coordinates.", Toast.LENGTH_LONG).show());
                            }

                            this.locationName = foundLocationName;
                            requireActivity().runOnUiThread(() -> {
                                locationDisplay.setText("Selected Location: " + this.locationName);
                                locationDisplay.setVisibility(View.VISIBLE);
                                setCurrentLocationBtn.setText("Location Set!");
                            });
                        });
                    } else {
                        Toast.makeText(getContext(), "Failed to get current location.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(requireActivity(), e -> {
                    Toast.makeText(getContext(), "Failed to get current location: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
        bundle.putString("locationName", this.locationName);
        return bundle;
    }
}
