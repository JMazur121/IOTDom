package project.iotdom;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import project.iotdom.connection.AbstractMessage;
import project.iotdom.connection.EncryptedMessage;
import project.iotdom.crypt.AES;
import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.AckPacket;
import project.iotdom.packets.EotPacket;
import project.iotdom.packets.GetPacket;
import project.iotdom.packets.LogPacket;
import project.iotdom.packets.NakPacket;
import project.iotdom.packets.SetPacket;

import static org.junit.Assert.*;

public class EncryptedMessageTest {
    @Test
    public void ackMessageTest() {
        AbstractPacket packet = new AckPacket((byte)0xFF);
        byte ssid = 0x12;
        AbstractMessage message = new EncryptedMessage(packet,ssid);
        byte[] messageBytes = message.getBytes();

        int expectedLength = 4 + 1 + 2 + AES.getLengthWithPKCSPadding(2) + 16;

        assertEquals(expectedLength,messageBytes.length);

        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(messageBytes);
        buffer.flip();

        int plainLength = buffer.getInt();
        assertEquals(plainLength,packet.getPacketBytes().length);

        assertEquals(AbstractMessage.encryptedMessage,buffer.get());

        assertEquals(AbstractPacket.HEADER_SSID,buffer.get());

        assertEquals(ssid,buffer.get());

        assertEquals(AES.getLengthWithPKCSPadding(2)+16,buffer.remaining());

        byte[] data = new byte[AES.getLengthWithPKCSPadding(2)];
        byte[] iv = new byte[16];

        buffer.get(data,0,AES.getLengthWithPKCSPadding(2));
        buffer.get(iv,0,16);

        byte[] decrypted = AES.getInstance().decryptBytes(data,iv);

        assertEquals(2,decrypted.length);
        assertTrue(Arrays.equals(decrypted,packet.getPacketBytes()));
    }

    @Test
    public void eotMessageTest() {
        AbstractPacket packet = new EotPacket();
        byte ssid = 0x12;
        AbstractMessage message = new EncryptedMessage(packet,ssid);
        byte[] messageBytes = message.getBytes();

        int expectedLength = 4 + 1 + 2 + AES.getLengthWithPKCSPadding(1) + 16;

        assertEquals(expectedLength,messageBytes.length);

        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(messageBytes);
        buffer.flip();

        int plainLength = buffer.getInt();
        assertEquals(plainLength,packet.getPacketBytes().length);

        assertEquals(AbstractMessage.encryptedMessage,buffer.get());

        assertEquals(AbstractPacket.HEADER_SSID,buffer.get());

        assertEquals(ssid,buffer.get());

        assertEquals(AES.getLengthWithPKCSPadding(1)+16,buffer.remaining());

        byte[] data = new byte[AES.getLengthWithPKCSPadding(1)];
        byte[] iv = new byte[16];

        buffer.get(data,0,AES.getLengthWithPKCSPadding(1));
        buffer.get(iv,0,16);

        byte[] decrypted = AES.getInstance().decryptBytes(data,iv);

        assertEquals(1,decrypted.length);
        assertTrue(Arrays.equals(decrypted,packet.getPacketBytes()));
    }

    @Test
    public void getMessageTest() {
        AbstractPacket packet = new GetPacket((byte)0xFF);
        byte ssid = 0x12;
        AbstractMessage message = new EncryptedMessage(packet,ssid);
        byte[] messageBytes = message.getBytes();

        int expectedLength = 4 + 1 + 2 + AES.getLengthWithPKCSPadding(2) + 16;

        assertEquals(expectedLength,messageBytes.length);

        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(messageBytes);
        buffer.flip();

        int plainLength = buffer.getInt();
        assertEquals(plainLength,packet.getPacketBytes().length);

        assertEquals(AbstractMessage.encryptedMessage,buffer.get());

        assertEquals(AbstractPacket.HEADER_SSID,buffer.get());

        assertEquals(ssid,buffer.get());

        assertEquals(AES.getLengthWithPKCSPadding(2)+16,buffer.remaining());

        byte[] data = new byte[AES.getLengthWithPKCSPadding(2)];
        byte[] iv = new byte[16];

        buffer.get(data,0,AES.getLengthWithPKCSPadding(2));
        buffer.get(iv,0,16);

        byte[] decrypted = AES.getInstance().decryptBytes(data,iv);

        assertEquals(2,decrypted.length);
        assertTrue(Arrays.equals(decrypted,packet.getPacketBytes()));
    }

