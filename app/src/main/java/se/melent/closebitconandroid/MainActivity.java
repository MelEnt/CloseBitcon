package se.melent.closebitconandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback{

    private List<BluetoothConnectionInfo> devices = new ArrayList<>();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static BluetoothAdapter bluetoothAdapter;
    private boolean isScanning;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Toast.makeText(this, R.string.welcome_text, Toast.LENGTH_SHORT).show();

        showSwitch();
    }

    private void showSwitch()
    {
        Switch scanToggle = (Switch) findViewById(R.id.scanToggle);
        scanToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked)
                {
                    scanDevice(true);
                }
                else
                {
                    scanDevice(false);
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        recyclerView = (RecyclerView) findViewById(R.id.devices_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DeviceListAdapter(this, devices));
    }

    public void scanDevice(final boolean enable)
    {
        if(enable) {
            isScanning = true;
            bluetoothAdapter.startLeScan(MainActivity.this);
            Log.d(TAG, "Scanning Started");
        }
        else
        {
            isScanning = false;
            bluetoothAdapter.stopLeScan(MainActivity.this);
            Log.d(TAG, "Scanning Stopped");
        }
    }


    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] bytes)
    {
        devices.add(new BluetoothConnectionInfo(device, rssi, bytes));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Device Found: " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        Log.d(TAG, "Found device: " + device.toString() + " with strength: " + rssi);
        recyclerView.invalidate();
    }
}
