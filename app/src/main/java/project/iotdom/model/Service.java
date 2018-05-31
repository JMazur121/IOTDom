package project.iotdom.model;

import project.iotdom.packets.DescPacket;

public class Service {

     public static final int ANALOG_IN = 0x00, ANALOG_OUT = 0x01, DIGITAL_IN = 0x02, DIGITAL_OUT = 0x03;

     private int deviceClass;
     private byte serviceID;
     private String humanReadableName;
     private String unitName;
     private float minValue;
     private float maxValue;
     private float currentValue;

    public Service(DescPacket packet, float currentValue) {
        deviceClass = getDeviceClassAsInt(packet.getDeviceClass());
        serviceID = packet.getServiceID();
        humanReadableName = packet.getHumanReadableName();
        unitName = packet.getUnitName();
        minValue = packet.getMinValue();
        maxValue = packet.getMaxValue();
        this.currentValue = currentValue;
    }

    public boolean isInBoundary(float value) {
        int retval = Float.compare(value,maxValue);
        if (retval > 0)
            return false;
        retval = Float.compare(value,minValue);
        return retval >= 0;
    }

    private int getDeviceClassAsInt(byte val) {
        return ((int) val) & 0xff;
    }

    public int getDeviceClass() {
        return deviceClass;
    }

    public byte getServiceID() {
        return serviceID;
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

    public float getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
    }
}
