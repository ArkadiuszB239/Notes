package com.example.notes.main.links;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
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
import com.example.notes.main.MainActivity;
import com.example.notes.main.MainPage;
import com.example.notes.main.NoteGroupActivity;
import com.example.notes.main.groups.GroupModel;
import com.example.notes.main.groups.Note;
import com.example.notes.main.groups.NoteType;
import com.example.notes.main.textnotes.TextNotesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class LinksActivity extends AppCompatActivity {

    private String groupName;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.links_activity_page);

        extractExtras();
        initVariables();
        initListeners();
        loadLinks();
    }

    private void initVariables() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        tableLayout = findViewById(R.id.viewTableLinks);
    }

    private void initListeners() {
        findViewById(R.id.backLinks).setOnClickListener(v -> startActivity(new Intent(LinksActivity.this, NoteGroupActivity.class).putExtra("groupName", groupName)));
        findViewById(R.id.addNoteMarkLinks).setOnClickListener(v -> addLink());
    }

    private void extractExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString("groupName");
        }
    }

    private void addLink() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LinksActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Dodaj link");
        builder.setMessage("Podaj link:");
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        builder.setView(input);
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    if (validation(input)) {
                        addLinkToDB(input.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "Puste pole!", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addLinkToDB(String link) {
        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        Note note = new Note(NoteType.LINK, link);
                        assert model != null;
                        model.addNote(note);
                        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).setValue(model)
                                .addOnCompleteListener(task -> reloadLinks());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void reloadLinks() {
        tableLayout.removeAllViews();
        loadLinks();
    }

    private void loadLinks() {
        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        assert model != null;
                        if(model.getNotes() != null) {
                            List<Pair<Integer, Note>> notesWithIndexes = model.getNotesWithIndexesForType(NoteType.LINK);
                            for (Pair<Integer, Note> notePair : notesWithIndexes) {
                                TableRow row = (TableRow) LayoutInflater.from(LinksActivity.this).inflate(R.layout.link_row, null);
                                TextView tv = row.findViewById(R.id.linkView);
                                tv.setText(notePair.second.getContent());
                                tv.setOnClickListener(v -> openLinkInBrowser(notePair.second.getContent()));
                                tv.setOnLongClickListener(v -> {
                                    openRemovingDialog(notePair.first);
                                    return false;
                                });
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

    private void openRemovingDialog(Integer index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LinksActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Potwierdź");
        builder.setMessage("Czy chcesz usunąć link?");
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    deleteLink(index);
                });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteLink(Integer index) {
        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        model.removeNoteFromList(index);
                        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).setValue(model)
                                .addOnCompleteListener(task -> reloadLinks());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private void openLinkInBrowser(String link){
        if (!link.startsWith("http://") && !link.startsWith("https://"))
            link = "http://" + link;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        startActivity(browserIntent);
    }

    private boolean validation(EditText data) {
        return !data.getText().toString().isEmpty();
    }
}
