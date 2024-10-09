package com.moneymap.expensetracker.ui.settings;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.moneymap.expensetracker.R;
import com.moneymap.expensetracker.database.AppLockDatabaseHelper;

public class AppLockActivity extends AppCompatActivity {

    private EditText Pin, ConfirmPin, RemovePin, CurrentPin, NewPin, ConfirmNewPin;
    private TextView btnSave, btnRemoveLock, btnSaveNewPin;
    private AppLockDatabaseHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_lock);

        dbHelper = new AppLockDatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        Pin = findViewById(R.id.pin);
        ConfirmPin = findViewById(R.id.confirm_pin);
        RemovePin = findViewById(R.id.remove_pin);
        CurrentPin = findViewById(R.id.enter_current_pin);
        NewPin = findViewById(R.id.edit_new_pin);
        ConfirmNewPin = findViewById(R.id.confirm_edit_pin);

        btnSave = findViewById(R.id.btn_save);
        btnRemoveLock = findViewById(R.id.btn_remove_lock);
        btnSaveNewPin = findViewById(R.id.btn_save_new_pin);

        btnSave.setOnClickListener(v -> saveLockType());
        btnRemoveLock.setOnClickListener(v -> removeAppLock());
        btnSaveNewPin.setOnClickListener(v -> changePin());
    }

    private void saveLockType() {
        String pin = Pin.getText().toString();
        String confirmPin = ConfirmPin.getText().toString();

        if (TextUtils.isEmpty(pin) || TextUtils.isEmpty(confirmPin)) {
            Toast.makeText(this, "Please enter and confirm your PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pin.equals(confirmPin)) {
            Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(AppLockDatabaseHelper.COLUMN_LOCK_TYPE, "PIN");
        values.put(AppLockDatabaseHelper.COLUMN_PIN, pin);

        database.delete(AppLockDatabaseHelper.TABLE_NAME, null, null);

        long newRowId = database.insert(AppLockDatabaseHelper.TABLE_NAME, null, values);
        if (newRowId != -1) {
            Toast.makeText(this, "PIN saved", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error saving PIN", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePin() {
        String currentPin = CurrentPin.getText().toString();
        String newPin = NewPin.getText().toString();
        String confirmNewPin = ConfirmNewPin.getText().toString();

        if (TextUtils.isEmpty(currentPin) || TextUtils.isEmpty(newPin) || TextUtils.isEmpty(confirmNewPin)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPin.equals(confirmNewPin)) {
            Toast.makeText(this, "New PINs do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = database.query(AppLockDatabaseHelper.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String savedPin = cursor.getString(cursor.getColumnIndex(AppLockDatabaseHelper.COLUMN_PIN));
            if (currentPin.equals(savedPin)) {
                ContentValues values = new ContentValues();
                values.put(AppLockDatabaseHelper.COLUMN_PIN, newPin);

                int rowsUpdated = database.update(AppLockDatabaseHelper.TABLE_NAME, values, null, null);
                if (rowsUpdated > 0) {
                    Toast.makeText(this, "PIN updated successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error updating PIN", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Incorrect current PIN", Toast.LENGTH_SHORT).show();
            }
        }
        cursor.close();
    }

    private void removeAppLock() {
        String currentPin = RemovePin.getText().toString();

        if (TextUtils.isEmpty(currentPin)) {
            Toast.makeText(this, "Please enter the current PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = database.query(AppLockDatabaseHelper.TABLE_NAME, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String savedPin = cursor.getString(cursor.getColumnIndex(AppLockDatabaseHelper.COLUMN_PIN));
            if (currentPin.equals(savedPin)) {
                database.delete(AppLockDatabaseHelper.TABLE_NAME, null, null);
                Toast.makeText(this, "App lock removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
            }
        }
        cursor.close();
    }
}