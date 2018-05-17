package project.iotdom;

import org.junit.Test;

import java.security.SecureRandom;
import java.util.Arrays;

import project.iotdom.crypt.AES;

import static org.junit.Assert.*;

public class AESTests {
    AES aes = AES.getInstance();

    @Test
    public void encryptedLengthEqualsExpected() {

        //10 chars, encrypted should contain 32 (10 + 6 padding + 16 IV)
        byte[] plain1 = "abcdefghij".getBytes();
        try {
            byte[] encrypted = aes.encryptBytes(plain1);
            assertEquals(32,encrypted.length);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }
        //2 chars, encrypted should contain 32 (2 + 14 padding + 16 IV)
        byte[] plain2 = "ab".getBytes();
        try {
            byte[] encrypted = aes.encryptBytes(plain2);
            assertEquals(32,encrypted.length);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }
        //16 chars, encrypted should contain 48 (16 + 16 PKCS padding + 16 IV)
        byte[] plain3 = "aaaabbbbccccdddd".getBytes();
        try {
            byte[] encrypted = aes.encryptBytes(plain3);
            assertEquals(48,encrypted.length);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }
    }

    @Test
    public void pkcsPaddingLengthEqualsExpected() {
        assertEquals(AES.getLengthWithPKCSPadding(1),16);
        assertEquals(AES.getLengthWithPKCSPadding(2),16);
        assertEquals(AES.getLengthWithPKCSPadding(15),16);
        assertEquals(AES.getLengthWithPKCSPadding(16),32);
        assertEquals(AES.getLengthWithPKCSPadding(17),32);
        assertEquals(AES.getLengthWithPKCSPadding(32),48);
        assertEquals(AES.getLengthWithPKCSPadding(35),48);
    }

    @Test
    public void decryptedLengthEqualsPlainLength() {
        byte[] plain1 = "abcdef".getBytes();
        byte[] encrypted1 = null;
        byte[] iv = new byte[16];
        byte[] toDecrypt = new byte[16];
        byte[] decrypted1 = null;

        try {
            encrypted1 = aes.encryptBytes(plain1);
            assertEquals(32,encrypted1.length);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }

        try {
            System.arraycopy(encrypted1,encrypted1.length-16,iv,0,16);
            System.arraycopy(encrypted1,0,toDecrypt,0,16);
            decrypted1 = aes.decryptBytes(toDecrypt,iv);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }

        assertEquals(plain1.length,decrypted1.length);

        //try when plainText is multiple of blockSize
        plain1 = "1234567890abcdef".getBytes();
        toDecrypt = new byte[32];

        try {
            encrypted1 = aes.encryptBytes(plain1);
            assertEquals(48,encrypted1.length);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }

        try {
            System.arraycopy(encrypted1,encrypted1.length-16,iv,0,16);
            System.arraycopy(encrypted1,0,toDecrypt,0,32);
            decrypted1 = aes.decryptBytes(toDecrypt,iv);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }

        assertEquals(plain1.length,decrypted1.length);
    }

    @Test
    public void decryptedEqualsPlain() {
        byte[] plain1 = "abcdef".getBytes();
        byte[] encrypted1 = null;
        byte[] iv = new byte[16];
        byte[] toDecrypt = new byte[16];
        byte[] decrypted1 = null;

        try {
            encrypted1 = aes.encryptBytes(plain1);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }

        try {
            System.arraycopy(encrypted1,encrypted1.length-16,iv,0,16);
            System.arraycopy(encrypted1,0,toDecrypt,0,16);
            decrypted1 = aes.decryptBytes(toDecrypt,iv);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }

        assertTrue(Arrays.equals(plain1,decrypted1));

        //try when plainText is multiple of blockSize
        plain1 = "1234567890abcdef".getBytes();
        toDecrypt = new byte[32];

        try {
            encrypted1 = aes.encryptBytes(plain1);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }

        try {
            System.arraycopy(encrypted1,encrypted1.length-16,iv,0,16);
            System.arraycopy(encrypted1,0,toDecrypt,0,32);
            decrypted1 = aes.decryptBytes(toDecrypt,iv);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }

        assertTrue(Arrays.equals(plain1,decrypted1));
    }

    @Test
    public void encryptedTwoTimesAndIVIsDifferent() {
        byte[] plain1 = "abcdef".getBytes();
        byte[] encrypted1 = null;
        byte[] encrypted2 = null;
        byte[] iv1 = new byte[16];
        byte[] iv2 = new byte[16];

        try {
            encrypted1 = aes.encryptBytes(plain1);
            encrypted2 = aes.encryptBytes(plain1);
            System.arraycopy(encrypted1,encrypted1.length-16,iv1,0,16);
            System.arraycopy(encrypted2,encrypted2.length-16,iv2,0,16);
        } catch (Exception e) {
            fail("Exception thrown " + e.getMessage());
        }

        assertFalse(Arrays.equals(iv1,iv2));
    }

    @Test
    public void generatedNewKeyAndKeyChanged() {
        byte[] key1 = aes.getKey();
        aes.generateNewKey();
        byte[] key2 = aes.getKey();

        assertFalse(Arrays.equals(key1,key2));
    }
}
