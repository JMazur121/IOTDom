package project.iotdom.connection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import project.iotdom.crypt.RSA;
import project.iotdom.packets.AbstractPacket;

/**
 * Class for messages with plain header byte.
 * Messages that can contain plain header are CHALLENGE and KEY
 * Plain message contain 4 bytes of length, 1 byte of encryption information and packet bytes.
 */
public class PlainMessage extends AbstractMessage {

    public PlainMessage(AbstractPacket packet) {
        if (packet.getPacketHeader() == AbstractPacket.HEADER_CHALL) {
            byte[] packetBytes = packet.getPacketBytes();
            ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + packetBytes.length);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(packetBytes.length);
            buffer.put(plainMessage);
            buffer.put(packetBytes);
            messageBytes = buffer.array();
        }
        else if (packet.getPacketHeader() == AbstractPacket.HEADER_KEY) {
            RSA rsa = RSA.getInstance();
            // don't encrypt header
            byte[] encrypted = rsa.encryptData(packet.getPacketBytes(),1);
            ByteBuffer buffer = ByteBuffer.allocate(4 + 1 + 257);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(257);
            buffer.put(plainMessage);
            buffer.put(packet.getPacketHeader());
            buffer.put(encrypted);
            messageBytes = buffer.array();
        }
    }

    @Override
    public byte[] getBytes() {
        return messageBytes;
    }
}
