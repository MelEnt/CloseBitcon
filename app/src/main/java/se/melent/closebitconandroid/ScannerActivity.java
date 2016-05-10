package se.melent.closebitconandroid;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.Scanner;

/**
 * Created by MelEnt on 2016-05-03.
 */
public class ScannerActivity extends ListActivity implements BluetoothAdapter.LeScanCallback
{
    private static final String TAG = ScannerActivity.class.getSimpleName();
    private DeviceListAdapter deviceListAdapter;
    private BluetoothAdapter btAdapter;
    private boolean isScanning;
    private Handler handler;

    private static final long SCAN_PERIOD = 10000;

    public ScannerActivity()
    {}


    public ScannerActivity(BluetoothAdapter btAdapter)
    {
        this.btAdapter = btAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanDevice(true);
    }

    public void scanDevice(final boolean enable)
    {
        if(enable) {
            isScanning = true;
            btAdapter.startLeScan(ScannerActivity.this);
            Log.d(TAG, "Scanning Started");
        }
        else
        {
            isScanning = false;
            btAdapter.stopLeScan(ScannerActivity.this);
            Log.d(TAG, "Scanning Stopped");
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int i, byte[] bytes)
    {
        Toast.makeText(this, "Device Found: " + device.getName(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Found device: " + device.getName());
    }
}
