package project.iotdom.packets;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SetPacket extends AbstractPacket {

    private byte serviceID;
    private float value;

    public SetPacket(byte serviceID, float value) {
        this.serviceID = serviceID;
        this.value = value;
        packetHeader = HEADER_SET;
    }

    @Override
    public byte[] getPacketBytes() {
        byte[] bytes = new byte[6];
        bytes[0] = HEADER_SET;
        bytes[1] = serviceID;
        ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        byte[] floatBytes = buf.putFloat(value).array();
        System.arraycopy(floatBytes,0,bytes,2,4);
        return bytes;
    }
}
