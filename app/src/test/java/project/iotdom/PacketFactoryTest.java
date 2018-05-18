package project.iotdom;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.AckPacket;
import project.iotdom.packets.DescPacket;
import project.iotdom.packets.NakPacket;
import project.iotdom.packets.PacketsFactory;
import project.iotdom.packets.ValPacket;

import static org.junit.Assert.*;

public class PacketFactoryTest {

    @Test
    public void ackCreationTest() {
        AckPacket ackPacket = new AckPacket((byte)0);

        ByteBuffer buffer = ByteBuffer.allocate(2).put(ackPacket.getPacketBytes());
        buffer.flip();

        AbstractPacket packet = PacketsFactory.buildPacketFromBytes(buffer);

        assertNotNull(packet);
        assertEquals(AbstractPacket.HEADER_ACK,packet.getPacketHeader());
        AckPacket fromFactory = (AckPacket)packet;
        assertEquals((byte)0,fromFactory.getVal());
    }

    @Test
    public void nakCreationTest() {
        NakPacket nakPacket = new NakPacket((byte)0xFF);

        ByteBuffer buffer = ByteBuffer.allocate(2).put(nakPacket.getPacketBytes());
        buffer.flip();

        AbstractPacket packet = PacketsFactory.buildPacketFromBytes(buffer);

        assertNotNull(packet);
        assertEquals(AbstractPacket.HEADER_NAK,packet.getPacketHeader());
        NakPacket fromFactory = (NakPacket)packet;
        assertEquals((byte)0xFF,fromFactory.getVal());
    }

    @Test
    public void descCreationTest() {
        ByteBuffer buffer = ByteBuffer.allocate(19).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(AbstractPacket.HEADER_DESC);
        buffer.put((byte)0x01);
        byte[] nameBytes = "nazwa".getBytes(StandardCharsets.US_ASCII);
        byte[] unitNameBytes = "gram".getBytes(StandardCharsets.US_ASCII);
        buffer.put(nameBytes);
        buffer.put(unitNameBytes);
        buffer.putFloat(0.5f);
        buffer.putFloat(2.5f);

        buffer.flip();

        AbstractPacket packet = PacketsFactory.buildPacketFromBytes(buffer);

        assertNotNull(packet);
        DescPacket fromFactory = (DescPacket)packet;

        assertEquals(AbstractPacket.HEADER_DESC,fromFactory.getPacketHeader());
        assertEquals((byte)0x01,fromFactory.getDeviceClass());
        assertEquals("nazwa",fromFactory.getHumanReadableName());
        assertEquals("gram",fromFactory.getUnitName());
        assertEquals(0.5f,fromFactory.getMinValue(),0.00001f);
        assertEquals(2.5f,fromFactory.getMaxValue(),0.00001f);
    }

    @Test
    public void valCreationTest() {
        ByteBuffer buffer = ByteBuffer.allocate(10).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(AbstractPacket.HEADER_VAL);
        buffer.put((byte)0x05);
        buffer.putFloat(2.5f);
        buffer.putInt(100);

        buffer.flip();

        AbstractPacket packet = PacketsFactory.buildPacketFromBytes(buffer);

        assertNotNull(packet);
        ValPacket fromFactory = (ValPacket)packet;

        assertEquals(AbstractPacket.HEADER_VAL,fromFactory.getPacketHeader());
        assertEquals(fromFactory.getServiceID(),0x05);
        assertEquals(2.5f,fromFactory.getValue(),0.00001f);
        assertEquals(100,fromFactory.getUnixTimestamp());
    }
}
