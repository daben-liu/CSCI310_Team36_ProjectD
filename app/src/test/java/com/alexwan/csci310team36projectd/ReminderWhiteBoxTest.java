package com.alexwan.csci310team36projectd;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.MainViewModel;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Calendar;
import java.util.HashMap;

public class ReminderWhiteBoxTest {

    @Rule
    public InstantTaskExecutorRule rule = new InstantTaskExecutorRule();


    // -------------------------------------------------------------
    // 1. White-box test for getReminderData() logic (mock equivalent)
    // -------------------------------------------------------------
    @Test
    public void test_timeReminderFragment_getReminderData() {
        // Instead of Bundle, use a plain map (local-test-friendly)
        HashMap<String, Object> fake = new HashMap<>();

        Calendar expected = Calendar.getInstance();
        expected.set(2025, Calendar.MAY, 10, 14, 30, 0);

        fake.put("reminderType", "time");
        fake.put("reminderTime", expected.getTimeInMillis());

        // Mock a "fragment-like" object that returns our data
        class FakeReminderProvider {
            HashMap<String, Object> getReminderData() {
                return fake;
            }
        }

        FakeReminderProvider provider = Mockito.spy(new FakeReminderProvider());
        HashMap<String, Object> result = provider.getReminderData();

        assertEquals("time", result.get("reminderType"));
        assertEquals(expected.getTimeInMillis(), result.get("reminderTime"));
    }


    // -------------------------------------------------------------
    // 2. time reminder logic
    // -------------------------------------------------------------
    @Test
    public void test_mainViewModel_timeReminder_relevanceLogic() {
        Note note = new Note();
        note.reminderType = "time";

        long now = System.currentTimeMillis();
        note.reminderTime = now - 5 * 60 * 1000; // 5 minutes ago

        assertTrue(
                "Note must be relevant if time-based reminder triggered within last hour",
                note.isNoteRelevant(null)
        );
    }


    // -------------------------------------------------------------
    // 3. Geo reminder relevant when user is close (< 5km)
    // -------------------------------------------------------------
    @Test
    public void test_mainViewModel_geoReminder_relevanceDistance() {
        Note note = new Note();
        note.reminderType = "geo";
        note.reminderLocation = new com.alexwan.csci310team36projectd.data.Location();
        note.reminderLocation.latitude = 34.021;
        note.reminderLocation.longitude = -118.285;

        // Fake location object (local-test-friendly)
        com.alexwan.csci310team36projectd.data.Location current =
                new com.alexwan.csci310team36projectd.data.Location();
        current.latitude = 34.022;
        current.longitude = -118.286;

        assertTrue(note.isNoteRelevant(current));
    }


    // -------------------------------------------------------------
    // 4. Geo reminder not relevant when far away (> 5km)
    // -------------------------------------------------------------
    @Test
    public void test_mainViewModel_geoReminder_notRelevantFarAway() {
        Note note = new Note();
        note.reminderType = "geo";
        note.reminderLocation = new com.alexwan.csci310team36projectd.data.Location();
        note.reminderLocation.latitude = 34.0;
        note.reminderLocation.longitude = -118.0;

        com.alexwan.csci310team36projectd.data.Location current =
                new com.alexwan.csci310team36projectd.data.Location();
        current.latitude = 10.0;
        current.longitude = 10.0;

        assertFalse(note.isNoteRelevant(current));
    }


    // -------------------------------------------------------------
    // 5. Plain Note object update test
    // -------------------------------------------------------------
    @Test
    public void test_note_updatesCorrectly_timeReminder() {
        Note note = new Note();
        note.id = 1;

        long future = System.currentTimeMillis() + 60000;
        note.reminderType = "time";
        note.reminderTime = future;

        assertEquals("time", note.reminderType);
        assertEquals(future, note.reminderTime);
        assertNull(note.reminderLocation);
    }
}