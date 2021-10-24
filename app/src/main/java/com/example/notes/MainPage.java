package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainPage extends AppCompatActivity {

    private Button settingsB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        initVariables();
        initListeners();
    }

    private void initVariables(){
        settingsB = (Button) findViewById(R.id.settings);
    }

    private void initListeners(){
        settingsB.setOnClickListener(
                v -> {
                    Intent intent = new Intent(MainPage.this, Settings.class);
                    startActivity(intent);
                }
        );
    }

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
        finish();
    }
}