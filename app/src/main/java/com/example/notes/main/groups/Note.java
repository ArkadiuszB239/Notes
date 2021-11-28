package com.example.notes.main.groups;

public class Note {
    private NoteType type;
    private String content;

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
}
