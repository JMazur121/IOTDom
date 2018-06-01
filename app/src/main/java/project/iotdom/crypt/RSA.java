package project.iotdom.crypt;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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

    public byte[] encryptData(byte[] toEncrypt, int outputOffset) {
        Cipher encryptCipher = null;
        try {
            encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (Exception e) {}
        byte[] encrypted = new byte[encryptCipher.getOutputSize(toEncrypt.length)];
        try {
            encryptCipher.doFinal(toEncrypt, 0, toEncrypt.length, encrypted, outputOffset);
        } catch (Exception e) {}
        return encrypted;
    }

    public boolean verifySign(byte[] signature, byte[] plain) {
        Signature publicSignature = null;
        try {
            publicSignature = Signature.getInstance("SHA1withRSA");
            publicSignature.initVerify(publicKey);
        } catch (Exception e) {}
        try {
            publicSignature.update(plain);
            return publicSignature.verify(signature);
        } catch (SignatureException e) {
            return false;
        }
    }
}
