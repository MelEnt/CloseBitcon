package se.melent.closebitconandroid.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
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
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import se.melent.closebitconandroid.R;
import se.melent.closebitconandroid.extra.AutoLog;
import se.melent.closebitconandroid.extra.Toasters;

public class AuthUserActivity extends AppCompatActivity {

    private EditText editText;
    private String publicKey;
    private byte[] finalKey;
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

    private PublicKey generatePublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decode(publicKey, Base64.DEFAULT));

        return KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
    }

    private void encodeRequest() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException
    {
        String authCode = editText.getText().toString();

        //byte[] requestToken     = new byte[20]; //final
        byte[] protocolBytes    = new byte[]{1, 0, 0, 0};
        byte[] authCodeBytes    = authCode.getBytes();
        byte[] saltBytes        = randomBytes(4);

        byte[] requestToken = concatArrays(protocolBytes, authCodeBytes, saltBytes);
        //insertArray(requestToken, protocolBytes, 0);
        //insertArray(requestToken, authCodeBytes, 4);
        //insertArray(requestToken, saltBytes, 16);

        AutoLog.debug("AuthReq: " + Arrays.toString(requestToken));
        AutoLog.debug("PublicKey: " + publicKey);

        final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, generatePublicKey(publicKey));
        finalKey = cipher.doFinal(requestToken);

        final String cipherText = Base64.encodeToString(finalKey, Base64.DEFAULT);

        AutoLog.debug("publicKey: " + publicKey);
        AutoLog.debug("KeyToSend: " + Arrays.toString(finalKey));

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
                String key = null;
                try
                {
                    Document doc = connectToPage(url);
                    if(doc == null)
                    {
                        runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Toast.makeText(getApplicationContext(), getString(R.string.cannot_connect) + " " + url, Toast.LENGTH_SHORT).show();
                            }
                        });
                        progress.dismiss();
                        return;
                    }
                    key = doc.body().text();
                    key = key.replace("-----BEGIN PUBLIC KEY----- ", "");
                    key = key.replace(" -----END PUBLIC KEY-----", "");

                } catch (IOException e)
                {
                    e.printStackTrace();
                }
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

    private Document connectToPage(String url) throws IOException
    {
        Connection.Response response = Jsoup.connect(url).execute();
        if(response.statusCode() == 200)
        {
            return response.parse();
        }
        return null;
    }

	private byte[] concatArrays(byte[]... byteArrays)
	{
		int length = 0;
		for (byte[] bytes : byteArrays)
		{
			length += length;
		}
		byte[] result = new byte[length];
		int index = 0;
		for (byte[] bytes : byteArrays)
		{
			insertArray(result,bytes,index);
			index += bytes.length;
		}
		return result;
	}

    private void insertArray(byte[] bytes, byte[] src, int index)
    {
        for(int i=0;i<src.length-1;i++)
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
