//package com.example.notes.main.gallery;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Bundle;
//import android.widget.ImageView;
//
//import com.example.notes.R;
//
//public class SingleImage extends AppCompatActivity {
//
//    ImageView imageView;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_single_image);
//
////        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
////        getSupportActionBar().setDisplayShowHomeEnabled(true);
//
//        imageView.findViewById(R.id.rImageView2);
//
//        byte[] bytes = getIntent().getByteArrayExtra("image");
//
//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//
//        imageView.setImageBitmap(bitmap);
//
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//
//        onBackPressed();
//
//        return true;
//    }
//}