package se.melent.closebitconandroid.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.melent.closebitconandroid.bluetooth.bci_sorters.Order;
import se.melent.closebitconandroid.bluetooth.bci_sorters.RssiComparator;
import se.melent.closebitconandroid.bluetooth.BluetoothMaster;
import se.melent.closebitconandroid.R;
import se.melent.closebitconandroid.bluetooth.BluetoothConnectionInfo;
import se.melent.closebitconandroid.bluetooth.OnConnectionsChanged;
import se.melent.closebitconandroid.bubbles.BubbleScreen;
import se.melent.closebitconandroid.extra.AutoLog;
import se.melent.closebitconandroid.extra.Toasters;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_ENABLE_BT = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private Switch scanToggle;
    private LinearLayout linearLayout;
    private BubbleScreen bubbleScreen;

    private BluetoothMaster bluetoothMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        scanToggle = (Switch) findViewById(R.id.scanToggle);
        linearLayout = (LinearLayout) findViewById(R.id.devices_scoll_view);
//        bubbleScreen = (BubbleScreen) findViewById(R.id.main_bubble_screen);

        AutoLog.introduce();
        Toasters.setContext(this);

        // PREPARE BLUETOOTH //
        bluetoothMaster = new BluetoothMaster(this);
        if (bluetoothMaster.bluetoothSupported() == false)
        {
            AutoLog.warn("This device does not support Bluetooth LE");
            scanToggle.setText("Bluetooth unavailable");
            scanToggle.setClickable(false);
            Toasters.show(R.string.ble_not_supported);
            return;
        }
        if (bluetoothMaster.isEnabled() == false) // what is this code for?
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        Toasters.show(R.string.welcome_text);

        // SET CALLBACK FOR BLUETOOTH //
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

                        List<BluetoothConnectionInfo> mutableDeviceList = new ArrayList<>(devices);

                        Collections.sort(mutableDeviceList, new RssiComparator(Order.ASCENDING));
                        for (BluetoothConnectionInfo device : mutableDeviceList)
                        {
                            createDeviceRow(inflater, device);
                        }

                    }
                });
            }
        });

        // PREPARE SWITCH VIEW //
        scanToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enable)
            {
                bluetoothMaster.enable(enable);
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
                Intent intent = new Intent(MainActivity.this, AuthUserActivity.class);
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

    @Override
    protected void onResume()
    {
        super.onResume();
        bluetoothMaster.togglePingService(true);
//        bubbleScreen.setUpdatingState(true);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        bluetoothMaster.togglePingService(false);
//        bubbleScreen.setUpdatingState(false);
    }
}