    @Test
    public void logMessageTest() {
        AbstractPacket packet = new LogPacket("login","password");
        byte ssid = 0x12;
        AbstractMessage message = new EncryptedMessage(packet,ssid);
        byte[] messageBytes = message.getBytes();

        int expectedLength = 4 + 1 + 2 + AES.getLengthWithPKCSPadding(63) + 16;

        assertEquals(expectedLength,messageBytes.length);

        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(messageBytes);
        buffer.flip();

        int plainLength = buffer.getInt();
        assertEquals(plainLength,packet.getPacketBytes().length);

        assertEquals(AbstractMessage.encryptedMessage,buffer.get());

        assertEquals(AbstractPacket.HEADER_SSID,buffer.get());

        assertEquals(ssid,buffer.get());

        assertEquals(AES.getLengthWithPKCSPadding(63)+16,buffer.remaining());

        byte[] data = new byte[AES.getLengthWithPKCSPadding(63)];
        byte[] iv = new byte[16];

        buffer.get(data,0,AES.getLengthWithPKCSPadding(63));
        buffer.get(iv,0,16);

        byte[] decrypted = AES.getInstance().decryptBytes(data,iv);

        assertEquals(63,decrypted.length);
        assertTrue(Arrays.equals(decrypted,packet.getPacketBytes()));
    }

    @Test
    public void nakMessageTest() {
        AbstractPacket packet = new NakPacket((byte)0xFF);
        byte ssid = 0x12;
        AbstractMessage message = new EncryptedMessage(packet,ssid);
        byte[] messageBytes = message.getBytes();

        int expectedLength = 4 + 1 + 2 + AES.getLengthWithPKCSPadding(2) + 16;

        assertEquals(expectedLength,messageBytes.length);

        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(messageBytes);
        buffer.flip();

        int plainLength = buffer.getInt();
        assertEquals(plainLength,packet.getPacketBytes().length);

        assertEquals(AbstractMessage.encryptedMessage,buffer.get());

        assertEquals(AbstractPacket.HEADER_SSID,buffer.get());

        assertEquals(ssid,buffer.get());

        assertEquals(AES.getLengthWithPKCSPadding(2)+16,buffer.remaining());

        byte[] data = new byte[AES.getLengthWithPKCSPadding(2)];
        byte[] iv = new byte[16];

        buffer.get(data,0,AES.getLengthWithPKCSPadding(2));
        buffer.get(iv,0,16);

        byte[] decrypted = AES.getInstance().decryptBytes(data,iv);

        assertEquals(2,decrypted.length);
        assertTrue(Arrays.equals(decrypted,packet.getPacketBytes()));
    }

    @Test
    public void setMessageTest() {
        AbstractPacket packet = new SetPacket((byte)0xFF,1.2f);
        byte ssid = 0x12;
        AbstractMessage message = new EncryptedMessage(packet,ssid);
        byte[] messageBytes = message.getBytes();

        int expectedLength = 4 + 1 + 2 + AES.getLengthWithPKCSPadding(6) + 16;

        assertEquals(expectedLength,messageBytes.length);

        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(messageBytes);
        buffer.flip();

        int plainLength = buffer.getInt();
        assertEquals(plainLength,packet.getPacketBytes().length);

        assertEquals(AbstractMessage.encryptedMessage,buffer.get());

        assertEquals(AbstractPacket.HEADER_SSID,buffer.get());

        assertEquals(ssid,buffer.get());

        assertEquals(AES.getLengthWithPKCSPadding(6)+16,buffer.remaining());

        byte[] data = new byte[AES.getLengthWithPKCSPadding(6)];
        byte[] iv = new byte[16];

        buffer.get(data,0,AES.getLengthWithPKCSPadding(6));
        buffer.get(iv,0,16);

        byte[] decrypted = AES.getInstance().decryptBytes(data,iv);

        assertEquals(6,decrypted.length);
        assertTrue(Arrays.equals(decrypted,packet.getPacketBytes()));
    }

    @Test
    public void ssidMessageTest() {
        AbstractPacket packet = new AckPacket((byte)0xFF);
        byte ssid = 0x12;
        AbstractMessage message = new EncryptedMessage(packet,ssid);
        byte[] messageBytes = message.getBytes();

        int expectedLength = 4 + 1 + 2 + AES.getLengthWithPKCSPadding(2) + 16;

        assertEquals(expectedLength,messageBytes.length);

        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(messageBytes);
        buffer.flip();

        int plainLength = buffer.getInt();
        assertEquals(plainLength,packet.getPacketBytes().length);

        assertEquals(AbstractMessage.encryptedMessage,buffer.get());

        assertEquals(AbstractPacket.HEADER_SSID,buffer.get());

        assertEquals(ssid,buffer.get());

        assertEquals(AES.getLengthWithPKCSPadding(2)+16,buffer.remaining());

        byte[] data = new byte[AES.getLengthWithPKCSPadding(2)];
        byte[] iv = new byte[16];

        buffer.get(data,0,AES.getLengthWithPKCSPadding(2));
        buffer.get(iv,0,16);

        byte[] decrypted = AES.getInstance().decryptBytes(data,iv);

        assertEquals(2,decrypted.length);
        assertTrue(Arrays.equals(decrypted,packet.getPacketBytes()));
    }
}
