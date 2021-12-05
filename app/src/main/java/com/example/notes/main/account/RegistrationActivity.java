package com.example.notes.main.account;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.main.MainActivity;
import com.example.notes.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private EditText loginE;
    private EditText passwdE;
    private Button regButton;
    private Button backButton;
    private FirebaseAuth firebaseAuth;
    private CheckBox registry;
    private DatabaseReference databaseReference;

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
        databaseReference = FirebaseDatabase.getInstance().getReference();
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
                    if(checkDataEntered()) {
                        saveDataInDB();
                    }
                }
        );
    }

    private void saveDataInDB() {
        String email = loginE.getText().toString().trim();
        String passwd = passwdE.getText().toString().trim();

        firebaseAuth.createUserWithEmailAndPassword(email, passwd).addOnCompleteListener(
                task -> {
                    if (task.isSuccessful()) {
                        showToast("Rejestracja się powiodła!");
                        String user_id = firebaseAuth.getCurrentUser().getUid();
                        databaseReference.child(user_id).child("email").setValue(email);
                        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else showToast("Rejestracja się nie powiodła!");
                }
        );
    }
    private boolean checkDataEntered() {
        boolean temp = true;

        if(!validation(loginE)) {
            loginE.setError("Nie podano e-maila!");
            temp = false;
        }
        if(!validation(passwdE)) {
            passwdE.setError("Nie podano hasła!");
            temp = false;
        }

        if(!isEmail(loginE)) {
            showToast("Błędnie podany e-mail!");
            temp = false;
        }
        if(passwdE.getText().toString().length()<8 && !isValidPassword(passwdE.getText().toString())) {
            showToast("Błędnie podane hasło!");
            temp = false;
        }
        if(!registry.isChecked()) {
            showToast("Zaznacz akceptacje regulaminu");
            temp = false;
        }
        return temp;
    }

    private static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    private boolean validation(EditText data) {
        return !data.getText().toString().isEmpty();
    }

    private boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private void showToast(String msg) {
        Toast.makeText(RegistrationActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

}