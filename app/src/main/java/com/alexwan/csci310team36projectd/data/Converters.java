package com.alexwan.csci310team36projectd.data;

import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import androidx.core.text.HtmlCompat;
import androidx.room.TypeConverter;

import com.alexwan.csci310team36projectd.data.model.ChecklistElement;
import com.alexwan.csci310team36projectd.data.model.NoteElement;
import com.alexwan.csci310team36projectd.data.model.PhotoElement;
import com.alexwan.csci310team36projectd.data.model.RuntimeTypeAdapterFactory;
import com.alexwan.csci310team36projectd.data.model.TextElement;
import com.alexwan.csci310team36projectd.data.model.VoiceMemoElement;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
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
        // Before serialization, convert SpannableString to HTML for TextElements
        List<NoteElement> listToSerialize = new ArrayList<>();
        for (NoteElement element : list) {
            if (element instanceof TextElement) {
                TextElement textElement = (TextElement) element;
                // Convert SpannableString to HTML for persistence
                String htmlContent = HtmlCompat.toHtml(textElement.getContentAsSpannable(), HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);
                // Create a temporary TextElement with String content for serialization
                TextElement newTextElement = new TextElement(textElement.getId(), textElement.getTimestamp(), htmlContent);
                listToSerialize.add(newTextElement);
            } else {
                listToSerialize.add(element);
            }
        }

        Type listType = new TypeToken<List<NoteElement>>() {}.getType();
        return gson.toJson(listToSerialize, listType);
    }

    @TypeConverter
    public static List<NoteElement> fromNoteElementList(String value) {
        if (value == null) {
            return null;
        }
        Type listType = new TypeToken<List<NoteElement>>() {}.getType();
        List<NoteElement> deserializedList = gson.fromJson(value, listType);

        // After deserialization, convert HTML back to SpannableString for TextElements
        if (deserializedList != null) {
            for (int i = 0; i < deserializedList.size(); i++) {
                NoteElement element = deserializedList.get(i);
                if (element instanceof TextElement) {
                    TextElement textElement = (TextElement) element;
                    // Convert HTML back to SpannableString
                    Spanned spanned = HtmlCompat.fromHtml(textElement.getContent().toString(), HtmlCompat.FROM_HTML_MODE_LEGACY);
                    // Update the TextElement with the SpannableString content
                    textElement.setContent(new SpannableString(spanned));
                }
            }
        }
        return deserializedList;
    }
}
