package se.melent.closebitconandroid;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class BeaconFormActivity extends AppCompatActivity {

    private static final String TAG = BeaconFormActivity.class.getSimpleName();
    private EditText editText;
    private String publicKey;
    private byte[] finalKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_form);
    }

    public void submit(View view) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        String authCode = editText.getText().toString();
        getPublicKey("http://smartsensor.io/CBtest/getpubkey.php");
        if(authCode.length() == 12)
        {
            Cipher cipher       = Cipher.getInstance("RSA");
            Log.d(TAG, "publicKey.getBytes: " + publicKey.getBytes(Charset.forName("UTF-8")));
            Log.d(TAG, "publicKey.toString: " + publicKey); 
            SecretKey secretKey = new SecretKeySpec(publicKey.getBytes(), "RSA");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey); //CAUSES InvalidKeyException due to publicKey.getBytes returns [B@someHashCode and not the public key in binary format
//            byte[] publicKeyEncoded = Base64.encode(publicKey.getBytes(), Base64.URL_SAFE); //final

            byte[] requestToken     = new byte[20]; //final
            byte[] protocolBytes    = new byte[]{0,0,0,1};
            byte[] authCodeBytes    = authCode.getBytes();
            byte[] saltBytes        = randomBytes(4);

            insertArray(requestToken, protocolBytes, 0);
            insertArray(requestToken, authCodeBytes, 4);
            insertArray(requestToken, saltBytes, 16);

            //request token ciphered with public key
            byte[] cipheredRequestToken = cipher.doFinal(requestToken);
            Log.d(TAG, "Ciphered request token: " + cipheredRequestToken);


            //cipheredreqtoken encoded with base64
            final String encodedRequestToken = Base64.encodeToString(cipheredRequestToken, Base64.URL_SAFE);

            Log.d(TAG, "Encoded, ciphered request token: " + encodedRequestToken);

            new Thread(new Runnable() {
                @Override
                public void run()
                {
                    Connection connection = Jsoup.connect("http://smartsensor.io/CBtest/auth_user.php");
                    connection.data("enc", encodedRequestToken);
                    Log.d(TAG, "Final key to send as String: " + encodedRequestToken);
                    Document result = null;
                    try
                    {
                        result = connection.get();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Log.d(TAG, result.toString());

                }
            }).start();

        }
    }

    private String byteToString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes)
        {
            sb.append(b);
        }
        return sb.toString();
    }

    private void getPublicKey(final String url)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                String key = null;
                try
                {
                    key = Jsoup.connect(url).get().body().text();
                    key = key.replace("-----BEGIN PUBLIC KEY----- ", "");
                    key = key.replace(" -----END PUBLIC KEY-----", "");

                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                publicKey = key;
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

    private byte[] randomBytes(int count)
    {
        Random random = new SecureRandom();
        byte[] bytes = new byte[count];
        random.nextBytes(bytes);

        return bytes;
    }

    private void insertArray(byte[] bytes, byte[] src, int index)
    {
        for(int i=0;i<src.length;i++)
        {
            bytes[index+i] = src[i];
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        editText = (EditText) findViewById(R.id.auth_code);
    }
}
