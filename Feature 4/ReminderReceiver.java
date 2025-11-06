package com.example.csci310_team36_projectd.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.csci310_team36_projectd.data.NoteManager;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int noteId = intent.getIntExtra("note_id", -1);

        // Mark the note as “Relevant” (appears in the Relevant Notes section)
        NoteManager manager = NoteManager.getInstance(context);
        manager.addRelevant(noteId);

        // Show a notification/toast
        Toast.makeText(context, "Reminder for Note " + noteId + " triggered!", Toast.LENGTH_LONG).show();
    }
}