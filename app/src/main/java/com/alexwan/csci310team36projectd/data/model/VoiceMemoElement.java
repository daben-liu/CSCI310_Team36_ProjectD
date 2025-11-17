package com.alexwan.csci310team36projectd.data.model;

public class VoiceMemoElement extends NoteElement {
    private String filePath;

    public VoiceMemoElement(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public Type getType() {
        return Type.VOICE_MEMO;
    }
}
