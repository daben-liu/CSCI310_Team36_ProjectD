package com.alexwan.csci310team36projectd.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Note.class}, version = 4, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract NoteDao noteDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Correct migration from version 3 to 4
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // 1. Create the new table with the correct V4 schema.
            database.execSQL(
                "CREATE TABLE notes_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "title TEXT, " +
                "isPinned INTEGER NOT NULL, " +
                "dateCreated INTEGER, " +
                "lastEdited INTEGER, " +
                "elements TEXT, " +
                "tags TEXT, " +
                "reminderTime INTEGER NOT NULL, " +
                "reminderType TEXT, " +
                // Prefixed columns for the location tag (new feature)
                "tag_latitude REAL NOT NULL DEFAULT 0.0, " +
                "tag_longitude REAL NOT NULL DEFAULT 0.0, " +
                "tag_name TEXT, " +
                "tag_radius REAL NOT NULL DEFAULT 0.0, " +
                // Prefixed columns for the reminder location (migrated from old columns)
                "reminder_latitude REAL NOT NULL DEFAULT 0.0, " +
                "reminder_longitude REAL NOT NULL DEFAULT 0.0, " +
                "reminder_name TEXT, " +
                "reminder_radius REAL NOT NULL DEFAULT 0.0)"
            );

            // 2. Copy data from the old 'notes' table to 'notes_new'.
            // This version correctly reads from the old column names (e.g., reminderLatitude)
            // and inserts them into the new reminder_prefixed columns.
            database.execSQL(
                "INSERT INTO notes_new (id, title, isPinned, dateCreated, lastEdited, elements, tags, reminderTime, reminderType, " +
                "reminder_latitude, reminder_longitude, reminder_name) " +
                "SELECT id, title, isPinned, dateCreated, lastEdited, elements, tags, reminderTime, reminderType, " +
                "COALESCE(reminderLatitude, 0.0), COALESCE(reminderLongitude, 0.0), reminderLocationName " +
                "FROM notes"
            );

            // 3. Drop the old table.
            database.execSQL("DROP TABLE notes");

            // 4. Rename the new table to the original name.
            database.execSQL("ALTER TABLE notes_new RENAME TO notes");
        }
    };


    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "note_database")
                            .addMigrations(MIGRATION_3_4)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
