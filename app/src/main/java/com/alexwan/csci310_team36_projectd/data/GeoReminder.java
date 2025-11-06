package com.alexwan.csci310_team36_projectd.data;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.alexwan.csci310_team36_projectd.services.GeofenceBroadcastReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

public class GeoReminder extends Reminder {
    private final Context context;
    private final Location location;
    private final int radius;
    private final int noteId;
    private final GeofencingClient geofencingClient;

    public GeoReminder(Context context, Location location, int radius, int noteId) {
        this.context = context;
        this.location = location;
        this.radius = radius;
        this.noteId = noteId;
        this.geofencingClient = LocationServices.getGeofencingClient(context);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void setReminder() {
        Geofence geofence = new Geofence.Builder()
                .setRequestId(String.valueOf(noteId))
                .setCircularRegion(location.getLatitude(), location.getLongitude(), radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest request = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);
        intent.putExtra("note_id", noteId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                noteId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return; // permission must be granted before use
        }

        geofencingClient.addGeofences(request, pendingIntent);
        activate();
    }

    @Override
    public void cancelReminder() {
        geofencingClient.removeGeofences(java.util.Collections.singletonList(String.valueOf(noteId)));
        deactivate();
    }

    public boolean inLocation(float lat, float lon) {
        float dx = lat - (float) location.getLatitude();
        float dy = lon - (float) location.getLongitude();
        return (dx * dx + dy * dy) <= radius * radius;
    }
}