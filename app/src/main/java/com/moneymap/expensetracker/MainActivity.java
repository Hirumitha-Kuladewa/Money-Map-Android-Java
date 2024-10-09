package com.moneymap.expensetracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.moneymap.expensetracker.database.AppLockDatabaseHelper;
import com.moneymap.expensetracker.databinding.ActivityMainBinding;
import com.moneymap.expensetracker.login.PinEntryActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PIN = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;

    private ActivityMainBinding binding;
    private AppLockDatabaseHelper dbHelper;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new AppLockDatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            proceedWithAppFlow();
        }
    }

    private void proceedWithAppFlow() {
        if (isPinRequired()) {
            Intent intent = new Intent(this, PinEntryActivity.class);
            startActivityForResult(intent, REQUEST_PIN);
        } else {
            setupUI();
        }
    }

    private boolean isPinRequired() {
        Cursor cursor = null;
        boolean pinRequired = false;
        try {
            cursor = database.query(
                    AppLockDatabaseHelper.TABLE_NAME,
                    new String[]{AppLockDatabaseHelper.COLUMN_PIN},
                    AppLockDatabaseHelper.COLUMN_LOCK_TYPE + " = ?",
                    new String[]{"PIN"},
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.moveToFirst()) {
                @SuppressLint("Range") String storedPin = cursor.getString(cursor.getColumnIndex(AppLockDatabaseHelper.COLUMN_PIN));
                pinRequired = !TextUtils.isEmpty(storedPin);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return pinRequired;
    }

    private void setupUI() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PIN) {
            if (resultCode == RESULT_OK) {
                setupUI();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                proceedWithAppFlow();
            } else {
                finish();
            }
        }
    }
}