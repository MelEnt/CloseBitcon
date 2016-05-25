package se.melent.closebitconandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

import se.melent.closebitconandroid.R;
import se.melent.closebitconandroid.bluetooth.BluetoothConnectionInfo;

/**
 * Created by MelEnt on 2016-05-18.
 */
public class BeaconFormActivity extends AppCompatActivity
{
    private EditText macAdress;
    private String currentDeviceAddress;
    private EditText adminKey;
    private EditText mobileKey;
    private EditText beaconType;
    private EditText majorNumber;
    private EditText minorNumber;
    private EditText proxUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_form);

        Intent intent = getIntent();
        BluetoothConnectionInfo bluetoothConnectionInfo = intent.getParcelableExtra("BEACON");
        String sha1Code = intent.getStringExtra("SHA1");
        currentDeviceAddress = bluetoothConnectionInfo.getAddress();

        macAdress            = (EditText)findViewById(R.id.beaconform_mac_address_edit);
        adminKey             = (EditText)findViewById(R.id.beaconform_admin_nbrs_adminkey_edit);
        mobileKey            = (EditText)findViewById(R.id.beaconform_admin_nbrs_mobilekey_edit);
        beaconType           = (EditText)findViewById(R.id.beaconform_type_edit);
        majorNumber          = (EditText)findViewById(R.id.beaconform_majorminor_nbrs_major_edit);
        minorNumber          = (EditText)findViewById(R.id.beaconform_majorminor_nbrs_minor_edit);
        proxUUID             = (EditText)findViewById(R.id.beaconform_proxUUID_edit);

        macAdress.setText(currentDeviceAddress, TextView.BufferType.EDITABLE);

        proxUUID.setText(generateUUID(currentDeviceAddress));


    }

    private String generateUUID(String deviceAddress)
    {
        return null;
    }
}


