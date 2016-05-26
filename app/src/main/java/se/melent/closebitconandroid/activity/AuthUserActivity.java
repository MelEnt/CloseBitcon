package se.melent.closebitconandroid.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import se.melent.closebitconandroid.R;
import se.melent.closebitconandroid.bluetooth.BluetoothConnectionInfo;
import se.melent.closebitconandroid.extra.AutoLog;
import se.melent.closebitconandroid.extra.EncodeUtils;
import se.melent.closebitconandroid.extra.Toasters;

public class AuthUserActivity extends AppCompatActivity {

    private BluetoothConnectionInfo bluetoothConnectionInfo;
    private EditText editText;
    private String stringPublicKey;
    private ProgressDialog progress;
    private PublicKey publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_user);
        Toasters.setContext(this);
        Intent intent = getIntent();
        bluetoothConnectionInfo = intent.getParcelableExtra("BEACON");
    }

    public void submit(View view) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        progress = new ProgressDialog(this);
        progress.setTitle(getString(R.string.dialog_downloading_key));
        progress.setMessage(getString(R.string.dialog_downloading_key_specific));
        getPublicKey("http://smartsensor.io/CBtest/getpubkey.php");
        progress.show();
    }

    private void encodeRequest() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException
    {
        publicKey = EncodeUtils.generatePublicKey(stringPublicKey);
        final String authCode = editText.getText().toString();

        byte[] protocolBytes    = new byte[]{1, 0, 0, 0};
        byte[] authCodeBytes    = authCode.getBytes();
        byte[] saltBytes        = EncodeUtils.randomBytes(4);

        byte[] requestToken = EncodeUtils.concatArrays(protocolBytes, authCodeBytes, saltBytes);

        final String cipherText = EncodeUtils.encodeToString(requestToken, publicKey);

        AutoLog.debug("stringPublicKey: " + stringPublicKey);

        controlServerResponse(requestToken);

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                Connection connection = Jsoup.connect("http://smartsensor.io/CBtest/auth_user.php");
                connection.data("enc", cipherText); //Jsoup does automatic URLEncoding (utf-8) to connection.data values
                try
                {
                    AutoLog.debug("Final key to send as String: " + URLEncoder.encode(cipherText, "UTF-8"));
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
                if(result.body().text().equals("ERROR"))
                {
                    Toasters.show(getString(R.string.auth_failed));
                    return;
                }

                String sha1Code = result.body().text().replace("=", "");
                AutoLog.debug("Sha1Code: " + sha1Code);
                Intent intent = new Intent(AuthUserActivity.this, BeaconFormActivity.class);
                intent.putExtra("SHA1", sha1Code);
                AutoLog.debug("ShaResponseInBytes: " + EncodeUtils.joinArray(", ", ArrayUtils.toObject(DigestUtils.sha(sha1Code))));
                intent.putExtra("BEACON", bluetoothConnectionInfo);
                intent.putExtra("AUTHCODE", authCode);
                intent.putExtra("PUBLIC_KEY", stringPublicKey);
                startActivity(intent);

            }
        }).start();
    }

    private void controlServerResponse(byte[] requestToken) throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, UnsupportedEncodingException
    {
        byte[] okBytes = "OK".getBytes();
        byte[] uknBytes = "UNKNOWN".getBytes();

        byte[] requestTokenOk     = EncodeUtils.concatArrays(requestToken, okBytes);
        byte[] requestTokenUnknown = EncodeUtils.concatArrays(requestToken, uknBytes);

        AutoLog.debug("REQTOKENOK: " + EncodeUtils.joinArray(", ", ArrayUtils.toObject(requestTokenOk)));
        AutoLog.debug("REQTOKENUNKNOWN: " + EncodeUtils.joinArray(", ", ArrayUtils.toObject(requestTokenUnknown)));

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashOk = md.digest(requestTokenOk);
        byte[] hashUnkn = md.digest(requestTokenUnknown);


        AutoLog.debug("OK: " + EncodeUtils.joinArray(", ", ArrayUtils.toObject(hashOk)));
        AutoLog.debug("UNKNOWN: " + EncodeUtils.joinArray(", ", ArrayUtils.toObject(hashUnkn)));

        String stringHashOkEncoded      = EncodeUtils.encodeToString(hashOk, publicKey);
        String stringHashUnknownEncoded = EncodeUtils.encodeToString(hashUnkn, publicKey);

        AutoLog.debug("stringOKENCODED: " + URLEncoder.encode(stringHashOkEncoded, "UTF-8"));
        AutoLog.debug("stringUNKNOWNENCODED: " + URLEncoder.encode(stringHashUnknownEncoded, "UTF-8"));

    }

    private void getPublicKey(final String url)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                String key = EncodeUtils.parsePublicKey(url);
                AutoLog.info(key);
                if (key == null)
                {
                    Toasters.show(getString(R.string.cannot_connect) + " " + url);
                    progress.dismiss();
                    return;
                }
                stringPublicKey = key;
                progress.dismiss();
                try
                {
                    encodeRequest();
                } catch (GeneralSecurityException | UnsupportedEncodingException e)
                {
                    throw new RuntimeException(e);
                }
            }

        });
        thread.start();
        try
        {
            thread.join();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        editText = (EditText) findViewById(R.id.auth_code);
    }
}
