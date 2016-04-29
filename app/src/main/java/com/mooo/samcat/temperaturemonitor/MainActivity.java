package com.mooo.samcat.temperaturemonitor;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
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
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

//import java.util.jar.Manifest;

public class MainActivity extends AppCompatActivity implements OnListFragmentInteractionListener {

    private CollapsingToolbarLayout collapsingToolbarLayout;

    private SensorReaderDbHelper sensor_sDbHelper;
    private ThresholdReaderDbHelper threshold_sDbHelper;

    private SQLiteDatabase sensor_dbRead;
    private SQLiteDatabase sensor_dbWrite;
    private SQLiteDatabase threshold_dbRead;

    private ContentValues sensor_valuesToAdd_db;

    public final static String EXTRA_DB = "com.mooo.samcat.temperaturemonitor.db";
    private TextView userMessage;

    public static List<sensor> savedDevices = new ArrayList<sensor>();
    public static List<Integer> thresholds = new ArrayList<Integer>();

    //Activity Request Codes
    private final static int dataBase_RequestCode = 1;
    private final static int ble_RequestCode = 2;
    private final static int settings_RequestCode = 3;

    private BluetoothAdapter mBluetoothAdapter;

    private String[] sensor_projection = {
            SavedSensorsContract.SensorEntry._ID,
            SavedSensorsContract.SensorEntry.SENSOR_ID,
            SavedSensorsContract.SensorEntry.SENSOR_ADDRESS,
            SavedSensorsContract.SensorEntry.SENSOR_HUMANREADABLE
    };
    private String[] threshold_projection = {
            SavedThresholdsContract.ThresholdEntry._ID,
            SavedThresholdsContract.ThresholdEntry.THRESHOLD_VALUE
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

        /*Setup Databases*/
        sensor_sDbHelper = new SensorReaderDbHelper(this);
        sensor_dbRead = sensor_sDbHelper.getReadableDatabase();
        sensor_dbWrite = sensor_sDbHelper.getWritableDatabase();
        sensor_valuesToAdd_db = new ContentValues();
        threshold_sDbHelper = new ThresholdReaderDbHelper(this);
        threshold_dbRead = threshold_sDbHelper.getReadableDatabase();

        Cursor sensor_cursor = sensor_dbRead.query(
                SavedSensorsContract.SensorEntry.TABLE_NAME, //table to query
                sensor_projection, //columns to return
                null,
                null,
                null,
                null,
                null //no sort
        );
        Cursor threshold_cursor = threshold_dbRead.query(
                SavedThresholdsContract.ThresholdEntry.TABLE_NAME,
                threshold_projection,
                null,
                null,
                null,
                null,
                null //no sort
        );

        if(sensor_cursor.moveToFirst()) { //There are Sensors in the database
            sensor newSensor = new sensor(sensor_cursor.getString(sensor_cursor.getColumnIndex(SavedSensorsContract.SensorEntry.SENSOR_HUMANREADABLE)),
                    sensor_cursor.getString(sensor_cursor.getColumnIndex(SavedSensorsContract.SensorEntry.SENSOR_ID)),
                    sensor_cursor.getString(sensor_cursor.getColumnIndex(SavedSensorsContract.SensorEntry.SENSOR_ADDRESS)));
            savedDevices.add(newSensor);
            while(!sensor_cursor.isLast()) { //Iterate through the database and load every device
                sensor_cursor.moveToNext();
                newSensor = new sensor(sensor_cursor.getString(sensor_cursor.getColumnIndex(SavedSensorsContract.SensorEntry.SENSOR_HUMANREADABLE)),
                        sensor_cursor.getString(sensor_cursor.getColumnIndex(SavedSensorsContract.SensorEntry.SENSOR_ID)),
                        sensor_cursor.getString(sensor_cursor.getColumnIndex(SavedSensorsContract.SensorEntry.SENSOR_ADDRESS)));
                savedDevices.add(newSensor);
            }
            sensorItemFragmentRecyclerView.getAdapter().notifyDataSetChanged(); //display
            userMessage.setTextSize(0); //Hide "no sensors message"
        } else { //No Sensors in the database
            userMessage.setTextSize((int)this.getResources().getDimension(R.dimen.message_text_size));
            userMessage.setText(getString(R.string.no_sensors));
        }

        if(threshold_cursor.moveToFirst()) { //there are thresholds in the database
            thresholds.add(threshold_cursor.getInt(threshold_cursor.getColumnIndex(SavedThresholdsContract.ThresholdEntry.THRESHOLD_VALUE)));
            while(!threshold_cursor.isLast()) {
                thresholds.add(threshold_cursor.getInt(threshold_cursor.getColumnIndex(SavedThresholdsContract.ThresholdEntry.THRESHOLD_VALUE)));
            }
        }

        scheduleRefresh();
    }

