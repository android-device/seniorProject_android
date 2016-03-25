package com.mooo.samcat.temperaturemonitor;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private SensorReaderDbHelper sDbHelper;
    private SQLiteDatabase db;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);


        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.sensor_headings));

        //Get saved sensors
        sDbHelper = new SensorReaderDbHelper(this);
        Log.d("STATE","past dbHelper");
        db = sDbHelper.getReadableDatabase();
        Log.d("STATE","past getReadable");

        String[] projection = {
                SavedSensorsContract.SensorEntry._ID,
                SavedSensorsContract.SensorEntry.SENSOR_ID,
                SavedSensorsContract.SensorEntry.SENSOR_VALUE,
                SavedSensorsContract.SensorEntry.SENSOR_BATTERY
        };
        Log.d("STATE","past projection");

        //String sortOrder = SavedSensorsContract.SensorEntry._COUNT + "DESC";
        Log.d("STATE","past sort");

        Cursor c = db.query(
                SavedSensorsContract.SensorEntry.TABLE_NAME, //table to query
                projection, //columns to return
                null,
                null,
                null,
                null,
                null
        );
        Log.d("STATE","past query");

        if(c.moveToFirst()) {
            Toast.makeText(this,"No Sensors",Toast.LENGTH_LONG).show();
            Log.d("STATE", "past toast");
        }
        Log.d("STATE","end!");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void start_addSensor(View view) {
        Intent intent = new Intent(this, addSensor.class);
        startActivity(intent);
    }
}
