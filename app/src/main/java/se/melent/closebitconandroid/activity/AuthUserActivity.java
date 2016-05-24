package se.melent.closebitconandroid.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import se.melent.closebitconandroid.R;
import se.melent.closebitconandroid.extra.AutoLog;
import se.melent.closebitconandroid.extra.EncodeUtils;
import se.melent.closebitconandroid.extra.Toasters;

public class AuthUserActivity extends AppCompatActivity {

    private EditText editText;
    private String publicKey;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_user);
        Toasters.setContext(this);
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
        String authCode = editText.getText().toString();

        byte[] protocolBytes    = new byte[]{1, 0, 0, 0};
        byte[] authCodeBytes    = authCode.getBytes();
        byte[] saltBytes        = EncodeUtils.randomBytes(4);

        byte[] requestToken = EncodeUtils.concatArrays(protocolBytes, authCodeBytes, saltBytes);

        final String cipherText = EncodeUtils.encodeToString(requestToken, EncodeUtils.generatePublicKey(publicKey));

        AutoLog.debug("publicKey: " + publicKey);

        new Thread(new Runnable() {
            @Override
            public void run()
            {
                Connection connection = Jsoup.connect("http://smartsensor.io/CBtest/auth_user.php");
                connection.data("enc", cipherText); //Jsoup does automatic URLEncoding (utf-8) to connection.data values
                AutoLog.debug("Final key to send as String: " + cipherText);
                Document result = null;
                try
                {
                    result = connection.get();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if(result.body().text().equals("ERROR"))
                {
                    Toasters.show("Failed to auth user!");
                    return;
                }

                String sha1Code = result.body().text().replace("=", "");
                AutoLog.debug(sha1Code);
                Intent intent = new Intent(AuthUserActivity.this, BeaconFormActivity.class);
                intent.putExtra("SHA1", sha1Code);
                startActivity(intent);


            }
        }).start();
    }

    private void getPublicKey(final String url)
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run()
            {
                String key = EncodeUtils.parsePublicKey(url);

                if (key == null)
                {
                    Toasters.show(getString(R.string.cannot_connect) + " " + url);

                    publicKey = key;
                    progress.dismiss();
                    try
                    {
                        encodeRequest();
                    } catch (GeneralSecurityException | UnsupportedEncodingException e)
                    {
                        throw new RuntimeException(e);
                    }
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
