package com.alexwan.csci310team36projectd.data.model;

public class PhotoElement extends NoteElement {
    private String filePath;

    public PhotoElement(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public Type getType() {
        return Type.PHOTO;
    }
}
