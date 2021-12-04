package com.example.notes.main.textnotes;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Pair;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.R;
import com.example.notes.main.NoteGroupActivity;
import com.example.notes.main.groups.GroupModel;
import com.example.notes.main.groups.Note;
import com.example.notes.main.groups.NoteType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class TextNotesActivity extends AppCompatActivity {

    private String groupName;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_notes);

        extractExtras();
        initVariables();
        initListeners();
        loadTextNotes();
    }

    private void initVariables() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        tableLayout = findViewById(R.id.viewTableTextNotes);
    }

    private void initListeners() {
        findViewById(R.id.addNoteMark).setOnClickListener(v -> addNote());
        findViewById(R.id.backTextNotesB).setOnClickListener(v ->
                startActivity(new Intent(TextNotesActivity.this, NoteGroupActivity.class)
                        .putExtra("groupName", groupName)));
    }

    private void extractExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString("groupName");
        }
    }

    private void loadTextNotes() {
        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        assert model != null;
                        if(model.getNotes() != null) {
                            List<Pair<Integer, Note>> notesWithIndexes = model.getNotesWithIndexesForType(NoteType.TEXT);
                            for (Pair<Integer, Note> notePair : notesWithIndexes) {
                                TableRow row = (TableRow) LayoutInflater.from(TextNotesActivity.this).inflate(R.layout.text_note, null);
                                TextView tv = row.findViewById(R.id.noteView);
                                tv.setText(notePair.second.getContent());
                                tv.setOnClickListener(v -> editNote(notePair.second.getContent(), notePair.first));
                                tableLayout.addView(row);
                            }
                            tableLayout.requestLayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void editNote(String content, Integer index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TextNotesActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Edytuj notatkę");
        builder.setMessage("Podaj treść notatki:");
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
        input.setText(content);
        builder.setView(input);
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    if (validation(input)) {
                        addNoteToDb(input.getText().toString(), index);
                    } else {
                        Toast.makeText(getApplicationContext(), "Puste pola notatki!", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.setNeutralButton(R.string.delete, (dialog, which) -> deleteNote(index));

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addNote() {
        AlertDialog.Builder builder = new AlertDialog.Builder(TextNotesActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Dodaj notatkę");
        builder.setMessage("Podaj treść notatki:");
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    if (validation(input)) {
                        addNoteToDb(input.getText().toString(), null);
                    } else {
                        Toast.makeText(getApplicationContext(), "Puste pola notatki!", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void reloadNotes() {
        tableLayout.removeAllViews();
        loadTextNotes();
    }

    private void addNoteToDb(String textNote, Integer index) {
        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        Note note = new Note(NoteType.TEXT, textNote);
                        assert model != null;
                        if (Objects.isNull(index)) {
                            model.addNote(note);
                        } else {
                            model.replaceNote(note, index);
                        }
                        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).setValue(model)
                                .addOnCompleteListener(task -> reloadNotes());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void deleteNote(Integer index) {
        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        model.removeNoteFromList(index);
                        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).setValue(model)
                                .addOnCompleteListener(task -> reloadNotes());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private boolean validation(EditText data) {
        return !data.getText().toString().isEmpty();
    }
}
