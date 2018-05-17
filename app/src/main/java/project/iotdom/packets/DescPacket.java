package project.iotdom.packets;

public class DescPacket extends AbstractPacket {

    private byte deviceClass;
    private String humanReadableName;
    private String unitName;
    private float minValue;
    private float maxValue;

    public DescPacket(byte deviceClass, String humanReadableName, String unitName, float minValue, float maxValue) {
        this.deviceClass = deviceClass;
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

    @Override
    public byte[] getPacketBytes() {
        return new byte[0];
    }
}
