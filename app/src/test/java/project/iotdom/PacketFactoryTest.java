package project.iotdom;

import org.junit.Test;

import java.nio.ByteBuffer;

import project.iotdom.packets.AckPacket;

import static org.junit.Assert.*;

public class PacketFactoryTest {

    @Test
    public void ackCreationTest() {
        AckPacket ackPacket = new AckPacket((byte)0);

        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.put(ackPacket.getPacketBytes());
    }
}
