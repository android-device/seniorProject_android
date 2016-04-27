package com.mooo.samcat.temperaturemonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by rodrigo on 3/25/16.
 */
public class SensorReaderDbHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SavedSensorsContract.SensorEntry.TABLE_NAME + " (" +
                    SavedSensorsContract.SensorEntry._ID + " INTEGER PRIMARY KEY," +
                    SavedSensorsContract.SensorEntry.SENSOR_ID + TEXT_TYPE + COMMA_SEP +
                    SavedSensorsContract.SensorEntry.SENSOR_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    SavedSensorsContract.SensorEntry.SENSOR_HUMANREADABLE + TEXT_TYPE +
                    " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SavedSensorsContract.SensorEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Sensors.db";

    public SensorReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
