package project.iotdom.packets;

public class SSIDPacket extends AbstractPacket {

    private byte ssid;

    public SSIDPacket(byte ssid) {
        this.ssid = ssid;
        packetHeader = HEADER_SSID;
    }

    public byte getSsid() {
        return ssid;
    }

    @Override
    public byte[] getPacketBytes() {
        byte[] bytes = new byte[2];
        bytes[0] = HEADER_SSID;
        bytes[1] = ssid;
        return bytes;
    }
}
