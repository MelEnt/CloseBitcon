package se.melent.closebitconandroid.extra;

import android.util.Base64;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by MelEnt on 2016-05-24.
 */
public class EncodeUtils
{
    private static byte[] finalKey;

    public static String encodeToString(byte[] inputArray, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException
    {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        finalKey = cipher.doFinal(inputArray);

        return Base64.encodeToString(finalKey, Base64.DEFAULT);
    }

    public static PublicKey generatePublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Base64.decode(publicKey, Base64.DEFAULT));

        return KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);
    }

    public static byte[] randomBytes(int count)
    {
        Random random = new SecureRandom();
        byte[] bytes = new byte[count];
        random.nextBytes(bytes);

        return bytes;
    }

    public static String parsePublicKey(String url)
    {
        String key = null;
        try
        {
            Document result = connectToPage(url);
            key = result.body().text();
            key = key.replace("-----BEGIN PUBLIC KEY----- ", "");
            key = key.replace(" -----END PUBLIC KEY-----", "");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return key;
    }

    private static Document connectToPage(String url) throws IOException
    {
        Connection.Response response = Jsoup.connect(url).execute();
        if(response.statusCode() == 200)
        {
            return response.parse();
        }
        return null;
    }

    public static byte[] concatArrays(byte[]... byteArrays)
    {
        int length = 0;
        for (byte[] bytes : byteArrays)
        {
            length += bytes.length;
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

    private static void insertArray(byte[] bytes, byte[] src, int index)
    {
        for(int i=0;i<src.length-1;i++)
        {
            bytes[index+i] = src[i];
        }
    }




}
