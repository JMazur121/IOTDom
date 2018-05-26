package project.iotdom;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import project.iotdom.connection.AbstractMessage;
import project.iotdom.connection.PlainMessage;
import project.iotdom.crypt.AES;
import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.ChallPacket;
import project.iotdom.packets.KeyPacket;

import static org.junit.Assert.*;

public class PlainMessageTest {
    @Test
    public void challengeMessageTest() {

        AbstractPacket packet = new ChallPacket();
        AbstractMessage message = new PlainMessage(packet);
        byte[] messageBytes = message.getBytes();

        assertEquals(14,messageBytes.length);

        ByteBuffer buffer = ByteBuffer.allocate(14);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(messageBytes);
        buffer.flip();
        int length = buffer.getInt();

        assertEquals(9,length);

        byte encryptionByte = buffer.get();

        assertEquals(0x00,encryptionByte);

        byte[] data = new byte[9];
        buffer.get(data);
        assertTrue(Arrays.equals(data,Arrays.copyOfRange(packet.getPacketBytes(),0,9)));
    }
    @Test
    public void keyMessageTest() {

        AbstractPacket packet = new KeyPacket(AES.getInstance().getKey());
        AbstractMessage message = new PlainMessage(packet);
        byte[] messageBytes = message.getBytes();

        assertEquals(4+1+257,messageBytes.length);

        ByteBuffer byteBuffer = ByteBuffer.allocate(4+1+257);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.put(messageBytes);
        byteBuffer.flip();

        int length = byteBuffer.getInt();

        assertEquals(257,length);

        byte encryptionByte = byteBuffer.get();

        assertEquals(0x00,encryptionByte);
    }
}
