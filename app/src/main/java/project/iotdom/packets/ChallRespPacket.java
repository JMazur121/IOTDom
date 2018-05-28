package project.iotdom.packets;

public class ChallRespPacket extends AbstractPacket {

    private byte[] responseBytes;

    public ChallRespPacket(byte[] responseBytes) {
        this.responseBytes = responseBytes;
        packetHeader = HEADER_CHALL_RESP;
    }

    @Override
    public byte[] getPacketBytes() {
        return responseBytes;
    }
}
