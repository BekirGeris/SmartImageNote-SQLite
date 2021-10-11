package com.begers.smartnote;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.begers.smartnote.databinding.ActivityAddBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class AddActivity extends AppCompatActivity {

    private ActivityAddBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;  //galeriye gitmek için kullanılır
    ActivityResultLauncher<String> permissionLauncher;  //izin almak için kullanılır.
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database = this.openOrCreateDatabase("Notes", MODE_PRIVATE, null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if (info.equals("new")){
            //yeni bir şey eklenmek isteniyor.
            binding.textName.setText("");
            binding.textDate.setText("");
            binding.textDesc.setText("");
            binding.button.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.setsrc);
        }else {
            //var olanı görmek istiyor.
            int noteId = intent.getIntExtra("noteId", 0);
            binding.button.setVisibility(View.INVISIBLE);

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM notes WHERE id = ?", new String[] {String.valueOf(noteId)});
                int noteNameIx = cursor.getColumnIndex("note_Name");
                int descriptionIx = cursor.getColumnIndex("description");
                int dateIx = cursor.getColumnIndex("date");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.textName.setText(cursor.getString(noteNameIx));
                    binding.textDesc.setText(cursor.getString(descriptionIx));
                    binding.textDate.setText(cursor.getString(dateIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public  void  save(View view){

        String name = binding.textName.getText().toString();
        String noteDescription = binding.textDesc.getText().toString();
        String date = binding.textDate.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage, 300);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS notes(id INTEGER PRIMARY KEY, note_Name VARCHAR, description VARCHAR, date VARCHAR, image BLOB)");

            String sqlString = "INSERT INTO notes (note_Name, description, date, image) VALUES (?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, name);
            sqLiteStatement.bindString(2, noteDescription);
            sqLiteStatement.bindString(3, date);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(AddActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public Bitmap makeSmallerImage(Bitmap image, int maxSize){
        float width = image.getWidth();
        float height = image.getHeight();

        float bitmapRetio = width / height;
        if (bitmapRetio > 1){
            //yatay görsel
            width = maxSize;
            height = width / bitmapRetio;
        }else {
            //dikey görsel
            height = maxSize;
            width = height * bitmapRetio;
        }
        return  image.createScaledBitmap(image, (int) width, (int) height, true);
    }

    public void selectImage(View view){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){ //izin verilmedi izinin istenilme sebebi gösterilsin mi
                //izin ikinci kez verilmemiş
                Snackbar.make(view, "Galeriye gitmek için izniniz gerekiyor.", Snackbar.LENGTH_INDEFINITE).setAction("İzin ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else {
                //izin verilmemiş
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }else{
            //izin verilmiş
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }


    }

    private void registerLauncher(){  //tanımlamalar yapılacak

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null){
                        Uri imageData = intentFromResult.getData(); //kullanıcının seçtiği resmin kaynağını verir.
                        //binding.imageView.setImageURI(imageData);

                        try{
                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                            }else {
                                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageData);
                            }
                            binding.imageView.setImageBitmap(selectedImage);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                    //izin verildi
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }else{
                    //izin verilmedi
                    Toast.makeText(AddActivity.this, "İzin verilmedi", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}