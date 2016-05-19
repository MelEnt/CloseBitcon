package se.melent.closebitconandroid;

import android.util.Log;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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

import se.melent.closebitconandroid.extra.AutoLog;

/**
 * Created by MelEnt on 2016-05-18.
 */
public class BullshitbeaconTester
{
    private String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDI1Iy41Srk8sxd21Qywbk/28EW oRrgun4wMwle0uK4dJ3nXsW3HF9wzhUDZX6sGzV2hgmbpIKwiNjaZ35b5DwzDUow cJLhM+K+S+VLx+63uEpdJ/SiuqvDNKlIIl1lXYqahop6unXW6UE5jDdHfJ4Yl1Yi 0YVGWdA6zBxRGsLwgQIDAQAB";
    private byte[] finalKey;

    public BullshitbeaconTester() throws NoSuchPaddingException, DecoderException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidKeyException, InvalidKeySpecException
    {
        encodeRequest();
    }

    private PublicKey generatePublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException, DecoderException
    {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(android.util.Base64.decode(publicKey, android.util.Base64.DEFAULT));
        PublicKey pubKey = KeyFactory.getInstance("RSA").generatePublic(x509EncodedKeySpec);

        return pubKey;
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
        for(int i=0;i<src.length-1;i++)
        {
            bytes[index+i] = src[i];
        }
    }

    private void encodeRequest() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, DecoderException, UnsupportedEncodingException
    {
        String authCode = "whnmoX2N2JSx";

        byte[] requestToken = new byte[20]; //final
        byte[] protocolBytes = new byte[]{1, 0, 0, 0};
        byte[] authCodeBytes = authCode.getBytes();
        byte[] saltBytes = randomBytes(4);

        insertArray(requestToken, protocolBytes, 0);
        insertArray(requestToken, authCodeBytes, 4);
        insertArray(requestToken, saltBytes, 16);

        AutoLog.debug("AuthReq: "+ Arrays.toString(requestToken));
        AutoLog.debug("PublicKey; " + Arrays.toString(publicKey.getBytes()));

        final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, generatePublicKey(publicKey));
        finalKey = cipher.doFinal(requestToken);

        final String cipherText = URLEncoder.encode(android.util.Base64.encodeToString(finalKey, android.util.Base64.DEFAULT), "UTF-8");

        System.out.println(cipherText);

    }
}