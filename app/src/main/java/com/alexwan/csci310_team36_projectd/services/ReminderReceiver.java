package com.alexwan.csci310_team36_projectd.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alexwan.csci310_team36_projectd.data.AppDatabase;
import com.alexwan.csci310_team36_projectd.data.Note;
import com.alexwan.csci310_team36_projectd.data.NoteDao;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long noteId = intent.getLongExtra("note_id", -1);
        if (noteId != -1) {
            AppDatabase database = AppDatabase.getDatabase(context.getApplicationContext());
            NoteDao noteDao = database.noteDao();

            AppDatabase.databaseWriteExecutor.execute(() -> {
                Note note = noteDao.getNoteByIdBlocking(noteId);
                if (note != null) {
                    note.isRelevant = true;
                    // Set the note to be relevant for 1 hour (3600 * 1000 milliseconds)
                    note.relevantUntil = System.currentTimeMillis() + 3600 * 1000;
                    noteDao.update(note);
                }
            });

            // Optional: Show a toast for debugging/confirmation
            // Toast.makeText(context, "Reminder for Note " + noteId + " triggered!", Toast.LENGTH_LONG).show();
        }
    }
}
