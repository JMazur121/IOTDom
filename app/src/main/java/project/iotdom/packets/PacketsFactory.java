package project.iotdom.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PacketsFactory {

    public static int byteToUnsignedInt(byte val) {
        return ((int) val) & 0xff;
    }

    public static AbstractPacket buildPacketFromBytes(ByteBuffer packetBytes) {
        if (packetBytes.hasRemaining()) {
            int received = packetBytes.remaining();
            switch (packetBytes.get()) {//remaining--
                case AbstractPacket.HEADER_ACK: {
                    if (received == AbstractPacket.expectedLength(AbstractPacket.HEADER_ACK))
                        return new AckPacket(packetBytes.get());
                    return null;
                }
                case AbstractPacket.HEADER_NAK: {
                    if (received == AbstractPacket.expectedLength(AbstractPacket.HEADER_NAK))
                        return new NakPacket(packetBytes.get());
                    return null;
                }
                case AbstractPacket.HEADER_EOT: {
                    if (received == AbstractPacket.expectedLength(AbstractPacket.HEADER_EOT))
                        return new EotPacket();
                    return null;
                }
                //length of DESC can be arbitrary, but shouldn't be smaller than 15 bytes
                case AbstractPacket.HEADER_DESC: {
                    if (received < 16)
                        return null;
                    byte deviceClass = packetBytes.get();
                    if (byteToUnsignedInt(deviceClass) > 3)
                        return null;
                    byte serviceID = packetBytes.get();
                    int nameLength = received - 15;
                    byte[] nameBytes = new byte[nameLength];
                    packetBytes.get(nameBytes, 0, nameLength);
                    String name = new String(nameBytes, StandardCharsets.US_ASCII);
                    byte[] unitNameBytes = new byte[4];
                    packetBytes.get(unitNameBytes, 0, 4);
                    String unitName = new String(unitNameBytes, StandardCharsets.US_ASCII);
                    packetBytes.order(ByteOrder.LITTLE_ENDIAN);
                    float minValue = packetBytes.getFloat();
                    float maxValue = packetBytes.getFloat();
                    return new DescPacket(deviceClass, serviceID, name, unitName, minValue, maxValue);
                }
                case AbstractPacket.HEADER_VAL: {
                    if (received == AbstractPacket.expectedLength(AbstractPacket.HEADER_VAL)) {
                        packetBytes.order(ByteOrder.LITTLE_ENDIAN);
                        byte serviceID = packetBytes.get();
                        float value = packetBytes.getFloat();
                        int unixTimestamp = packetBytes.getInt();
                        return new ValPacket(serviceID, value, unixTimestamp);
                    }
                    return null;
                }
                case AbstractPacket.HEADER_SSID: {
                    if (received == AbstractPacket.expectedLength(AbstractPacket.HEADER_SSID))
                        return new SSIDPacket(packetBytes.get());
                    return null;
                }
                case AbstractPacket.HEADER_CHALL_RESP: {
                    if (received == AbstractPacket.expectedLength(AbstractPacket.HEADER_CHALL_RESP)) {
                        byte[] resp = new byte[256];
                        packetBytes.get(resp);
                        return new ChallRespPacket(resp);
                    }
                    return null;
                }
                default:
                    return null;
            }
        }
        return null;
    }
}
