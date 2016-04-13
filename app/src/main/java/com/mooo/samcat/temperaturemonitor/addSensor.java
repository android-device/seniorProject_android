package com.mooo.samcat.temperaturemonitor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.util.ArrayList;

//import java.util.logging.Handler;

public class addSensor extends AppCompatActivity {

    private static int bleRequestCode = 2;
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning;
    private Handler mHandler;
    private ArrayList<sensor> devices;
    private TextView placeholderText;
    private TextView placeholderText2;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mHandler = new Handler();
        devices = new ArrayList<sensor>();
        setTitle(getString(R.string.title_activity_add_sensor));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        placeholderText = (TextView) findViewById(R.id.placeholder2);
        placeholderText2 = (TextView) findViewById(R.id.placeholder3);

        placeholderText.setText("New");
        placeholderText2.setText("Newer");

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        refreshClicked(null);
    }

    public void refreshClicked(View view) {
        Toast.makeText(this, getString(R.string.refreshing_sensors), Toast.LENGTH_SHORT).show();
        checkBluetooth(); //If bluetooth is not set up, quits
        scanLeDevice(true);
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
        //Check for Bluetooth Low Energy - the sensors need it.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == bleRequestCode) {
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, getString(R.string.no_bluetooth), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

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
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sensor newSensor = new sensor();
                            newSensor.setName(device.getName());
                            newSensor.setUUID(device.getAddress());
                            devices.add(newSensor);
                            placeholderText2.setText(device.getAddress());
                            //placeholderText2.setText(placeholderText.getText());
                            placeholderText.setText(device.getName());
                        }
                    });
                }
            };
}
