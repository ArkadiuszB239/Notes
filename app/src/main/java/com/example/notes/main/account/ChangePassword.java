package com.example.notes.main.account;

import android.content.Intent;
import android.os.Bundle;

import com.example.notes.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class ChangePassword extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText password;
    private EditText repeatedPassword;
    private EditText oldPasswordE;
    private Button changeButton;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initVariables();
        initListeners();
    }

    private void initVariables() {
        firebaseAuth = FirebaseAuth.getInstance();
        password = (EditText) findViewById(R.id.newPassword);
        repeatedPassword = (EditText) findViewById(R.id.newPasswordRepeat);
        changeButton = (Button) findViewById(R.id.changePassButton);
        backButton = (Button) findViewById(R.id.changeBackButton);
        oldPasswordE = (EditText) findViewById(R.id.oldPassword);
    }

    private void initListeners() {
        backButton.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                ChangePassword.this,
                                Settings.class
                        )
                )
        );

        changeButton.setOnClickListener(
                v -> {
                    if (validation(password) && validation(repeatedPassword) && validation(oldPasswordE)) {
                        changePassword();
                    } else {
                        Toast.makeText(ChangePassword.this, "Wypełnij wymagane pola", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void changePassword() {
        String oldPassword = oldPasswordE.getText().toString().trim();
        String newPassword = password.getText().toString().trim();
        String newPasswordRepeated = repeatedPassword.getText().toString().trim();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        assert user != null;
        AuthCredential authCredential = EmailAuthProvider.getCredential(Objects.requireNonNull(user.getEmail()), oldPassword);

        if (newPassword.equals(newPasswordRepeated)) {
            user.reauthenticate(authCredential).addOnCompleteListener(
                    task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword).addOnCompleteListener(
                                    task1 -> {
                                        if (task1.isSuccessful()) {
                                            Toast.makeText(getApplicationContext(), "Hasło zmienione!", Toast.LENGTH_SHORT).show();
                                            goBack();
                                        } else
                                            Toast.makeText(getApplicationContext(), "Nowe hasło jest zbyt słabe!", Toast.LENGTH_SHORT).show();
                                    }
                            );
                        } else {
                            Toast.makeText(getApplicationContext(), "Reauthentication failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } else {
            Toast.makeText(getApplicationContext(), "Hasła różnią się!", Toast.LENGTH_SHORT).show();
        }
    }

    private void goBack() {
        Intent intent = new Intent(ChangePassword.this, Settings.class);
        startActivity(intent);
    }

    private boolean validation(EditText data) {
        return !data.getText().toString().isEmpty();
    }
}