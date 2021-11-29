package com.example.notes.main;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.R;
import com.example.notes.main.textnotes.TextNotesActivity;


public class NoteGroupActivity extends AppCompatActivity {

    private String groupName;
    private TextView groupTextLabel;

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


    private void initVariables() {
        groupTextLabel = (TextView) findViewById(R.id.groupNameLabel);
    }

    private void initListeners() {
        findViewById(R.id.backNotes).setOnClickListener(v -> startActivity(new Intent(this, MainPage.class)));
        findViewById(R.id.textNotes).setOnClickListener(v -> {
            Intent intent = new Intent(this, TextNotesActivity.class);
            intent.putExtra("groupName", groupName);
            startActivity(intent);
        });
    }
}