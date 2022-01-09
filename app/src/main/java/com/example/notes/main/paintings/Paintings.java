package com.example.notes.main.paintings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.example.notes.R;
import com.example.notes.main.groups.GroupModel;
import com.example.notes.main.groups.Note;
import com.example.notes.main.groups.NoteType;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.OutputStream;

import yuku.ambilwarna.AmbilWarnaDialog;
import com.google.android.material.slider.RangeSlider;

public class Paintings extends AppCompatActivity {

    private PaintView paintView;
    private int currentColor;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private Uri imageUri;
    private RangeSlider rangeSlider;
    private String groupName;

    ActivityResultLauncher<Intent> activityResultLauncherForUpload = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    imageUri = null;
                    imageUri = data.getData();

                    uploadImage();

                }
            }
        });

    ActivityResultLauncher<Intent> activityResultLauncherForDelete = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {

            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    imageUri = null;
                    imageUri = data.getData();

                    deleteImage();

                }
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paintings);
        initPaint();
        extractExtras();
        initFirebase();
        initRangeSlider();
    }

    private void initFirebase() {
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void initPaint() {
        paintView = findViewById(R.id.painView);
        rangeSlider = (RangeSlider) findViewById(R.id.rangebar);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);
    }

    private void initRangeSlider() {
        rangeSlider.setValueFrom(0.0f);
        rangeSlider.setValueTo(100.0f);

        rangeSlider.addOnChangeListener((slider, value, fromUser) -> paintView.setStrokeWidth((int) value));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.paintings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.normal:
                paintView.normal();
                return true;
            case R.id.emboss:
                paintView.emboss();
                return true;
            case R.id.blur:
                paintView.blur();
                return true;
            case R.id.clear:
                paintView.clear();
                return true;
            case R.id.colors:
                openDialog(false);
                return true;
            case R.id.stroke:
                setStrokeWidth();
                return true;
            case R.id.save:
                save();
                return true;
            case R.id.upload:
                openImageForUpload();
                return true;
            case R.id.delete:
                openImageForDelete();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openImageForUpload() {

        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");

        activityResultLauncherForUpload.launch(intent);
    }

    public void setStrokeWidth() {
        if (rangeSlider.getVisibility() == View.VISIBLE)
            rangeSlider.setVisibility(View.GONE);
        else
            rangeSlider.setVisibility(View.VISIBLE);
    }

    public void openImageForDelete() {

        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");

        activityResultLauncherForDelete.launch(intent);

    }

    public String getFileName(Uri uri) {
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

    private void deleteImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting");
        pd.show();
        System.out.println(DocumentFile.fromSingleUri(this, imageUri).getName());

        if(imageUri != null) {
            StorageReference filePath = storageReference.child(firebaseUser.getUid()).child(getFileName(imageUri));

            filePath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    pd.dismiss();
                    showToast("Image delete successfully");
                }
            });
        }
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Uploading");
        pd.show();

        if(imageUri != null) {
            final StorageReference fileRef = storageReference.child(firebaseUser.getUid()).child(getFileName(imageUri));

            fileRef.putFile(imageUri).addOnCompleteListener(task -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String url = uri.toString();

                Log.d("DownloadUrl", url);

                databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                GroupModel model = snapshot.getValue(GroupModel.class);
                                Note note = new Note(NoteType.PAINT, url);
                                assert model != null;
                                model.addNote(note);
                                databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).setValue(model);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }
                );

                pd.dismiss();
                showToast("Image upload successfull");
            }));
        }
    }

    private void openDialog(boolean supportAlpha) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, currentColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                showToast("Canceled!");
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                currentColor = color;
                paintView.setColors(color);
            }
        });

        dialog.show();
    }


    public void save() {

        Bitmap bmp = paintView.save();
        OutputStream imageOutStream = null;
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".png");
        cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
        try {
            // open the output stream with the above uri
            imageOutStream = getContentResolver().openOutputStream(uri);

            // this method writes the files in storage
            bmp.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);

            // close the output stream after use
            imageOutStream.close();

            showToast("Image saved!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void extractExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString("groupName");
        }
    }

//    private void downloadImage() {
//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setMessage("Downloading");
//        pd.show();
//
//        if(imageUri != null) {
//            StorageReference filePath = storageReference.child("uploads").child(getFileName(imageUri));
//
//            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                    pd.dismiss();
//
//                    Uri downloadUri = taskSnapshot.getUploadSessionUri();
//
//                    Picasso.get().load(downloadUri).into();
//
//                    showToast("Uploading finished ...");
//                }
//            });
//        }
//    }
}
