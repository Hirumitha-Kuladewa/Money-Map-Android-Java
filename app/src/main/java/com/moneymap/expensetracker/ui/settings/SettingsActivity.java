package com.moneymap.expensetracker.ui.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import com.moneymap.expensetracker.R;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_EDIT_PROFILE = 100;

    private static final String DATABASE_NAME = "user_db";
    private static final String TABLE_NAME = "user_profile";
    private static final String COLUMN_PROFILE_PICTURE = "profile_picture";

    private ImageView profilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profilePicture = findViewById(R.id.profile_picture);

        findViewById(R.id.text_edit_profile).setOnClickListener(this::onTextClicked);
        findViewById(R.id.text_app_lock).setOnClickListener(this::onTextClicked);
        findViewById(R.id.text_about_app).setOnClickListener(this::onTextClicked);
        findViewById(R.id.text_contact_us).setOnClickListener(this::onTextClicked);

        updateProfilePicture();
    }

    private void onTextClicked(View view) {
        Intent intent;
        int id = view.getId();

        if (id == R.id.text_edit_profile) {
            intent = new Intent(this, EditProfileActivity.class);
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
        } else if (id == R.id.text_app_lock) {
            intent = new Intent(this, AppLockActivity.class);
            startActivity(intent);
        } else if (id == R.id.text_about_app) {
            intent = new Intent(this, AboutAppActivity.class);
            startActivity(intent);
        } else if (id == R.id.text_contact_us) {
            intent = new Intent(this, ContactUsActivity.class);
            startActivity(intent);
        } else {
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_PROFILE && resultCode == RESULT_OK) {
            boolean profilePictureUpdated = data != null && data.getBooleanExtra("profile_picture_updated", false);
            if (profilePictureUpdated) {
                updateProfilePicture();
            }
        }
    }

    private void updateProfilePicture() {
        SQLiteDatabase database = null;
        Cursor cursor = null;
        try {
            database = openOrCreateDatabase(DATABASE_NAME, MODE_PRIVATE, null);
            cursor = database.rawQuery("SELECT " + COLUMN_PROFILE_PICTURE + " FROM " + TABLE_NAME + " LIMIT 1", null);

            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String profilePicturePath = cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_PICTURE));
                if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                    Uri profilePictureUri = Uri.parse(profilePicturePath);
                    Glide.with(this)
                            .load(profilePictureUri)
                            .transform(new CircleCrop())
                            .into(profilePicture);
                } else {
                    profilePicture.setImageResource(R.drawable.profile_placeholder);
                }
            } else {
                profilePicture.setImageResource(R.drawable.profile_placeholder);
            }
        } catch (Exception e) {
            Log.e("SettingsActivity", "Error! updating profile picture", e);
            profilePicture.setImageResource(R.drawable.profile_placeholder);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null) {
                database.close();
            }
        }
    }
}