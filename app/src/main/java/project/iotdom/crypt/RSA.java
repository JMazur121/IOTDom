package project.iotdom.crypt;

public class RSA {

    private static RSA instance = null;

    private RSA() {

    }

    public static RSA getInstance() {
        if (instance == null)
            instance = new RSA();
        return instance;
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
