package project.iotdom;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import project.iotdom.crypt.AES;
import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.AckPacket;
import project.iotdom.packets.ChallPacket;
import project.iotdom.packets.EotPacket;
import project.iotdom.packets.GetPacket;
import project.iotdom.packets.KeyPacket;
import project.iotdom.packets.LogPacket;
import project.iotdom.packets.NakPacket;
import project.iotdom.packets.ServicesPacket;
import project.iotdom.packets.SetPacket;

import static org.junit.Assert.*;

public class PacketsToBytesTest {
    @Test
    public void ackPacketTest() {
        AckPacket ackPacket = new AckPacket((byte)0);
        byte[] packetBytes = ackPacket.getPacketBytes();

        assertEquals(2,packetBytes.length);
        assertEquals(AbstractPacket.HEADER_ACK,packetBytes[0]);
        assertEquals((byte)0,packetBytes[1]);

        AckPacket ackPacket1 = new AckPacket((byte)0xFF);
        byte[] packetBytes1 = ackPacket1.getPacketBytes();

        assertEquals(2,packetBytes1.length);
        assertEquals(AbstractPacket.HEADER_ACK,packetBytes1[0]);
        assertEquals((byte)0xFF,packetBytes1[1]);

        AckPacket ackPacket2 = new AckPacket((byte)0xAB);
        byte[] packetBytes2 = ackPacket2.getPacketBytes();

        assertEquals(2,packetBytes2.length);
        assertEquals(AbstractPacket.HEADER_ACK,packetBytes2[0]);
        assertEquals((byte)0xAB,packetBytes2[1]);
    }

    @Test
    public void nakPacketTest() {
        NakPacket nakPacket = new NakPacket((byte)0);
        byte[] packetBytes = nakPacket.getPacketBytes();

        assertEquals(2,packetBytes.length);
        assertEquals(AbstractPacket.HEADER_NAK,packetBytes[0]);
        assertEquals((byte)0,packetBytes[1]);

        NakPacket nakPacket1 = new NakPacket((byte)0xFF);
        byte[] packetBytes1 = nakPacket1.getPacketBytes();

        assertEquals(2,packetBytes1.length);
        assertEquals(AbstractPacket.HEADER_NAK,packetBytes1[0]);
        assertEquals((byte)0xFF,packetBytes1[1]);

        NakPacket nakPacket2 = new NakPacket((byte)0xAB);
        byte[] packetBytes2 = nakPacket2.getPacketBytes();

        assertEquals(2,packetBytes2.length);
        assertEquals(AbstractPacket.HEADER_NAK,packetBytes2[0]);
        assertEquals((byte)0xAB,packetBytes2[1]);
    }

    @Test
    public void eotPacketTest() {
        EotPacket eot = new EotPacket();
        byte[] packetBytes = eot.getPacketBytes();

        assertEquals(1,packetBytes.length);
        assertEquals(AbstractPacket.HEADER_EOT,packetBytes[0]);
    }

    @Test
    public void challPacketTest() {
        ChallPacket challPacket = new ChallPacket();
        byte[] packetBytes = challPacket.getPacketBytes();

        assertEquals(9,packetBytes.length);
        assertEquals(AbstractPacket.HEADER_CHALL,packetBytes[0]);
    }

    @Test
    public void getPacketTest() {
        GetPacket getPacket = new GetPacket((byte)0);
        byte[] packetBytes = getPacket.getPacketBytes();

        assertEquals(2,packetBytes.length);
        assertEquals(AbstractPacket.HEADER_GET,packetBytes[0]);
        assertEquals((byte)0,packetBytes[1]);

        GetPacket getPacket1 = new GetPacket((byte)0xFF);
        byte[] packetBytes1 = getPacket1.getPacketBytes();

        assertEquals(2,packetBytes1.length);
        assertEquals(AbstractPacket.HEADER_GET,packetBytes1[0]);
        assertEquals((byte)0xFF,packetBytes1[1]);

        GetPacket getPacket2 = new GetPacket((byte)0xAB);
        byte[] packetBytes2 = getPacket2.getPacketBytes();

        assertEquals(2,packetBytes2.length);
        assertEquals(AbstractPacket.HEADER_GET,packetBytes2[0]);
        assertEquals((byte)0xAB,packetBytes2[1]);
    }

    @Test
    public void keyPacketTest() {
        AES aes = AES.getInstance();

        KeyPacket keyPacket = new KeyPacket(aes.getKey());
        byte[] packetBytes = keyPacket.getPacketBytes();

        assertEquals(17,packetBytes.length);
        assertEquals(AbstractPacket.HEADER_KEY,packetBytes[0]);
    }

    @Test
    public void logPacketTest() {
        LogPacket logPacket = new LogPacket("abcdef","ghijkl");
        byte[] packetBytes = logPacket.getPacketBytes();
        byte[] login = "abcdef".getBytes(StandardCharsets.US_ASCII);
        byte[] password = "ghijkl".getBytes(StandardCharsets.US_ASCII);

        assertEquals(63,packetBytes.length);
        assertEquals(AbstractPacket.HEADER_LOG,packetBytes[0]);

        byte[] loginFromPacket = new byte[6];
        byte[] passwordFromPacket = new byte[6];

        System.arraycopy(packetBytes,1,loginFromPacket,0,6);
        System.arraycopy(packetBytes,32,passwordFromPacket,0,6);

        assertTrue(Arrays.equals(login,loginFromPacket));
        assertTrue(Arrays.equals(password,passwordFromPacket));
    }

    @Test
    public void servicesPacketTest() {
        ServicesPacket servicesPacket = new ServicesPacket();
        byte[] packetBytes = servicesPacket.getPacketBytes();

        assertEquals(1,packetBytes.length);
        assertEquals(AbstractPacket.HEADER_SERVICES,packetBytes[0]);
    }

    @Test
    public void setPacketTest() {
        SetPacket setPacket = new SetPacket((byte)0xFF,1.5f);
        byte[] packetBytes = setPacket.getPacketBytes();
        ByteBuffer buff = ByteBuffer.allocate(4).putFloat(1.5f);
        byte[] floatBytes = buff.array();

        assertEquals(6,packetBytes.length);
        assertEquals(AbstractPacket.HEADER_SET,packetBytes[0]);
        assertEquals((byte)0xFF,packetBytes[1]);

        byte[] floatFromPacket = new byte[4];
        System.arraycopy(packetBytes,2,floatFromPacket,0,4);

        assertTrue(Arrays.equals(floatBytes,floatFromPacket));
    }
}
