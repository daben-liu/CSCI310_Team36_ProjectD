package com.example.csci310_team36_projectd.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;

import com.example.csci310_team36_projectd.services.ReminderReceiver;

public class TimeReminder extends Reminder {
    private Calendar alarmStart;
    private Calendar alarmEnd;
    private Context context;
    private int noteId; // associate reminder with a note

    public TimeReminder(Context context, Calendar start, Calendar end, int noteId) {
        super();
        this.context = context;
        this.alarmStart = start;
        this.alarmEnd = end;
        this.noteId = noteId;
    }

    @Override
    public void setReminder() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("note_id", noteId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                noteId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmStart.getTimeInMillis(), pendingIntent);
        }

        activate();
    }

    @Override
    public void cancelReminder() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                noteId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }

        deactivate();
    }

    public void setAlarm(Calendar newStart) {
        this.alarmStart = newStart;
        setReminder();
    }

    public boolean checkTime() {
        long now = System.currentTimeMillis();
        return now >= alarmStart.getTimeInMillis() && now <= alarmEnd.getTimeInMillis();
    }
}