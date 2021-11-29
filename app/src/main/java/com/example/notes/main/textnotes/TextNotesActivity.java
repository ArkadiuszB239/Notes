package com.example.notes.main.textnotes;

import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.widget.Button;
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
import com.example.notes.main.MainPage;
import com.example.notes.main.account.Settings;
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
import java.util.stream.Collectors;

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
        tableLayout = (TableLayout) findViewById(R.id.viewTableTextNotes);
    }

    private void initListeners() {
        findViewById(R.id.addNoteMark).setOnClickListener(v -> addNote());
    }

    private void extractExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString("groupName");
        }
    }

    private void loadTextNotes() {
        databaseReference.child(firebaseUser.getUid()).child(groupName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        assert model != null;
                        List<Note> textNotes = model.getNotes().stream().filter(n -> NoteType.TEXT.equals(n.getType())).collect(Collectors.toList());
                        for(Note note:textNotes) {
                            TableRow row = (TableRow) LayoutInflater.from(TextNotesActivity.this).inflate(R.layout.text_note, null);
                            TextView tv = (TextView) row.findViewById(R.id.noteView);
                            tv.setText(note.getContent());
                            //TODO listener for editing existing notes
                            //tv.setOnClickListener(v -> );
                            tableLayout.addView(row);
                        }
                        tableLayout.requestLayout();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
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
                    if (validation(input)){
                        addNoteToDb(input.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "Puste pola notatki!", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addNoteToDb(String textNote) {
        databaseReference.child(firebaseUser.getUid()).child(groupName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        Note note = new Note(NoteType.TEXT, textNote);
                        model.addNote(note);
                        databaseReference.child(firebaseUser.getUid()).child(groupName).setValue(model);
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
