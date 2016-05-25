package se.melent.closebitconandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.UUID;

import se.melent.closebitconandroid.R;
import se.melent.closebitconandroid.bluetooth.BluetoothConnectionInfo;
import se.melent.closebitconandroid.extra.AutoLog;
import se.melent.closebitconandroid.extra.EncodeUtils;
import se.melent.closebitconandroid.extra.Toasters;

/**
 * Created by MelEnt on 2016-05-18.
 */
public class BeaconFormActivity extends AppCompatActivity
{
    private EditText macAddress;
    private EditText adminKey;
    private EditText mobileKey;
    private EditText beaconType;
    private EditText majorNumber;
    private EditText minorNumber;
    private EditText proxUUID;
    private BluetoothConnectionInfo btci;
    private byte[] requestToken;
    private String stringMacAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_form);
        Toasters.setContext(this);

        Intent intent        = getIntent();
        btci                 = intent.getParcelableExtra("BEACON");
        String sha1Code      = intent.getStringExtra("SHA1");
        String authCode      = intent.getStringExtra("AUTHCODE");
        String uuid          = generateHashedUUID(btci.getAddress());

        macAddress           = (EditText)findViewById(R.id.beaconform_mac_address_edit);
        adminKey             = (EditText)findViewById(R.id.beaconform_admin_nbrs_adminkey_edit);
        mobileKey            = (EditText)findViewById(R.id.beaconform_admin_nbrs_mobilekey_edit);
        beaconType           = (EditText)findViewById(R.id.beaconform_type_edit);
        majorNumber          = (EditText)findViewById(R.id.beaconform_majorminor_nbrs_major_edit);
        minorNumber          = (EditText)findViewById(R.id.beaconform_majorminor_nbrs_minor_edit);
        proxUUID             = (EditText)findViewById(R.id.beaconform_proxUUID_edit);

        macAddress.setText(btci.getAddress(), TextView.BufferType.EDITABLE);
        proxUUID.setText(generateHashedUUID(btci.getAddress()));
        stringMacAddress     = macAddress.getText().toString();

        byte[] protocolVersion = new byte[]{1,0,0,0};
        byte[] authCodeBytes   = authCode.getBytes();
        byte[] macAddressBytes = convertMacAddressToByteArray(stringMacAddress);
        byte[] adminKeyBytes   = EncodeUtils.generateKeyBytes(stringMacAddress, "ADMIN_KEY");
        byte[] mobileKeyBytes  = EncodeUtils.generateKeyBytes(stringMacAddress, "MOBILE_KEY");
        byte[] beaconTypeBytes = new byte[]{100};
        byte[] majorNbrBytes   = new byte[]{6,9};
        byte[] minorNbrBytes   = new byte[]{8,8};
        byte[] uuidBytes       = uuid.getBytes();
        AutoLog.debug("AdminKeyBytes: " + EncodeUtils.joinArray(",", ArrayUtils.toObject(adminKeyBytes)));
        AutoLog.debug("MobileKeyBytes: " + TextUtils.join(",", ArrayUtils.toObject(mobileKeyBytes)));

        requestToken = EncodeUtils.concatArrays(
                protocolVersion,
                authCodeBytes,
                macAddressBytes,
                adminKeyBytes,
                mobileKeyBytes,
                beaconTypeBytes,
                majorNbrBytes,
                minorNbrBytes);

        if(requestToken.length == 83)
        {
            Toasters.show("VALID HOORRAAY");
        }
        else
        {
            Toasters.show("Invalid length of requestToken. Current: " +requestToken.length);
        }




    }

    private String generateHashedUUID(String hash)
    {
        return UUID.nameUUIDFromBytes(hash.getBytes()).toString();
    }

    private byte[] convertMacAddressToByteArray(String macAddress)
    {
        String[] macAddressArray = macAddress.split(":");

        byte[] macAddressAsByteArray = new byte[6];

        for(int i = 0; i < macAddressArray.length; i++)
        {
            macAddressAsByteArray[i] = Integer.decode("0x" + macAddressArray[i]).byteValue();
        }

        return macAddressAsByteArray;
    }
}


