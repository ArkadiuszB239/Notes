package com.example.notes.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;


import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.R;


public class NoteGroupActivity extends AppCompatActivity {

    private String groupName;
    private TextView groupTextLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_group);

        extractExtras();
        groupTextLabel = (TextView) findViewById(R.id.groupNameLabel);
    }

    private void extractExtras() {
        Bundle extras = getIntent().getExtras();
        if(extras!= null) {
            groupName = extras.getString("groupName");
        }
    }


    private void initVariables() {
    }

}