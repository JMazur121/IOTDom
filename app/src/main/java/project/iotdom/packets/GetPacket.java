package project.iotdom.packets;

public class GetPacket extends AbstractPacket {

    private byte serviceID;

    public GetPacket(byte serviceID) {
        this.serviceID = serviceID;
        this.packetHeader = HEADER_GET;
    }

    @Override
    public byte[] getPacketBytes() {
        byte[] bytes = new byte[2];
        bytes[0] = HEADER_GET;
        bytes[1] = serviceID;
        return bytes;
    }
}
