package com.example.notes.main.file_sharing;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notes.R;
import com.example.notes.main.NoteGroupActivity;
import com.example.notes.main.groups.GroupModel;
import com.example.notes.main.groups.Note;
import com.example.notes.main.groups.NoteType;
import com.example.notes.main.links.LinksActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.List;

public class FileSharingActivity extends AppCompatActivity {

    private String groupName;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private ImageView uploadView;
    private ProgressDialog dialog;
    private StorageReference storageReference;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_sharing_activity);

        extractExtras();
        initVariables();
        initListeners();
        loadPdfs();
    }

    private void initVariables() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        uploadView = findViewById(R.id.uploadpdf);
        storageReference = FirebaseStorage.getInstance().getReference();
        tableLayout = findViewById(R.id.viewTablePdfs);
    }

    private void initListeners() {
        uploadView.setOnClickListener(v -> {
            Intent galery = new Intent();
            galery.setAction(Intent.ACTION_GET_CONTENT);
            galery.setType("application/pdf");
            startActivityForResult(galery, 1);
        });
        findViewById(R.id.backpdfs).setOnClickListener(v -> startActivity(new Intent(this, NoteGroupActivity.class).putExtra("groupName", groupName)));
    }

    private void extractExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString("groupName");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            // Here we are initialising the progress dialog box
            dialog = new ProgressDialog(this);
            dialog.setMessage("Uploading");

            // this will show message uploading
            // while pdf is uploading
            dialog.show();
            Uri imageuri = data.getData();

            final String fileName = getFileName(imageuri);
            Toast.makeText(FileSharingActivity.this, imageuri.toString(), Toast.LENGTH_SHORT).show();

            // Here we are uploading the pdf in firebase storage with the name of current time
            final StorageReference filepath = storageReference.child(firebaseUser.getUid()).child(groupName).child("pdfs").child(fileName);
            Toast.makeText(FileSharingActivity.this, filepath.getName(), Toast.LENGTH_SHORT).show();
            filepath.putFile(imageuri).continueWithTask((Continuation) task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filepath.getDownloadUrl();
            }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                if (task.isSuccessful()) {
                    dialog.dismiss();
                    Uri uri = task.getResult();
                    String myurl = uri.toString();
                    databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    GroupModel model = snapshot.getValue(GroupModel.class);
                                    Note note = new Note(NoteType.PDF, myurl);
                                    note.setFileName(fileName);
                                    assert model != null;
                                    model.addNote(note);
                                    databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).setValue(model)
                                            .addOnCompleteListener(task -> reloadPdfs());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            }
                    );
                    Toast.makeText(FileSharingActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    Toast.makeText(FileSharingActivity.this, "UploadedFailed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void reloadPdfs() {
        tableLayout.removeAllViews();
        loadPdfs();
    }

    private void loadPdfs() {
        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        assert model != null;
                        if(model.getNotes() != null) {
                            List<Pair<Integer, Note>> notesWithIndexes = model.getNotesWithIndexesForType(NoteType.PDF);
                            for (Pair<Integer, Note> notePair : notesWithIndexes) {
                                TableRow row = (TableRow) LayoutInflater.from(FileSharingActivity.this).inflate(R.layout.pdf_row, null);
                                TextView tv = row.findViewById(R.id.pdfView);
                                tv.setText(notePair.second.getFileName());
                                tv.setOnLongClickListener(v -> {
                                    openRemovingDialog(notePair.first, notePair.second.getFileName());
                                    return false;
                                });

                                tv.setOnClickListener(v -> {
                                    Intent intent = new Intent(v.getContext(), ViewpdfAcitvity.class);
                                    intent.putExtra("url", notePair.second.getContent());
                                    startActivity(intent);
                                });

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

    private void openRemovingDialog(Integer index, String fileName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Potwierdź");
        builder.setMessage("Czy chcesz usunąć plik " + fileName + "?");
        builder.setPositiveButton("Confirm",
                (dialog, which) -> {
                    deletePdf(index, fileName);
                });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deletePdf(Integer index, String fileName){
        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        GroupModel model = snapshot.getValue(GroupModel.class);
                        model.removeNoteFromList(index);
                        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).setValue(model)
                                .addOnCompleteListener(task -> {
                                    reloadPdfs();
                                    storageReference.child(firebaseUser.getUid()).child(groupName).child("pdfs").child(fileName).delete();
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                }
        );
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
