package com.alexwan.csci310_team36_projectd.data.model;

public class PhotoElement extends NoteElement {
    private String filePath;

    public PhotoElement(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String getType() {
        return "photo";
    }
}
