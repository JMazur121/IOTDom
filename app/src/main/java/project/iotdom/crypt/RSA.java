package project.iotdom.crypt;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import project.iotdom.R;

public class RSA {

    private static RSA instance = null;

    private PublicKey publicKey;
    private RSAPrivateKey privateKey;

    private RSA() { }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public static RSA getInstance() {
        if (instance == null)
            instance = new RSA();
        return instance;
    }

    public void generatePrivateKey(byte[] keyBytes) throws Exception {
        String privateKeyPEM = new String(keyBytes);
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");
        privateKeyPEM = privateKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");
        byte [] encoded = Base64.decode(privateKeyPEM, Base64.DEFAULT);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        privateKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
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

    public byte[] decryptData(byte[] toDecrypt) {
        Cipher decryptCipher = null;
        try {
            decryptCipher = Cipher.getInstance("RSA/ECB/NoPadding");
            decryptCipher.init(Cipher.DECRYPT_MODE,privateKey);
        } catch (Exception e) {}
        byte[] decrypted = null;
        try {
            decrypted = decryptCipher.doFinal(toDecrypt);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return decrypted;
    }

    public byte[] encryptData(byte[] toEncrypt, int outputOffset) {
        Cipher encryptCipher = null;
        try {
            encryptCipher = Cipher.getInstance("RSA/ECB/NoPadding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } catch (Exception e) {}
        byte[] encrypted = new byte[encryptCipher.getOutputSize(toEncrypt.length)];
        Log.i("rsaencrypt","Dlugosc zaszyfrwangeo RSA : "+String.valueOf(encrypted.length));
        try {
            encryptCipher.doFinal(toEncrypt, 0, toEncrypt.length, encrypted, outputOffset);
        } catch (Exception e) {
            Log.i("rsacryptexception","Polecial wyjatek przy enkrypcji rsa");
        }
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
