package se.melent.closebitconandroid.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.melent.closebitconandroid.RssiComparator;
import se.melent.closebitconandroid.bluetooth.BluetoothMaster;
import se.melent.closebitconandroid.R;
import se.melent.closebitconandroid.bluetooth.BluetoothConnectionInfo;
import se.melent.closebitconandroid.bluetooth.OnConnectionsChanged;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Switch scanToggle;
    private LinearLayout linearLayout;

    private BluetoothMaster bluetoothMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scanToggle = (Switch) findViewById(R.id.scanToggle);
        linearLayout = (LinearLayout) findViewById(R.id.devices_scoll_view);

        bluetoothMaster = new BluetoothMaster(this);

        if (bluetoothMaster.bluetoothSupported() == false)
        {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
        }
        if (bluetoothMaster.isEnabled() == false)
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Toast.makeText(this, R.string.welcome_text, Toast.LENGTH_SHORT).show();

        showSwitch();

        bluetoothMaster.addChangeListener(new OnConnectionsChanged()
        {
            @Override
            public void changed(final List<BluetoothConnectionInfo> devices)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        linearLayout.removeAllViews();
                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        synchronized (devices)
                        {
                            Collections.sort(devices, new RssiComparator());
                            for (BluetoothConnectionInfo device : devices)
                            {
                                createDeviceRow(inflater, device);
                            }
                        }
                    }
                });
            }
        });
    }

    private void createDeviceRow(LayoutInflater inflater, final BluetoothConnectionInfo device)
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

    private void showSwitch()
    {
        scanToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked)
            {
                scanDevice(isChecked);
            }
        });
    }

    public void scanDevice(final boolean enable)
    {
        bluetoothMaster.enable(enable);
        if (enable)
        {
            Log.d(TAG, "Scanning Started");
        } else
        {
            Log.d(TAG, "Scanning Stopped");
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        bluetoothMaster.togglePingService(true);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        bluetoothMaster.togglePingService(false);
    }
}
