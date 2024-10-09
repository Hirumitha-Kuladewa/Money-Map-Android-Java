package com.moneymap.expensetracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AppLockDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "app_lock.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "app_lock";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LOCK_TYPE = "lock_type";
    public static final String COLUMN_PIN = "pin";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LOCK_TYPE + " TEXT, " +
                    COLUMN_PIN + " TEXT)";

    public AppLockDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public void clearDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }
}