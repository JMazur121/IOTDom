package project.iotdom.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class PacketsFactory {

    public static AbstractPacket buildPacketFromBytes(ByteBuffer packetBytes) {
        int capacity = packetBytes.capacity();
        switch (packetBytes.get()) {
            case AbstractPacket.HEADER_ACK: {
                return new AckPacket(packetBytes.get());
            }
            case AbstractPacket.HEADER_NAK: {
                return new NakPacket(packetBytes.get());
            }
            case AbstractPacket.HEADER_EOT: {
                return new EotPacket();
            }
            case AbstractPacket.HEADER_DESC: {
                byte deviceClass = packetBytes.get();
                if (deviceClass > 3)
                    return null;
                int nameLength = capacity - 14;
                byte[] nameBytes = new byte[nameLength];
                packetBytes.get(nameBytes,0,nameLength);
                String name = new String(nameBytes, StandardCharsets.US_ASCII);
                byte[] unitNameBytes = new byte[4];
                packetBytes.get(unitNameBytes,0,4);
                String unitName = new String(unitNameBytes, StandardCharsets.US_ASCII);
                packetBytes.order(ByteOrder.LITTLE_ENDIAN);
                float minValue = packetBytes.getFloat();
                float maxValue = packetBytes.getFloat();
                return new DescPacket(deviceClass,name,unitName,minValue,maxValue);
            }
            case AbstractPacket.HEADER_VAL: {
                packetBytes.order(ByteOrder.LITTLE_ENDIAN);
                byte serviceID = packetBytes.get();
                float value = packetBytes.getFloat();
                int unixTimestamp = packetBytes.getInt();
                return new ValPacket(serviceID,value,unixTimestamp);
            }
            case AbstractPacket.HEADER_SSID: {
                return new SSIDPacket(packetBytes.get());
            }
            default:
                return null;
        }
    }
}
