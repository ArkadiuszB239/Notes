package com.example.notes.main;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.R;
import com.example.notes.main.file_sharing.FileSharingActivity;
import com.example.notes.main.groups.GroupModel;
import com.example.notes.main.links.LinksActivity;
import com.example.notes.main.textnotes.TextNotesActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class NoteGroupActivity extends AppCompatActivity {

    private String groupName;
    private TextView groupTextLabel;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_group);

        extractExtras();
        initVariables();
        groupTextLabel.setText(groupName);
        initListeners();
    }

    private void extractExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString("groupName");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.group_meny, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteGroup:
                databaseReference.child(user.getUid()).child("groups").child(groupName).removeValue();

                Intent intent = new Intent(NoteGroupActivity.this, MainPage.class);
                startActivity(intent);
                return true;
            case R.id.shareGroup:
                openShareGroupDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initVariables() {
        groupTextLabel = (TextView) findViewById(R.id.groupNameLabel);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initListeners() {
        findViewById(R.id.backNotes).setOnClickListener(v -> startActivity(new Intent(this, MainPage.class)));
        findViewById(R.id.textNotes).setOnClickListener(v -> {
            Intent intent = new Intent(this, TextNotesActivity.class);
            intent.putExtra("groupName", groupName);
            startActivity(intent);
        });
        findViewById(R.id.linkNotes).setOnClickListener(v -> {
            Intent intent = new Intent(this, LinksActivity.class);
            intent.putExtra("groupName", groupName);
            startActivity(intent);
        });
        findViewById(R.id.pdfNotes).setOnClickListener(v -> {
            Intent intent = new Intent(this, FileSharingActivity.class);
            intent.putExtra("groupName", groupName);
            startActivity(intent);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void openShareGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NoteGroupActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Udostępnij grupę");
        builder.setMessage("Podaj email odbiorcy:");
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    if (validation(input)) {
                        shareGroup(input.getText().toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "Podaj adres e-mail!", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void shareGroup(String email) {
        List<Pair<String, String>> idMailPairs = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<?, ?> map = (Map<?, ?>) snapshot.getValue(Object.class);
                        for (Map.Entry<?, ?> key : map.entrySet()) {
                            String uuid = (String) key.getKey();
                            Map<?, ?> underMap = (Map<?, ?>) key.getValue();
                            String email = (String) underMap.get("email");
                            idMailPairs.add(new Pair<>(uuid, email));
                        }

                        String userUUID = null;
                        for (Pair<String, String> pair : idMailPairs) {
                            if (pair.second.equals(email)) {
                                userUUID = pair.first;
                            }
                        }

                        if (userUUID != null) {

                            String finalUserUUID = userUUID;
                            databaseReference.child(user.getUid()).child("groups").child(groupName)
                                    .addListenerForSingleValueEvent(
                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    GroupModel group = snapshot.getValue(GroupModel.class);
                                                    databaseReference.child(finalUserUUID).child("groups").child(group.getName()).setValue(group);
                                                    Toast.makeText(getApplicationContext(),
                                                            String.format("Udostepniono grupe %s uzytkownikowi %s",
                                                                    group.getName(),
                                                                    email),
                                                            Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            }
                                    );
                        } else {
                            Toast.makeText(getApplicationContext(), "Użytkownik nie istnieje!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private boolean validation(EditText data) {
        return !data.getText().toString().isEmpty();
    }
}