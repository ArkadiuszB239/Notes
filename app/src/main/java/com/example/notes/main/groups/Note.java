package com.example.notes.main.groups;

import androidx.annotation.Nullable;

public class Note {
    private NoteType type;
    private String content;

    @Nullable
    private String fileName;

    public Note(){ }

    public Note(NoteType type, String content) {
        this.type = type;
        this.content = content;
    }

    public NoteType getType() {
        return type;
    }

    public void setType(NoteType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Nullable
    public String getFileName() {
        return fileName;
    }

    public void setFileName(@Nullable String fileName) {
        this.fileName = fileName;
    }
}
