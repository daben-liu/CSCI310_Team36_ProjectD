package com.alexwan.csci310_team36_projectd.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alexwan.csci310_team36_projectd.data.AppDatabase;
import com.alexwan.csci310_team36_projectd.data.Note;
import com.alexwan.csci310_team36_projectd.data.NoteDao;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null || event.hasError()) {
            return;
        }

        List<Geofence> triggeringGeofences = event.getTriggeringGeofences();
        if (triggeringGeofences == null) {
            return;
        }

        AppDatabase database = AppDatabase.getDatabase(context.getApplicationContext());
        NoteDao noteDao = database.noteDao();

        for (Geofence fence : triggeringGeofences) {
            long noteId = Long.parseLong(fence.getRequestId());

            AppDatabase.databaseWriteExecutor.execute(() -> {
                Note note = noteDao.getNoteByIdBlocking(noteId);
                if (note != null) {
                    switch (event.getGeofenceTransition()) {
                        case Geofence.GEOFENCE_TRANSITION_ENTER:
                            note.isRelevant = true;
                            // Set the note to be relevant for 1 hour
                            note.relevantUntil = System.currentTimeMillis() + 3600 * 1000;
                            noteDao.update(note);
                            break;
                        case Geofence.GEOFENCE_TRANSITION_EXIT:
                            note.isRelevant = false;
                            note.relevantUntil = 0;
                            noteDao.update(note);
                            break;
                    }
                }
            });
        }
    }
}
