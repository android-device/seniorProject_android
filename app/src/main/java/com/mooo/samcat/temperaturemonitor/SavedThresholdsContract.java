package com.mooo.samcat.temperaturemonitor;

import android.provider.BaseColumns;

/**
 * Created by rodrigo on 3/25/16.
 */
public class SavedThresholdsContract {
    public SavedThresholdsContract() {} //prevent accidental instantiation

    //define the table contents
    public static abstract class ThresholdEntry implements BaseColumns {
        public static final String TABLE_NAME = "threshold";
        public static final String THRESHOLD_VALUE = "value";
            }
}
