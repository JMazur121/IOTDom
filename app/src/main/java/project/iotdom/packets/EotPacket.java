package project.iotdom.packets;

public class EotPacket extends AbstractPacket {

    public EotPacket() {
        packetHeader = HEADER_EOT;
    }

    @Override
    public byte[] getPacketBytes() {
        byte[] bytes = new byte[1];
        bytes[0] = HEADER_EOT;
        return bytes;
    }

}
