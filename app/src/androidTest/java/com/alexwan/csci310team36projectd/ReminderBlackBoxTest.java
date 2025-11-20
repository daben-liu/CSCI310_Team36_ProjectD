package com.alexwan.csci310team36projectd;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;

import android.content.Intent;
import android.view.View;

import org.hamcrest.Matcher;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.alexwan.csci310team36projectd.data.Note;
import com.alexwan.csci310team36projectd.data.AppDatabase;


@RunWith(AndroidJUnit4.class)
public class ReminderBlackBoxTest {

    private ActivityScenario<EditNoteActivity> scenario;

    @Rule
    public GrantPermissionRule locationPermission =
            GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    private ActivityScenario<EditNoteActivity> launchActivity() {
        Intent i = new Intent(
                ApplicationProvider.getApplicationContext(),
                MainActivity.class
        );
        return ActivityScenario.launch(i);
    }

    private void saveNoteAndReopen() {
         scenario = launchActivity();

        onView(withId(R.id.fab)).perform(click());

        // Type a title
        onView(withId(R.id.edit_note_title))
                .perform(typeText("Reminder Test Note"), closeSoftKeyboard());

        // Click save
        onView(withId(R.id.save_button)).perform(click());

        onView(new RecyclerViewMatcher(R.id.notes_recycler_view).atPosition(0)).perform(click());
    }

    private void deleteTestNote() {
        onView(withId(R.id.delete_button)).perform(click());
        onView(withText("Delete"))
                .inRoot(isDialog())   // optional but safer
                .perform(click());
    }

    // -------------------------------------------------------------
    // 1. Open reminder fragment
    // -------------------------------------------------------------
    @Test
    public void test_openReminderFragment() {
        saveNoteAndReopen();
        onView(withId(R.id.reminder_button)).perform(click());

        // Check the radio buttons exist
        onView(withId(R.id.reminderTypeGroup)).check(matches(isDisplayed()));
        onView(withId(R.id.timeReminderBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.geoReminderBtn)).check(matches(isDisplayed()));

        onView(withId(R.id.saveReminderBtn)).perform(click());
        deleteTestNote();
    }

    // -------------------------------------------------------------
    // 2. Switch between time and geo modes
    // -------------------------------------------------------------
    @Test
    public void test_switchReminderTabs() {
        saveNoteAndReopen();

        onView(withId(R.id.reminder_button)).perform(click());

        // Change to Geo reminder
        onView(withId(R.id.geoReminderBtn)).perform(click());

        // Should show a geo fragment view
        onView(withId(R.id.setCurrentLocationBtn)).check(matches(isDisplayed()));

        // Switch back to Time reminder
        onView(withId(R.id.timeReminderBtn)).perform(click());

        // Time fragment should now be visible
        onView(withId(R.id.datePicker)).check(matches(isDisplayed()));
        onView(withId(R.id.timePicker)).check(matches(isDisplayed()));

        onView(withId(R.id.saveReminderBtn)).perform(click());
        deleteTestNote();
    }

    // -------------------------------------------------------------
    // 3. Cancel closes reminder fragment
    // -------------------------------------------------------------
    @Test
    public void test_cancelReminderClosesFragment() {
        saveNoteAndReopen();

        onView(withId(R.id.reminder_button)).perform(click());
        onView(withId(R.id.cancelBtn)).perform(click());

        // After closing, the fragment views should not exist
        onView(withId(R.id.reminderTypeGroup)).check(doesNotExist());

        deleteTestNote();
    }

    // -------------------------------------------------------------
    // 4. Saved reminder appears in EditNoteActivity
    // -------------------------------------------------------------
    @Test
    public void test_savedReminderDisplayedInEditNoteActivity() {
        saveNoteAndReopen();

        onView(withId(R.id.reminder_button)).perform(click());
        onView(withId(R.id.timeReminderBtn)).perform(click());

        onView(withId(R.id.saveReminderBtn)).perform(click());

        onView(withId(R.id.reminder_display_layout)).check(matches(isDisplayed()));

        deleteTestNote();
    }
}