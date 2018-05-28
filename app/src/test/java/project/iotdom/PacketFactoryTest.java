package project.iotdom;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Random;

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
        // valid packet
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
    public void descNullTest() {
        //to few bytes and null
        ByteBuffer buffer = ByteBuffer.allocate(10);
        byte[] bytes = new byte[10];
        bytes[0] = AbstractPacket.HEADER_DESC;
        buffer.put(bytes);
        buffer.flip();

        AbstractPacket packet = PacketsFactory.buildPacketFromBytes(buffer);

        assertNull(packet);

        //wrong device class
        buffer.clear();
        bytes[1] = (byte) 0xAB;
        buffer.put(bytes);
        buffer.flip();

        AbstractPacket packet1 = PacketsFactory.buildPacketFromBytes(buffer);

        assertNull(packet1);
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

    @Test
    public void givenEmptyBufferAndNullReturn() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte) 0xFF);
        buffer.flip();
        buffer.get();

        AbstractPacket packet = PacketsFactory.buildPacketFromBytes(buffer);

        assertNull(packet);
    }

    @Test
    public void givenWrongHeaderAndNullReturn() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte)0x0E);
        buffer.flip();

        AbstractPacket packet = PacketsFactory.buildPacketFromBytes(buffer);

        assertNull(packet);
    }

    @Test
    public void givenOnlyHeaderAndNullReturn() {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put(AbstractPacket.HEADER_ACK);
        buffer.flip();

        AbstractPacket packet = PacketsFactory.buildPacketFromBytes(buffer);
        assertNull(packet);

        buffer.clear();
        buffer.put(AbstractPacket.HEADER_NAK);
        buffer.flip();
        AbstractPacket packet1 = PacketsFactory.buildPacketFromBytes(buffer);
        assertNull(packet1);

        buffer.clear();
        buffer.put(AbstractPacket.HEADER_DESC);
        buffer.flip();
        AbstractPacket packet2 = PacketsFactory.buildPacketFromBytes(buffer);
        assertNull(packet2);

        buffer.clear();
        buffer.put(AbstractPacket.HEADER_VAL);
        buffer.flip();
        AbstractPacket packet3 = PacketsFactory.buildPacketFromBytes(buffer);
        assertNull(packet3);

        buffer.clear();
        buffer.put(AbstractPacket.HEADER_SSID);
        buffer.flip();
        AbstractPacket packet4 = PacketsFactory.buildPacketFromBytes(buffer);
        assertNull(packet4);

        buffer.clear();
        buffer.put(AbstractPacket.HEADER_CHALL_RESP);
        buffer.flip();
        AbstractPacket packet5 = PacketsFactory.buildPacketFromBytes(buffer);
        assertNull(packet5);
    }

    @Test
    public void givenMoreThanNeededAndNullReturn() {
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put(AbstractPacket.HEADER_ACK);
        buffer.put((byte)0xFF);
        buffer.put((byte)0xFF);
        buffer.flip();

        AbstractPacket packet = PacketsFactory.buildPacketFromBytes(buffer);
        assertNull(packet);

        buffer.clear();
        buffer.put(AbstractPacket.HEADER_NAK);
        buffer.put((byte)0xFF);
        buffer.put((byte)0xFF);
        buffer.flip();

        AbstractPacket packet1 = PacketsFactory.buildPacketFromBytes(buffer);
        assertNull(packet1);

        buffer.clear();
        buffer.put(AbstractPacket.HEADER_EOT);
        buffer.put((byte)0xFF);
        buffer.put((byte)0xFF);
        buffer.flip();

        AbstractPacket packet2 = PacketsFactory.buildPacketFromBytes(buffer);
        assertNull(packet2);

        buffer.clear();
        buffer.put(AbstractPacket.HEADER_SSID);
        buffer.put((byte)0xFF);
        buffer.put((byte)0xFF);
        buffer.flip();

        AbstractPacket packet3 = PacketsFactory.buildPacketFromBytes(buffer);
        assertNull(packet3);

        ByteBuffer buffer1 = ByteBuffer.allocate(12);
        Random rand = new Random();
        byte[] randomBytes = new byte[12];
        rand.nextBytes(randomBytes);
        randomBytes[0] = AbstractPacket.HEADER_VAL;
        buffer1.put(randomBytes);
        buffer1.flip();

        AbstractPacket packet4 = PacketsFactory.buildPacketFromBytes(buffer1);
        assertNull(packet4);

        ByteBuffer buffer2 = ByteBuffer.allocate(300);
        byte[] randomBytes1 = new byte[300];
        rand.nextBytes(randomBytes);
        randomBytes[0] = AbstractPacket.HEADER_CHALL_RESP;
        buffer2.put(randomBytes1);
        buffer2.flip();

        AbstractPacket packet5 = PacketsFactory.buildPacketFromBytes(buffer1);
        assertNull(packet5);
    }
}
