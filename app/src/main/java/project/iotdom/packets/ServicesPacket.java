package project.iotdom.packets;

public class ServicesPacket extends AbstractPacket {

    public ServicesPacket() {
        packetHeader = HEADER_SERVICES;
    }

    @Override
    public byte[] getPacketBytes() {
        byte[] bytes = new byte[1];
        bytes[0] = HEADER_SERVICES;
        return bytes;
    }
}
