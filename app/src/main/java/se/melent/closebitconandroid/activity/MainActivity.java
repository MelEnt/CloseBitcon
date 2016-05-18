package se.melent.closebitconandroid.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.melent.closebitconandroid.R;
import se.melent.closebitconandroid.model.BluetoothConnectionInfo;

public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback
{

    private List<BluetoothConnectionInfo> devices = new ArrayList<>();
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static BluetoothAdapter bluetoothAdapter;
    private boolean isScanning;
    private Switch scanToggle;

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanToggle = (Switch) findViewById(R.id.scanToggle);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Toast.makeText(this, R.string.welcome_text, Toast.LENGTH_SHORT).show();

        showSwitch();
    }

    private void showSwitch()
    {
        scanToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                if (isChecked)
                {
                    scanDevice(true);
                } else
                {
                    scanDevice(false);
                }
            }
        });
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "onResume called");
        super.onResume();
        linearLayout = (LinearLayout) findViewById(R.id.devices_scoll_view);
    }

    public void scanDevice(final boolean enable)
    {
        if (enable)
        {
            isScanning = true;
            bluetoothAdapter.startLeScan(MainActivity.this);
            Log.d(TAG, "Scanning Started");
        } else
        {
            isScanning = false;
            bluetoothAdapter.stopLeScan(MainActivity.this);
            Log.d(TAG, "Scanning Stopped");
        }
    }


    @Override
    public void onLeScan(final BluetoothDevice device, int rssi, byte[] bytes)
    {
        BluetoothConnectionInfo bci = new BluetoothConnectionInfo(device, rssi, bytes);
        synchronized (devices)
        {

            devices.remove(bci);
            devices.add(bci);
        }

        Log.d(TAG, "Found device: " + device.toString() + " with strength: " + rssi);
        redrawList();
    }

    private void redrawList()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {

                linearLayout.removeAllViews();
//                LayoutInflater inflater = getLayoutInflater();
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                synchronized (devices)
                {
                    Collections.sort(devices, new Comparator<BluetoothConnectionInfo>()
                    {
                        @Override
                        public int compare(BluetoothConnectionInfo t1, BluetoothConnectionInfo t2)
                        {
                            return t2.getRssi() - t1.getRssi();
                        }
                    });
                    for (final BluetoothConnectionInfo device : devices)
                    {
                        View view = inflater.inflate(R.layout.device_row, linearLayout, false);
                        view.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent intent = new Intent(MainActivity.this, BeaconFormActivity.class);
                                intent.putExtra("BEACON", device);
                                startActivity(intent);
                                if (scanToggle.isChecked())
                                {
                                    scanToggle.performClick();
                                }
                            }
                        });
                        ((TextView) view.findViewById(R.id.device_address)).setText(device.getDevice().getAddress());
                        ((TextView) view.findViewById(R.id.device_rssi)).setText(String.valueOf(device.getRssi()));
                        linearLayout.addView(view);
                    }
                }
            }
        });

    }
}
