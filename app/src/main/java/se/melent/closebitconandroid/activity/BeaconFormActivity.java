package se.melent.closebitconandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import se.melent.closebitconandroid.R;
import se.melent.closebitconandroid.bluetooth.BluetoothConnectionInfo;

/**
 * Created by MelEnt on 2016-05-18.
 */
public class BeaconFormActivity extends AppCompatActivity
{
    private EditText macAdress;
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

        macAdress = (EditText)findViewById(R.id.macAdress);
        adminKey = (EditText)findViewById(R.id.adminKey);
        mobileKey = (EditText)findViewById(R.id.mobileKey);
        beaconType = (EditText)findViewById(R.id.beaconType);
        majorNumber = (EditText)findViewById(R.id.majorNumber);
        minorNumber = (EditText)findViewById(R.id.minorNumber);
        proxUUID = (EditText)findViewById(R.id.proxUUID);

        Intent intent = getIntent();
        BluetoothConnectionInfo bluetoothConnectionInfo = intent.getParcelableExtra("BEACON");
        String sha1Code = intent.getStringExtra("SHA1");

    }
}


