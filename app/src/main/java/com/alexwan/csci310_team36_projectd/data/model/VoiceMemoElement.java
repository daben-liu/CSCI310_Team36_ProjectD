package com.alexwan.csci310_team36_projectd.data.model;

public class VoiceMemoElement extends NoteElement {
    private String filePath;

    public VoiceMemoElement(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String getType() {
        return "voice_memo";
    }
}
