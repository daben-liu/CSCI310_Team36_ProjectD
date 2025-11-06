package com.alexwan.csci310team36projectd.data;

import androidx.room.TypeConverter;

import com.alexwan.csci310team36projectd.data.model.ChecklistElement;
import com.alexwan.csci310team36projectd.data.model.NoteElement;
import com.alexwan.csci310team36projectd.data.model.PhotoElement;
import com.alexwan.csci310team36projectd.data.model.RuntimeTypeAdapterFactory;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.alexwan.csci310team36projectd.data.model.VoiceMemoElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Converters {

    // This factory is the key. It tells Gson how to handle the different NoteElement types.
    private static final RuntimeTypeAdapterFactory<NoteElement> runtimeTypeAdapterFactory = RuntimeTypeAdapterFactory
            .of(NoteElement.class, "type") // Use "type" as the discriminating field
            .registerSubtype(TextElement.class, "text")
            .registerSubtype(PhotoElement.class, "photo")
            .registerSubtype(ChecklistElement.class, "checklist")
            .registerSubtype(VoiceMemoElement.class, "voice_memo");

    // We create a single, configured Gson instance to use for all conversions.
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(runtimeTypeAdapterFactory)
            .create();

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split(",")));
    }

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) {
            return null;
        }
        return String.join(",", list);
    }

    @TypeConverter
    public static Location toLocation(String locationString) {
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }
        String[] parts = locationString.split(",", 3);
        if (parts.length != 3) return null;
        try {
            double latitude = Double.parseDouble(parts[0]);
            double longitude = Double.parseDouble(parts[1]);
            String name = parts[2];
            return new Location(latitude, longitude, name);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @TypeConverter
    public static String fromLocation(Location location) {
        if (location == null) {
            return null;
        }
        return location.latitude + "," + location.longitude + "," + location.name;
    }

    @TypeConverter
    public static String toNoteElementList(List<NoteElement> list) {
        if (list == null) {
            return null;
        }
        Type listType = new TypeToken<List<NoteElement>>() {}.getType();
        return gson.toJson(list, listType);
    }

    @TypeConverter
    public static List<NoteElement> fromNoteElementList(String value) {
        if (value == null) {
            return null;
        }
        Type listType = new TypeToken<List<NoteElement>>() {}.getType();
        return gson.fromJson(value, listType);
    }
}
