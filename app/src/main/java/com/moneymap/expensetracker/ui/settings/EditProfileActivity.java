package com.moneymap.expensetracker.ui.settings;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.github.dhaval2404.imagepicker.ImagePicker;

import com.moneymap.expensetracker.R;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profilePicture;
    private EditText Name, ContactNumber;
    private Uri profileImageUri;
    private SQLiteDatabase database;

    private static final String DATABASE_NAME = "user_db";
    private static final String TABLE_NAME = "user_profile";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CONTACT_NUMBER = "contact_number";
    private static final String COLUMN_PROFILE_PICTURE = "profile_picture";

    private long userId = -1;
    private static final int REQUEST_GALLERY_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePicture = findViewById(R.id.profile_picture);
        ImageButton btnAddProfilePicture = findViewById(R.id.btn_add_profile_picture);
        Name = findViewById(R.id.name);
        ContactNumber = findViewById(R.id.contact_number);
        TextView btnSave = findViewById(R.id.btn_save);

        database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
        createTableIfNotExists();

        btnAddProfilePicture.setOnClickListener(v -> checkGalleryPermissionAndShowOptions());
        btnSave.setOnClickListener(v -> saveUserProfile());

        loadUserProfile();
    }

    private void createTableIfNotExists() {
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_CONTACT_NUMBER + " TEXT, " +
                COLUMN_PROFILE_PICTURE + " TEXT)");
    }

    private void checkGalleryPermissionAndShowOptions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_GALLERY_PERMISSION);
        } else {
            showProfilePictureOptions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showProfilePictureOptions();
            } else {
                Toast.makeText(this, "Gallery access is required to change profile picture", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProfilePictureOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Profile Picture")
                .setItems(new CharSequence[]{"Change Profile Picture", "Remove Profile Picture"}, (dialog, which) -> {
                    if (which == 0) {
                        selectProfilePicture();
                    } else {
                        removeProfilePicture();
                    }
                })
                .show();
    }

    private void selectProfilePicture() {
        ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }

    private void removeProfilePicture() {
        profileImageUri = null;
        profilePicture.setImageResource(R.drawable.profile_placeholder);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            profileImageUri = data.getData();
            Glide.with(this)
                    .load(profileImageUri)
                    .transform(new CircleCrop())
                    .into(profilePicture);
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled !", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserProfile() {
        String name = Name.getText().toString();
        String contactNumber = ContactNumber.getText().toString();
        String profilePicturePath = profileImageUri != null ? profileImageUri.toString() : "";

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_CONTACT_NUMBER, contactNumber);
        values.put(COLUMN_PROFILE_PICTURE, profilePicturePath);

        if (userId == -1) {
            long newRowId = database.insert(TABLE_NAME, null, values);
            if (newRowId != -1) {
                userId = newRowId;
                Toast.makeText(this, "Profile Created", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("profile_picture_updated", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Error! Creating Profile", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = database.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(userId)});
            if (rowsAffected > 0) {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("profile_picture_updated", true);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Error! Updating Profile", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("Range")
    private void loadUserProfile() {
        Cursor cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = 1", null);
        if (cursor.moveToFirst()) {
            userId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
            String contactNumber = cursor.getString(cursor.getColumnIndex(COLUMN_CONTACT_NUMBER));
            String profilePicturePath = cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_PICTURE));

            Name.setText(name);
            ContactNumber.setText(contactNumber);

            if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                profileImageUri = Uri.parse(profilePicturePath);
                Glide.with(this)
                        .load(profileImageUri)
                        .transform(new CircleCrop())
                        .into(profilePicture);
            } else {
                profilePicture.setImageResource(R.drawable.profile_placeholder);
            }
        }
        cursor.close();
    }
}