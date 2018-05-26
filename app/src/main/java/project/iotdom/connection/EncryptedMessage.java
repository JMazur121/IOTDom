package project.iotdom.connection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import project.iotdom.crypt.AES;
import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.SSIDPacket;

/**
 * Class for messages that are encrypted with AES
 */
public class EncryptedMessage extends AbstractMessage {

    public EncryptedMessage(AbstractPacket packet, byte ssid) {
        byte[] packetBytes = packet.getPacketBytes();
        AbstractPacket ssidPacket = new SSIDPacket(ssid);
        int plainLength = packetBytes.length;
        int capacity = 4 + 1 + 2 + AES.getLengthWithPKCSPadding(plainLength) + 16;
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(plainLength);
        buffer.put(encryptedMessage);
        buffer.put(ssidPacket.getPacketBytes());
        buffer.put(AES.getInstance().encryptBytes(packetBytes));
        messageBytes = buffer.array();
    }

    @Override
    public byte[] getBytes() {
        return messageBytes;
    }
}
