package com.example.notes.main.account;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.main.MainActivity;
import com.example.notes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private EditText loginE;
    private EditText passwdE;
    private Button regButton;
    private Button backButton;
    private FirebaseAuth firebaseAuth;
    private CheckBox registry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initComponenets();
        initListeners();
    }

    private void initComponenets() {
        loginE = (EditText) findViewById(R.id.username);
        passwdE = (EditText) findViewById(R.id.passwd);
        regButton = (Button) findViewById(R.id.regButton);
        backButton = (Button) findViewById(R.id.backButton);
        firebaseAuth = FirebaseAuth.getInstance();
        registry = (CheckBox) findViewById(R.id.checkReg);
    }

    private void initListeners() {
        backButton.setOnClickListener(
                v -> {
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                }
        );

        regButton.setOnClickListener(
                v -> {
                    if (validation(loginE) && validation(passwdE))
                        if(registry.isChecked())saveDataInDB();
                        else showToast("Check reg acceptation!");
                    else showToast("Wrong e-mail or passwd!");
                }
        );
    }

    private void saveDataInDB() {
        String email = loginE.getText().toString().trim();
        String passwd = passwdE.getText().toString().trim();

        firebaseAuth.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        showToast("Registration Succesfull!");
                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else showToast("Registration Failed!");
                }
        );
    }

    private boolean validation(EditText data) {
        return !data.getText().toString().isEmpty();
    }

    private void showToast(String msg) {
        Toast.makeText(RegistrationActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

}