    public void checkBluetooth() {
        /* Ensures Bluetooth is available on the device and it is enabled. If not,
        displays a dialog requesting user permission to enable Bluetooth. */
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ble_RequestCode);
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
            Intent notificationManagerIntent = new Intent(this,notificationManager.class);
            startActivityForResult(notificationManagerIntent, settings_RequestCode);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent addSensorIntent;

    public void start_addSensor(View view) {
        addSensorIntent = new Intent(this, addSensor.class);
        startActivityForResult(addSensorIntent, dataBase_RequestCode);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == dataBase_RequestCode) {
            if (resultCode == RESULT_OK) {
                String newName = data.getStringExtra("sensorName");
                String newID = data.getStringExtra("sensorID");
                String newAddress = data.getStringExtra("sensorAddress");

                sensor_valuesToAdd_db.put(SavedSensorsContract.SensorEntry.SENSOR_ID, newID);
                sensor_valuesToAdd_db.put(SavedSensorsContract.SensorEntry.SENSOR_HUMANREADABLE, newName);
                sensor_valuesToAdd_db.put(SavedSensorsContract.SensorEntry.SENSOR_ADDRESS, newAddress);
                sensor newSensor = new sensor(newName, newID, newAddress);
                savedDevices.add(newSensor);
                sensorItemFragmentRecyclerView.getAdapter().notifyDataSetChanged();
                sensor_dbWrite.insert(SavedSensorsContract.SensorEntry.TABLE_NAME, null, sensor_valuesToAdd_db);
                refreshSensors();
            }
            else {
                Toast.makeText(this,getString(R.string.failed_to_add_device),Toast.LENGTH_LONG).show();
            }
        }
        if(requestCode == ble_RequestCode) {
            if(resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this,getString(R.string.no_bluetooth),Toast.LENGTH_SHORT).show();
                finish();
            }
        } else { //Don't recurse...
            checkBluetooth();
        }
        /*When returning from settings, reload the notifications database,
        Regardless of the return status, which is actually unused - so don't
        use the return status!!
         */
        if(requestCode == settings_RequestCode) {
            thresholds.clear();
            Cursor threshold_cursor = threshold_dbRead.query(
                    SavedThresholdsContract.ThresholdEntry.TABLE_NAME,
                    threshold_projection,
                    null,
                    null,
                    null,
                    null,
                    null //no sort
            );
            if(threshold_cursor.moveToFirst()) { //there are thresholds in the database
                thresholds.add(threshold_cursor.getInt(threshold_cursor.getColumnIndex(SavedThresholdsContract.ThresholdEntry.THRESHOLD_VALUE)));
                while(!threshold_cursor.isLast()) {
                    thresholds.add(threshold_cursor.getInt(threshold_cursor.getColumnIndex(SavedThresholdsContract.ThresholdEntry.THRESHOLD_VALUE)));
                }
            }
        }

        scheduleRefresh();
    }

    public static boolean checkForDuplicateDevice(sensor item) {
        for(int i = 0; i < savedDevices.size(); i++) {
            if(savedDevices.get(i).getAddress().equals(item.getAddress())) //already in saved devices
                return false;
        }
        return  true;
    }

    private void refreshSensors() {
        userMessage.setTextSize(0);
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
            for(int i=0; i<thresholds.size(); i++) {
                if (savedDevices.get(sensorAt).getNotify() && //haven't notified for this threshold
                        savedDevices.get(sensorAt).getTemperature() <= thresholds.get(i) //value is within notification range
                        )
                {
                    savedDevices.get(sensorAt).clearNotify(); //don't re-notify
                    String notificationMessage = savedDevices.get(sensorAt).getName() + " reached " + thresholds.get(i).toString() + "C";
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_bluetooth_icon)
                            .setContentTitle("Alert Temperature Reached")
                            .setContentText(notificationMessage);
                    Intent resultIntent = new Intent(this, MainActivity.class);
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);
                    NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(4,mBuilder.build());
                }
            }
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
