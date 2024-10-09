package com.moneymap.expensetracker.login;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.moneymap.expensetracker.R;
import com.moneymap.expensetracker.database.AppLockDatabaseHelper;
import com.moneymap.expensetracker.database.DatabaseHelper;

public class PinEntryActivity extends AppCompatActivity {

    private final TextView[] pinCircles = new TextView[4];
    private final StringBuilder enteredPin = new StringBuilder();

    private AppLockDatabaseHelper appLockDbHelper;
    private DatabaseHelper additionalDbHelper;
    private SQLiteDatabase appLockDatabase;
    private SQLiteDatabase additionalDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_entry);

        pinCircles[0] = findViewById(R.id.circle1);
        pinCircles[1] = findViewById(R.id.circle2);
        pinCircles[2] = findViewById(R.id.circle3);
        pinCircles[3] = findViewById(R.id.circle4);

        appLockDbHelper = new AppLockDatabaseHelper(this);
        additionalDbHelper = new DatabaseHelper(this);
        appLockDatabase = appLockDbHelper.getReadableDatabase();
        additionalDatabase = additionalDbHelper.getReadableDatabase();

        setNumberClickListeners();
        setForgotPasswordListener();
        setCancelButtonListener();
    }

    private void setNumberClickListeners() {
        int[] buttonIds = {
                R.id.button1, R.id.button2, R.id.button3,
                R.id.button4, R.id.button5, R.id.button6,
                R.id.button7, R.id.button8, R.id.button9,
                R.id.button0
        };

        for (int buttonId : buttonIds) {
            Button button = findViewById(buttonId);
            button.setOnClickListener(v -> {
                if (enteredPin.length() < 4) {
                    enteredPin.append(button.getText().toString());
                    updateCircles();

                    if (enteredPin.length() == 4) {
                        verifyPin();
                    }
                }
            });
        }

        Button deleteButton = findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(v -> {
            if (enteredPin.length() > 0) {
                enteredPin.deleteCharAt(enteredPin.length() - 1);
                updateCircles();
            }
        });
    }

    private void setForgotPasswordListener() {
        TextView forgotPassword = findViewById(R.id.forgot_password);
        forgotPassword.setOnClickListener(v -> {
            clearAllDatabases();
            finish();
        });
    }

    private void setCancelButtonListener() {
        TextView cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(v -> finish());
    }

    private void clearAllDatabases() {
        appLockDbHelper.getWritableDatabase().delete(AppLockDatabaseHelper.TABLE_NAME, null, null);
        additionalDbHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_NAME, null, null);
    }

    private void verifyPin() {
        Cursor cursor = appLockDatabase.query(
                AppLockDatabaseHelper.TABLE_NAME,
                new String[]{AppLockDatabaseHelper.COLUMN_PIN},
                AppLockDatabaseHelper.COLUMN_LOCK_TYPE + " = ?",
                new String[]{"PIN"},
                null,
                null,
                null
        );

        boolean pinCorrect = false;
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String storedPin = cursor.getString(cursor.getColumnIndex(AppLockDatabaseHelper.COLUMN_PIN));
            pinCorrect = enteredPin.toString().equals(storedPin);
        }
        cursor.close();

        if (pinCorrect) {
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            vibrateAndShowError();
        }
    }

    private void vibrateAndShowError() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(300);
        }

        for (TextView circle : pinCircles) {
            circle.setBackgroundResource(R.drawable.circle_error);
        }

        new Handler().postDelayed(() -> {
            enteredPin.setLength(0);
            updateCircles();

            for (TextView circle : pinCircles) {
                circle.setBackgroundResource(R.drawable.circle_empty);
            }
        }, 1000);
    }

    private void updateCircles() {
        for (int i = 0; i < pinCircles.length; i++) {
            if (i < enteredPin.length()) {
                pinCircles[i].setBackgroundResource(R.drawable.circle_filled);
            } else {
                pinCircles[i].setBackgroundResource(R.drawable.circle_empty);
            }
        }
    }
}