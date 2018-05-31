package project.iotdom.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

public class DescPacket extends AbstractPacket {

    private byte deviceClass;
    private byte serviceID;
    private String humanReadableName;
    private String unitName;
    private float minValue;
    private float maxValue;

    public DescPacket(byte deviceClass, byte id, String humanReadableName, String unitName, float minValue, float maxValue) {
        this.deviceClass = deviceClass;
        this.serviceID = id;
        this.humanReadableName = humanReadableName;
        this.unitName = unitName;
        this.minValue = minValue;
        this.maxValue = maxValue;
        packetHeader = HEADER_DESC;
    }

    public byte getDeviceClass() {
        return deviceClass;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public String getUnitName() {
        return unitName;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public byte getServiceID() {return serviceID;}

    @Override
    public byte[] getPacketBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(1+1+1+humanReadableName.length()+(3*4));
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(HEADER_DESC);
        buffer.put(deviceClass);
        buffer.put(serviceID);
        buffer.put(humanReadableName.getBytes(StandardCharsets.US_ASCII));
        buffer.put(unitName.getBytes(StandardCharsets.US_ASCII));
        buffer.putFloat(minValue);
        buffer.putFloat(maxValue);
        return buffer.array();
    }
}
