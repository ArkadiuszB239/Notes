package com.example.notes.main;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.R;
import com.example.notes.main.account.Settings;
import com.example.notes.main.groups.GroupModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MainPage extends AppCompatActivity {

    private TableLayout tableLayout;
    private ImageView ivAddGroup;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        initVariables();
        initListeners();

        loadNotesGroups();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.user_management, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.settings:
                Intent intent = new Intent(MainPage.this, Settings.class);
                startActivity(intent);
                return true;
            case R.id.logout:
                this.logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initVariables(){
        tableLayout = findViewById(R.id.viewTableGroups);
        ivAddGroup = findViewById(R.id.addMark);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
    }

    private void initListeners(){
        ivAddGroup.setOnClickListener(v -> addGroupDialog());
    }

    private void loadNotesGroups() {
        databaseReference.child(user.getUid()).child("groups").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, GroupModel> groups = (Map<String, GroupModel>) snapshot.getValue();
                        if(groups != null) {
                            for(String groupName: groups.keySet()) {
                                TableRow row = (TableRow) LayoutInflater.from(MainPage.this).inflate(R.layout.table_row, null);
                                Button b = (Button)row.findViewById(R.id.rowButton);
                                b.setText(groupName);
                                b.setOnClickListener(v -> startNoteGroupActivity(groupName));
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

    private void startNoteGroupActivity(String groupName) {
        Intent intent = new Intent(MainPage.this, NoteGroupActivity.class);
        intent.putExtra("groupName", groupName);
        startActivity(intent);
    }

    private void addGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainPage.this);
        builder.setCancelable(true);
        builder.setTitle("Dodaj grupę!");
        builder.setMessage("Podaj nazwę:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
        builder.setView(input);
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    if (!input.getText().toString().isEmpty()){
                        addGroupToDB(input.getText().toString());
                        TableRow row = (TableRow) LayoutInflater.from(MainPage.this).inflate(R.layout.table_row, null);
                        Button b = (Button)row.findViewById(R.id.rowButton);
                        b.setText(input.getText().toString());
                        b.setOnClickListener(v -> startNoteGroupActivity(input.getText().toString()));
                        tableLayout.addView(row);
                        tableLayout.requestLayout();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(), "Błędna nazwa grupy!", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addGroupToDB(String groupName){
        GroupModel model = new GroupModel(groupName);
        assert user != null;
        databaseReference.child(user.getUid()).child("groups").child(model.getName()).setValue(model);
    }

    public void logout(){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}