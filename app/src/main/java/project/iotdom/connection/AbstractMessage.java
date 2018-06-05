package project.iotdom.connection;

import project.iotdom.packets.AbstractPacket;

public abstract class AbstractMessage {

    public static final byte plainMessage = 0x00;

    public static final byte encryptedMessage = 0x01;

    protected byte[] messageBytes;

    public abstract byte[] getBytes();

    public static String bytesToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes) {
            sb.append(String.format("%02x", b&0xff));
        }
        return sb.toString();
    }

}
