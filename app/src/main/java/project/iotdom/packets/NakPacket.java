package project.iotdom.packets;

public class NakPacket extends AbstractPacket {

    private byte val;

    public NakPacket(byte val) {
        this.val = val;
        packetHeader = HEADER_NAK;
    }

    public byte getVal() {
        return val;
    }

    @Override
    public byte[] getPacketBytes() {
        byte[] bytes = new byte[2];
        bytes[0] = HEADER_NAK;
        bytes[1] = val;
        return bytes;
    }

}
