package project.iotdom.packets;

public abstract class AbstractPacket {

    //definitions of headers
    public static final byte HEADER_ACK = 0x01;
    public static final byte HEADER_NAK = 0x02;
    public static final byte HEADER_EOT = 0x03;
    public static final byte HEADER_CHALL = 0x04;
    public static final byte HEADER_CHALL_RESP = 0x05;
    public static final byte HEADER_KEY = 0x06;
    public static final byte HEADER_DESC = 0x07;
    public static final byte HEADER_VAL = 0x08;
    public static final byte HEADER_GET = 0x09;
    public static final byte HEADER_SET = 0x0A;
    public static final byte HEADER_SSID = 0x0B;
    public static final byte HEADER_LOG = 0x0C;
    public static final byte HEADER_SERVICES = 0x0D;

    protected byte packetHeader;

    public abstract byte[] getPacketBytes();

    public byte getPacketHeader() {
        return packetHeader;
    }
}
