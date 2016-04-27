package com.mooo.samcat.temperaturemonitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.mooo.samcat.temperaturemonitor.dummy.DummyContent;

import java.util.ArrayList;
import java.util.List;

//import java.util.logging.Handler;

public class addSensor extends AppCompatActivity implements sensorItemFragment.OnListFragmentInteractionListener {
    private static int bleRequestCode = 2;
    private static final long SCAN_PERIOD = 100000;
    private boolean mScanning;
    private Handler mHandler;
    public static final List<sensor> devices = new ArrayList<sensor>();
    public static RecyclerView sensorItemFragmentRecyclerView;

    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mHandler = new Handler();
        setTitle(getString(R.string.title_activity_add_sensor));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sensor);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
        devices.clear();
        sensorItemFragmentRecyclerView.getAdapter().notifyDataSetChanged();
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

    private void interpretBleDevice(BluetoothDevice device,String uuidInfo) {
        sensor newSensor = new sensor();
        newSensor.setAddress(device.getAddress());
        newSensor.setData(uuidInfo);
        //Toast.makeText(this,uuidInfo.substring(50,58),Toast.LENGTH_SHORT).show();
        if(validateDevice(newSensor)) {
            devices.add(newSensor);
            sensorItemFragmentRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private boolean validateDevice(sensor item) {
        for(int i=0; i < devices.size(); i++) { //no duplicates in found devices
            if (devices.get(i).getAddress().equals(item.getAddress()))
                return false;
        }
        return MainActivity.checkForDuplicateDevice(item);
    }

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

    private String enteredName = "";
    private sensor sensorToReturn;

    public void onListFragmentInteraction(final sensor item) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.name_input));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        sensorToReturn = item;

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                enteredName = input.getText().toString();
                close();
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

    private void close() {
        setResult(RESULT_OK);
        Intent data = this.getIntent();
        sensorToReturn.setName(enteredName);
        data.putExtra("sensorID", sensorToReturn.getDeviceID());
        data.putExtra("sensorAddress", sensorToReturn.getAddress());
        data.putExtra("sensorName", sensorToReturn.getName());
        if (getParent() == null) {
            setResult(RESULT_OK, data);
        } else {
            getParent().setResult(RESULT_OK, data);
        }
        finish();
    }
}
