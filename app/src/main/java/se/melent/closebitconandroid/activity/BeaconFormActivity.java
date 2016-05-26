package se.melent.closebitconandroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Array;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
    private String authCode;
    private String uuid;
    private String publicKey;
    private String connectToken;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_form);
        Toasters.setContext(this);

        Intent intent        = getIntent();
        btci                 = intent.getParcelableExtra("BEACON");
        String sha1Code      = intent.getStringExtra("SHA1");
        authCode             = intent.getStringExtra("AUTHCODE");
        uuid                 = generateHashedUUID(btci.getAddress());
        publicKey            = intent.getStringExtra("PUBLIC_KEY");

        macAddress           = (EditText)findViewById(R.id.beaconform_mac_address_edit);
        adminKey             = (EditText)findViewById(R.id.beaconform_admin_nbrs_adminkey_edit);
        mobileKey            = (EditText)findViewById(R.id.beaconform_admin_nbrs_mobilekey_edit);
        beaconType           = (EditText)findViewById(R.id.beaconform_type_edit);
        majorNumber          = (EditText)findViewById(R.id.beaconform_majorminor_nbrs_major_edit);
        minorNumber          = (EditText)findViewById(R.id.beaconform_majorminor_nbrs_minor_edit);
        proxUUID             = (EditText)findViewById(R.id.beaconform_proxUUID_edit);

        macAddress.setText(btci.getAddress(), TextView.BufferType.EDITABLE);
        proxUUID.setText(uuid);
        stringMacAddress     = macAddress.getText().toString();

    }

    public void submitForm(View view) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidKeyException, InvalidKeySpecException
    {
        encodeRequest();
    }

    private String generateHashedUUID(String hash)
    {
        return UUID.nameUUIDFromBytes(hash.getBytes()).toString();
    }

    private void encodeRequest() throws InvalidKeySpecException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException
    {
        byte[] protocolVersion = new byte[]{1,0,0,0};
        byte[] authCodeBytes   = authCode.getBytes();
        byte[] macAddressBytes = EncodeUtils.stringMacToBytes(stringMacAddress);
        byte[] adminKeyBytes   = EncodeUtils.generateKeyBytes(stringMacAddress + "ADMIN_KEY", 20);
        byte[] mobileKeyBytes  = EncodeUtils.generateKeyBytes(stringMacAddress + "MOBILE_KEY", 20);
        byte[] beaconTypeBytes = new byte[]{100};
        byte[] majorNbrBytes   = new byte[]{6,9};
        byte[] minorNbrBytes   = new byte[]{8,8};
        byte[] proxUUIDBytes   = EncodeUtils.convertUUIDToByteArray(UUID.fromString(uuid), 16);

        AutoLog.debug("AdminKeyBytes: " + EncodeUtils.joinArray(",", ArrayUtils.toObject(adminKeyBytes)));
        AutoLog.debug("MobileKeyBytes: " + EncodeUtils.joinArray(",", ArrayUtils.toObject(mobileKeyBytes)));
        AutoLog.debug("MacToBytes: " + EncodeUtils.joinArray(", ", ArrayUtils.toObject(macAddressBytes)));

        requestToken = EncodeUtils.concatArrays(
                protocolVersion,
                authCodeBytes,
                macAddressBytes,
                adminKeyBytes,
                mobileKeyBytes,
                beaconTypeBytes,
                majorNbrBytes,
                minorNbrBytes,
                proxUUIDBytes);

        if(requestToken.length == 83)
        {
            Toasters.show("VALID HOORRAAY");
            connectToken = EncodeUtils.encodeToString(requestToken, EncodeUtils.generatePublicKey(publicKey));
            connect(connectToken);
        }
        else
        {
            Toasters.show("Invalid length of requestToken. Current: " +requestToken.length);
        }
    }//encodeReq

    private void connect(final String connectToken)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Connection connection = Jsoup.connect("http://smartsensor.io/CBtest/activate_beacon.php");
                connection.data("enc", connectToken); //Jsoup does automatic URLEncoding (utf-8) to connection.data values

                // LOG URL ENCODED VALUE (EVEN THO connection.data encodes value with URL encoding)
                try
                {
                    AutoLog.debug("ConnToken: " + URLEncoder.encode(connectToken, "UTF-8"));
                } catch (UnsupportedEncodingException e)
                {
                    e.printStackTrace();
                }
                Document result = null;
                try
                {
                    result = connection.get();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                AutoLog.debug(result.body().toString());
            }
        }).start();

    }
}//class


