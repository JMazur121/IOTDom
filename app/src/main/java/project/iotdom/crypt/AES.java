package project.iotdom.crypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    private static AES instance = null;
    private SecretKeySpec secretKey;
    private IvParameterSpec ivps;
    private byte[] key;
    private byte[] iv;
    private Cipher cipher;
    private SecureRandom secureRandom;

    private AES() {
        secureRandom = new SecureRandom();
        key = new byte[16];
        iv = new byte[16];
        secureRandom.nextBytes(key);
        secretKey = new SecretKeySpec(key, "AES");
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public byte[] getKey() {
        return key;
    }

    public static int getLengthWithPKCSPadding(int plainLength) {
        int padding = 16 - (plainLength % 16);
        return (padding + plainLength);
    }

    /**
     * Method for encrypting given array of bytes. Creates new IV and encrypt given array
     * using generated key and IV in CBC mode.
     * @param toEncrypt array of bytes to encrypt
     * @return encrypted bytes + IV
     */
    public byte[] encryptBytes(byte[] toEncrypt) {
        secureRandom.nextBytes(iv);
        ivps = new IvParameterSpec(iv);
        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKey,ivps);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        byte[] encrypted = new byte[cipher.getOutputSize(toEncrypt.length) + 16];
        try {
            cipher.doFinal(toEncrypt,0,toEncrypt.length,encrypted,0);
        } catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        System.arraycopy(iv,0,encrypted,encrypted.length-16,16);
        return encrypted;
    }

    public byte[] decryptBytes(byte[] toDecrypt, byte[] dec_iv) {
        ivps = new IvParameterSpec(dec_iv);
        try {
            cipher.init(Cipher.DECRYPT_MODE,secretKey,ivps);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        try {
            return cipher.doFinal(toDecrypt);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void generateNewKey() {
        key = new byte[16];
        secureRandom.nextBytes(key);
        secretKey = new SecretKeySpec(key, "AES");
    }

    public static AES getInstance() {
        if (instance == null)
            instance = new AES();
        return instance;
    }

}
