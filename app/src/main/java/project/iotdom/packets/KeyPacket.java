package project.iotdom.packets;

public class KeyPacket extends AbstractPacket {

    private byte[] keyBytes;

    public KeyPacket(byte[] key) {
        keyBytes = new byte[17];
        System.arraycopy(key,0,keyBytes,1,16);
        packetHeader = HEADER_KEY;
        keyBytes[0] = HEADER_KEY;
    }

    @Override
    public byte[] getPacketBytes() {
        return keyBytes;
    }

}
