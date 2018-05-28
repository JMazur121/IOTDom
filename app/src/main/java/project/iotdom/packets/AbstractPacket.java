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

    //lenght of packages from server
    public static final int ACK_LENGTH = 2;
    public static final int NAK_LENGTH = 2;
    public static final int EOT_LENGTH = 1;
    public static final int CHALL_RESP_LENGTH = 257;
    public static final int VAL_LENGTH = 10;
    public static final int SSID_LENGTH = 2;

    protected byte packetHeader;

    public static int expectedLength(byte header) {
        switch (header) {
            case HEADER_ACK: {
                return ACK_LENGTH;
            }
            case HEADER_NAK: {
                return NAK_LENGTH;
            }
            case HEADER_EOT: {
                return EOT_LENGTH;
            }
            case HEADER_CHALL_RESP: {
                return CHALL_RESP_LENGTH;
            }
            case HEADER_VAL: {
                return VAL_LENGTH;
            }
            case HEADER_SSID: {
                return SSID_LENGTH;
            }
            default:
                return -1;
        }
    }

    public abstract byte[] getPacketBytes();

    public byte getPacketHeader() {
        return packetHeader;
    }
}
