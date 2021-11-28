package com.example.notes.main;

import android.content.Intent;
import android.os.Bundle;

import com.example.notes.R;
import com.example.notes.main.account.RegistrationActivity;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText loginE;
    private EditText passwdE;
    private Button logInB;
    private Button registrationB;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
        checkIsLogged();
        initListeners();
    }

    private void initComponents() {
        loginE = (EditText) findViewById(R.id.username);
        passwdE = (EditText) findViewById(R.id.passwd);
        logInB = (Button) findViewById(R.id.logButton);
        registrationB = (Button) findViewById(R.id.regButton);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void checkIsLogged(){
        if(firebaseAuth.getCurrentUser() != null){
            startActivity(new Intent(MainActivity.this, MainPage.class));
        }
    }

    private void initListeners() {
        registrationB.setOnClickListener(
                v -> {
                    Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                    startActivity(intent);
                }
        );

        logInB.setOnClickListener(
                v -> {
                    if(validation(loginE) && validation(passwdE)){
                        String userName = loginE.getText().toString().trim();
                        String passwd = passwdE.getText().toString().trim();
                        loginIn(userName, passwd);
                    }
                }
        );
    }

    private void loginIn(String username, String passwd){
        firebaseAuth.signInWithEmailAndPassword(username, passwd).addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()){
                        Toast.makeText(MainActivity.this, "Welcome " + username, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, MainPage.class));
                    } else Toast.makeText(MainActivity.this, "Wrong credentials!", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private boolean validation(EditText data) {
        return !data.getText().toString().isEmpty();
    }
}