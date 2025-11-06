package com.example.csci310_team36_projectd.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.csci310_team36_projectd.data.NoteManager;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event.hasError()) return;

        List<Geofence> triggeringGeofences = event.getTriggeringGeofences();
        for (Geofence fence : triggeringGeofences) {
            int noteId = Integer.parseInt(fence.getRequestId());
            NoteManager manager = NoteManager.getInstance(context);

            switch (event.getGeofenceTransition()) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    manager.addRelevant(noteId);
                    Toast.makeText(context, "Entered location for note " + noteId, Toast.LENGTH_SHORT).show();
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    manager.removeRelevant(noteId);
                    Toast.makeText(context, "Left location for note " + noteId, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}