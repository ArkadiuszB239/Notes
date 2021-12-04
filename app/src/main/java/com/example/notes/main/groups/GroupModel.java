package com.example.notes.main.groups;

import android.os.Build;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GroupModel {
    private String name;
    private List<Note> notes;

    public GroupModel() {
    }

    public GroupModel(String name) {
        this.name = name;
        this.notes = new ArrayList<>();
    }

    public void addNote(Note note) {
        if(this.notes == null) {
            this.notes = new ArrayList<>();
        }
        this.notes.add(note);
    }

    public void replaceNote(Note note, Integer index) {
        this.notes.set(index, note);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public List<Pair<Integer, Note>> getNotesWithIndexesForType(NoteType type) {
        return getNotes().stream()
                .filter(n -> type.equals(n.getType()))
                .map(note ->  new Pair<>(getNotes().indexOf(note), note))
                .collect(Collectors.toList());
    }

    public void removeNoteFromList(int index) {
        this.notes.remove(index);
    }
}
