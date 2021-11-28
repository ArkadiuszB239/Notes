package com.example.notes.main.groups;

import java.util.ArrayList;
import java.util.List;

public class GroupModel {
    private String name;
    private List<Note> notes;

    public GroupModel(String name) {
        this.name = name;
        this.notes = new ArrayList<>();
    }

    public void addNote(Note note) {
        this.notes.add(note);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
}
