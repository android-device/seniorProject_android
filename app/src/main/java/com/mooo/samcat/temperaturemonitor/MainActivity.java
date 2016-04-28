package com.mooo.samcat.temperaturemonitor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

//import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity implements OnListFragmentInteractionListener {

    private CollapsingToolbarLayout collapsingToolbarLayout;
    private SensorReaderDbHelper sDbHelper;
    private SQLiteDatabase dbRead;
    private SQLiteDatabase dbWrite;
    private ContentValues valuesToAdd_db;
    public final static String EXTRA_DB = "com.mooo.samcat.temperaturemonitor.db";
    private TextView userMessage;
    private TextView contentMessage;
    public static List<sensor> savedDevices = new ArrayList<sensor>();

    //Activity Request Codes
    private final static int dataBaseRequestCode = 1;
    private static int bleRequestCode = 2;

    private BluetoothAdapter mBluetoothAdapter;

    private String[] projection = {
            SavedSensorsContract.SensorEntry._ID,
            SavedSensorsContract.SensorEntry.SENSOR_ID,
            SavedSensorsContract.SensorEntry.SENSOR_ADDRESS,
            SavedSensorsContract.SensorEntry.SENSOR_HUMANREADABLE
    };

    protected void onCreate(Bundle savedInstanceState) {
        /*Initializes Bluetooth adapter*/
        mHandler = new Handler();
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        /*Setup UI*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //userMessage.setTextSize(0);
        userMessage = (TextView) findViewById(R.id.main_message);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.sensor_headings));

        /*Setup Database*/
        sDbHelper = new SensorReaderDbHelper(this);
        dbRead = sDbHelper.getReadableDatabase();
        dbWrite = sDbHelper.getWritableDatabase();
        valuesToAdd_db = new ContentValues();

        Cursor c = dbRead.query(
                SavedSensorsContract.SensorEntry.TABLE_NAME, //table to query
                projection, //columns to return
                null,
                null,
                null,
                null,
                null //no sort
        );

        if(c.moveToFirst()) { //There are Sensors in the database
            sensor newSensor = new sensor(c.getString(c.getColumnIndex(SavedSensorsContract.SensorEntry.SENSOR_HUMANREADABLE)),
                    c.getString(c.getColumnIndex(SavedSensorsContract.SensorEntry.SENSOR_ID)),
                    c.getString(c.getColumnIndex(SavedSensorsContract.SensorEntry.SENSOR_ADDRESS)));
            savedDevices.add(newSensor);
            userMessage.setTextSize(0);
        } else { //No Sensors in the database
            userMessage.setTextSize((int)this.getResources().getDimension(R.dimen.message_text_size));
            userMessage.setText(getString(R.string.no_sensors));
        }

        scheduleRefresh();
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
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
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

    private Intent addSensorIntent;

    public void start_addSensor(View view) {
        addSensorIntent = new Intent(this, addSensor.class);
        startActivityForResult(addSensorIntent, dataBaseRequestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == dataBaseRequestCode) {
            if (resultCode == RESULT_OK) {
                String newName = data.getStringExtra("sensorName");
                String newID = data.getStringExtra("sensorID");
                String newAddress = data.getStringExtra("sensorAddress");

                valuesToAdd_db.put(SavedSensorsContract.SensorEntry.SENSOR_ID, newID);
                valuesToAdd_db.put(SavedSensorsContract.SensorEntry.SENSOR_HUMANREADABLE, newName);
                valuesToAdd_db.put(SavedSensorsContract.SensorEntry.SENSOR_ADDRESS, newAddress);
                sensor newSensor = new sensor(newName, newID, newAddress);
                savedDevices.add(newSensor);
                sensorItemFragmentRecyclerView.getAdapter().notifyDataSetChanged();
                dbWrite.insert(SavedSensorsContract.SensorEntry.TABLE_NAME, null, valuesToAdd_db);
                userMessage.setTextSize(0);
                refreshSensors();
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

    public static boolean checkForDuplicateDevice(sensor item) {
        for(int i = 0; i < savedDevices.size(); i++) {
            if(savedDevices.get(i).getAddress().equals(item.getAddress())) //already in saved devices
                return false;
        }
        return  true;
    }

    private void refreshSensors() {
        checkBluetooth(); //hasn't been disabled, right???
        scanLeDevice(true);
    }

    private Handler mHandler;
    private boolean mScanning;
    private static final long SCAN_PERIOD = 100000;
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] serviceUuidBytes = new byte[scanRecord.length];
                            String serviceUuid = "";
                            if(scanRecord != null) {
                                for (int i = 0; i < scanRecord.length; i++) {
                                    serviceUuidBytes[i] = scanRecord[i];
                                }
                                serviceUuid = bytesToHex(serviceUuidBytes);
                            }
                            interpretBleDevice(device,serviceUuid);
                        }
                    });
                }
            };

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for(int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static RecyclerView sensorItemFragmentRecyclerView;
    private void interpretBleDevice(BluetoothDevice device,String uuidInfo) {
        if(validateDevice(device.getAddress())) {
            savedDevices.get(sensorAt).setTemp_batt(uuidInfo);
            sensorItemFragmentRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private int sensorAt;
    private boolean validateDevice(String address) {
        for(int i=0; i<savedDevices.size(); i++) {
            if(savedDevices.get(i).getAddress().equals(address)) { //found a matching saved sensor!
                sensorAt = i;
                return true; //good device!
            }
        }
        return false;
    }

    private final int DELAY = 7500;
    private Handler nHandler = new Handler();
    public void scheduleRefresh() {
        nHandler.postDelayed(new Runnable() {
            public void run() {
                refreshSensors();          // this method will contain your almost-finished HTTP calls
                nHandler.postDelayed(this, DELAY);
            }
        }, DELAY);
    }

    @Override
    public void OnListFragmentInteraction(sensor item) {
        refreshSensors();
    }

    /*@Override
    public void onListFragmentInteraction(sensor item) {
        int j = 1;
        j++;
        System.out.println("j" + j);
    }*/
}
