package project.iotdom.connection;

import project.iotdom.packets.AbstractPacket;

public abstract class AbstractMessage {

    public static final byte plainMessage = 0x00;

    public static final byte encryptedMessage = 0x01;

    protected byte[] messageBytes;

    public abstract byte[] getBytes();
}
