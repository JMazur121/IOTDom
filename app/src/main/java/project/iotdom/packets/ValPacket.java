package project.iotdom.packets;

public class ValPacket extends AbstractPacket {

    private byte serviceID;
    private float value;
    private int unixTimestamp;

    public ValPacket(byte serviceID, float value, int unixTimestamp) {
        this.serviceID = serviceID;
        this.value = value;
        this.unixTimestamp = unixTimestamp;
        this.packetHeader = HEADER_VAL;
    }

    public byte getServiceID() {
        return serviceID;
    }

    public float getValue() {
        return value;
    }

    public int getUnixTimestamp() {
        return unixTimestamp;
    }

    @Override
    public byte[] getPacketBytes() {
        return new byte[0];
    }
}
