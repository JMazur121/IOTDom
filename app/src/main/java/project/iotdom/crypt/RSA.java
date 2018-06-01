package project.iotdom.crypt;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import project.iotdom.R;

public class RSA {

    private static RSA instance = null;

    private PublicKey publicKey;

    private RSA() { }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public static RSA getInstance() {
        if (instance == null)
            instance = new RSA();
        return instance;
    }

    public void generateKey(byte[] keyBytes) throws Exception {
        String keyString = new String(keyBytes);

        keyString = keyString.replace("-----BEGIN PUBLIC KEY-----\n", "");
        keyString = keyString.replace("-----END PUBLIC KEY-----", "");
        byte [] encoded = Base64.decode(keyString, Base64.DEFAULT);
        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encoded);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        publicKey = kf.generatePublic(encodedKeySpec);
    }

    public byte[] encryptData(byte[] toEncrypt, int offset) {
        byte[] encryptedData = new byte[256];
        System.arraycopy(toEncrypt,offset,encryptedData,0,toEncrypt.length-offset);
        return encryptedData;
    }

    public boolean verifySign(byte[] data) {
        return true;
    }
}
