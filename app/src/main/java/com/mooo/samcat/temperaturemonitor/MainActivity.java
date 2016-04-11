package com.mooo.samcat.temperaturemonitor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private SensorReaderDbHelper sDbHelper;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbWrite;
    private ContentValues valuesToAdd_db;
    public final static String EXTRA_DB = "com.mooo.samcat.temperaturemonitor.db";
    private TextView userMessage;
    private TextView contentMessage;

    //Activity Request Codes
    private final static int dataBaseRequestCode = 1;
    private static int bleRequestCode = 2;

    private BluetoothAdapter mBluetoothAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        // Initializes Bluetooth adapter.
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        userMessage = (TextView) findViewById(R.id.main_message);
        contentMessage = (TextView) findViewById(R.id.placeholder);

        mBluetoothAdapter = bluetoothManager.getAdapter();

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.sensor_headings));

        //Get saved sensors
        sDbHelper = new SensorReaderDbHelper(this);
        Log.d("STATE","past dbHelper");
        dbRead = sDbHelper.getReadableDatabase();
        dbWrite = sDbHelper.getWritableDatabase();
        valuesToAdd_db = new ContentValues();
        Log.d("STATE","past getReadable");

        String[] projection = {
                SavedSensorsContract.SensorEntry._ID,
                SavedSensorsContract.SensorEntry.SENSOR_ID,
                SavedSensorsContract.SensorEntry.SENSOR_VALUE,
                SavedSensorsContract.SensorEntry.SENSOR_BATTERY,
                SavedSensorsContract.SensorEntry.SENSOR_HUMANREADABLE
        };
        Log.d("STATE","past projection");

        //String sortOrder = SavedSensorsContract.SensorEntry._COUNT + "DESC";
        Log.d("STATE","past sort");

        Cursor c = dbRead.query(
                SavedSensorsContract.SensorEntry.TABLE_NAME, //table to query
                projection, //columns to return
                null,
                null,
                null,
                null,
                null //no sort
        );
        Log.d("STATE","past query");

        if(c.moveToFirst()) { //There are Sensors in the database
            Log.d("STATE", "past toast");
        }
        else { //No Sensors in the database
            userMessage.setTextSize((int)this.getResources().getDimension(R.dimen.message_text_size));
            userMessage.setText(getString(R.string.no_sensors));
        }
        Log.d("STATE","end!");

        checkBluetooth();
    }

    public void checkBluetooth() {
        /* Ensures Bluetooth is available on the device and it is enabled. If not,
        displays a dialog requesting user permission to enable Bluetooth. */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, bleRequestCode);
            }
        }
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
        startActivityForResult(intent,dataBaseRequestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == dataBaseRequestCode) {
            if (resultCode == RESULT_OK) {
                valuesToAdd_db.put(SavedSensorsContract.SensorEntry.SENSOR_ID, data.getStringExtra("sensorID"));
                valuesToAdd_db.put(SavedSensorsContract.SensorEntry.SENSOR_HUMANREADABLE, data.getStringExtra("sensorHuman"));
                valuesToAdd_db.put(SavedSensorsContract.SensorEntry.SENSOR_BATTERY, "100");
                valuesToAdd_db.put(SavedSensorsContract.SensorEntry.SENSOR_VALUE, "0");
            }
            else {
                Toast.makeText(this,getString(R.string.failed_to_add_device),Toast.LENGTH_LONG).show();
            }
        }
        if(requestCode == bleRequestCode) {
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this,getString(R.string.no_bluetooth),Toast.LENGTH_SHORT).show();
                finish();
            }
        } else { //Don't recurse...
            checkBluetooth();
        }
    }
}
