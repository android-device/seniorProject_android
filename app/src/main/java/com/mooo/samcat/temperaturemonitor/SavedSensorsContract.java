package com.mooo.samcat.temperaturemonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by rodrigo on 3/25/16.
 */
public class SavedSensorsContract {
    public SavedSensorsContract() {} //prevent accidental instantiation

    //define the table contents
    public static abstract class SensorEntry implements BaseColumns {
        public static final String TABLE_NAME = "sensor";
        public static final String SENSOR_ID = "sensorId";
        public static final String SENSOR_VALUE = "sensorValue";
        public static final String SENSOR_BATTERY = "sensorBattery";
        public static final String SENSOR_HUMANREADABLE = "sensorHumanReadable";
    }

    //SensorReaderDbHelper mDbHelper = new SensorReaderDbHelper(getContext());
}
