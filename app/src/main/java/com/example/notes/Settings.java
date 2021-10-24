package com.example.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.fragment.NavHostFragment;

import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    private Button deleteUserB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        initVariables();
        initListeners();
    }

    private void initVariables() {
        deleteUserB = (Button) findViewById(R.id.deleteUser);
    }

    private void initListeners() {
        findViewById(R.id.changePass).setOnClickListener(
                v -> {
                    Intent intent = new Intent(Settings.this, ChangePassword.class);
                    startActivity(intent);
                }
        );

        findViewById(R.id.settingBackButton).setOnClickListener(
                v -> startActivity(
                        new Intent(Settings.this, MainPage.class)
                )
        );

        deleteUserB.setOnClickListener(
                v -> removeUser()
        );
    }

    private void removeUser(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
        builder.setCancelable(true);
        builder.setTitle("Czy chcesz skasować konto?");
        builder.setMessage("Podaj hasło:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    if (validation(input)){
                        deleteUser(input);
                    } else {
                        Toast.makeText(getApplicationContext(), "Podaj hasło!", Toast.LENGTH_SHORT).show();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteUser(EditText input){
        String password = input.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(authCredential).addOnCompleteListener(
                task -> {
                    if(task.isSuccessful()){
                        user.delete().addOnCompleteListener(
                                task1 -> {
                                    if(task1.isSuccessful()){
                                        startActivity(
                                                new Intent(Settings.this, MainActivity.class)
                                        );
                                    }
                                }
                        );
                    } else {
                        Toast.makeText(getApplicationContext(), "Błędne hasło!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private boolean validation(EditText data) {
        return !data.getText().toString().isEmpty();
    }

}