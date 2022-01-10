package com.example.notes.main.gallery;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.notes.R;
import com.example.notes.main.groups.GroupModel;
import com.example.notes.main.groups.Note;
import com.example.notes.main.groups.NoteType;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Gallery extends AppCompatActivity implements ImageAdapter.OnItemClickListener {

    private String groupName;

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private List<Upload> mUploads;
    private FirebaseUser firebaseUser;


    //
//    FirebaseRecyclerAdapter<Member, ViewHolder> firebaseRecyclerAdapter;
//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        extractExtras();

        mUploads = new ArrayList<>();

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = firebaseDatabase.getReference();

        Query query = databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).child("notes").orderByChild("type").equalTo(NoteType.PAINT.name());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    mUploads.add(upload);
                }

                mAdapter = new ImageAdapter(Gallery.this, mUploads);

                mRecyclerView.setAdapter(mAdapter);

                mAdapter.setOnItemClickListener(Gallery.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Gallery.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(this, "Normal click at position: " + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDeleteClick(int position) {
        Upload selectedItem = mUploads.get(position);
        String selectedKey = selectedItem.getKey();

        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GroupModel model = snapshot.getValue(GroupModel.class);
                System.out.println("Numer obrazka id "+ Integer.parseInt(selectedKey));
                model.removeNoteFromList(Integer.parseInt(selectedKey)); //Nie usuwa nic <----------------------------------------------------------------------------- tutaj problem
                finish();
                startActivity(getIntent());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).child("notes").child(selectedKey).removeValue();


        //Toast.makeText(this, "Delete click at position: " + position, Toast.LENGTH_SHORT).show();
    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//
//
//        Query query = databaseReference.child(firebaseUser.getUid()).child("groups").child(groupName).child("notes").orderByChild("type").equalTo(NoteType.PAINT.name());
//
//        FirebaseRecyclerOptions<Member> options = new FirebaseRecyclerOptions.Builder<Member>()
//                .setQuery(query, Member.class)
//                .build();
//
//        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Member, ViewHolder>(options) {
//            @Override
//            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Member model) {
//                holder.setDetails(getApplication(), model.getType(), model.getContent());
//            }
//
//            @NonNull
//            @Override
//            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.image, parent, false);
//
//                ViewHolder viewHolder = new ViewHolder(rootView);
//
//                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//
//                        ImageView imageView = (ImageView) view.findViewById(R.id.rImgaeView);
//
//                        Drawable drawable = imageView.getDrawable();
//                        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
//
//                        Intent intent = new Intent(view.getContext(), SingleImage.class);
//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//                        byte[]bytes = stream.toByteArray();
//
//                        intent.putExtra("image", bytes);
//                        startActivity(intent);
//
//                    }
//
//                    @Override
//                    public void onItemLongClick(View view, int position) {
//                        // own implementation
//                    }
//                });
//
//                return viewHolder;
//            }
//        };
//
//        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
//
//        firebaseRecyclerAdapter.startListening();
//    }

    private void extractExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            groupName = extras.getString("groupName");
        }
    }
}