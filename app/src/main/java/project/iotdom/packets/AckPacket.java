package project.iotdom.packets;

public class AckPacket extends AbstractPacket {

    private byte val;

    public AckPacket(byte val) {
        this.val = val;
        packetHeader = HEADER_ACK;
    }

    public byte getVal() {
        return val;
    }

    @Override
    public byte[] getPacketBytes() {
        byte[] packetBytes = new byte[2];
        packetBytes[0] = HEADER_ACK;
        packetBytes[1] = val;
        return packetBytes;
    }

}
