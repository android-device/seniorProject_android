package com.mooo.samcat.temperaturemonitor;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class notificationManager extends AppCompatActivity implements OnThresholdListFragmentInteraction {

    private SQLiteDatabase getThreshold_dbWrite;
    private ThresholdReaderDbHelper threshold_sDbHelper;
    private ContentValues threshold_valuesToAdd_db;
    private SQLiteDatabase threshold_dbRead;
    private SQLiteDatabase threshold_dbWrite;

    public static List<Integer> thresholds = new ArrayList<Integer>();

    private String[] threshold_projection = {
            SavedThresholdsContract.ThresholdEntry._ID,
            SavedThresholdsContract.ThresholdEntry.THRESHOLD_VALUE
    };

    public static RecyclerView thresholdItemFragmentRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addThreshold();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        threshold_sDbHelper = new ThresholdReaderDbHelper(this);
        threshold_dbRead = threshold_sDbHelper.getReadableDatabase();
        threshold_dbWrite = threshold_sDbHelper.getWritableDatabase();
        threshold_valuesToAdd_db = new ContentValues();

        Cursor threshold_cursor = threshold_dbRead.query(
                SavedThresholdsContract.ThresholdEntry.TABLE_NAME,
                threshold_projection,
                null,
                null,
                null,
                null,
                null //no sort
        );

        thresholds.clear();

        if(threshold_cursor.moveToFirst()) { //there are thresholds in the database
            thresholds.add(threshold_cursor.getInt(threshold_cursor.getColumnIndex(SavedThresholdsContract.ThresholdEntry.THRESHOLD_VALUE)));
            while(!threshold_cursor.isLast()) {
                threshold_cursor.moveToNext();
                thresholds.add(threshold_cursor.getInt(threshold_cursor.getColumnIndex(SavedThresholdsContract.ThresholdEntry.THRESHOLD_VALUE)));
            }
        }

        thresholdItemFragmentRecyclerView.getAdapter().notifyDataSetChanged();
    }

    public void addThreshold() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.add_notification_threshold));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String newValue = input.getText().toString();
                    int newValueInt = Integer.parseInt(newValue);
                    if(!thresholds.contains(newValueInt)) {
                        threshold_valuesToAdd_db.put(SavedThresholdsContract.ThresholdEntry.THRESHOLD_VALUE, newValue);
                        thresholds.add(newValueInt);
                        thresholdItemFragmentRecyclerView.getAdapter().notifyDataSetChanged();
                        threshold_dbWrite.insert(SavedThresholdsContract.ThresholdEntry.TABLE_NAME, null, threshold_valuesToAdd_db);
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getApplicationContext(),"Enter only an integer value",Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void OnThresholdListFragmentInteraction (Integer value) {
        //thresholds.remove(value);
    }
}
