package project.iotdom.connection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import project.iotdom.crypt.AES;
import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.PacketsFactory;

public class MessageReceiver {

    public static final int MESSAGE_PREFIX_LENGTH = 5;
    public static final byte plainMessage = 0x00;
    public static final byte encryptedMessage = 0x01;

    public static final int IV_VECTOR_LENGTH = 16;

    public static ByteBuffer receiveBytes(ClientSocket socket, int expected) {
        byte[] buff = new byte[expected];
        int read = socket.readFromSocket(buff,0,expected);
        if (read != expected)
            return null;
        ByteBuffer buffer = ByteBuffer.allocate(expected);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(buff);
        buffer.flip();
        return buffer;
    }

    /**
     * Method for receiving packet with plain header : CHALL_RESP
     * @param socket
     * @return created CHALL_RESP Packet; null otherwise
     */
    public static AbstractPacket plainPacket(ClientSocket socket) {
        int expected = AbstractPacket.CHALL_RESP_LENGTH + MESSAGE_PREFIX_LENGTH;
        byte[] buff = new byte[expected];
        int read = socket.readFromSocket(buff,0,expected);
        if (read != expected)
            return null;
        ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_PREFIX_LENGTH);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(buff,0,MESSAGE_PREFIX_LENGTH);
        buffer.flip();
        int length = buffer.getInt();
        byte encryptionHeader = buffer.get();
        if ((length != AbstractPacket.CHALL_RESP_LENGTH) || (encryptionHeader != plainMessage))
            return null;
        ByteBuffer packetBytes = ByteBuffer.allocate(AbstractPacket.CHALL_RESP_LENGTH);
        packetBytes.order(ByteOrder.LITTLE_ENDIAN);
        packetBytes.put(buff,MESSAGE_PREFIX_LENGTH,AbstractPacket.CHALL_RESP_LENGTH);
        packetBytes.flip();
        return PacketsFactory.buildPacketFromBytes(packetBytes);
    }

    /**
     * Method for receiving encrypted packet. Method should calculate the length of data, decrypt it
     * and build a packet.
     * @param socket
     * @return
     */
    public static AbstractPacket decryptedPacket(ClientSocket socket) {
        //Chceck the message prefix
        byte[] prefixBuffer = new byte[MESSAGE_PREFIX_LENGTH];
        int prefixRead = socket.readFromSocket(prefixBuffer,0,MESSAGE_PREFIX_LENGTH);
        if (prefixRead != MESSAGE_PREFIX_LENGTH)
            return null;
        ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_PREFIX_LENGTH);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(prefixBuffer,0,MESSAGE_PREFIX_LENGTH);
        buffer.flip();
        int length = buffer.getInt();
        byte encryptionHeader = buffer.get();
        if ((length < 0) || (encryptionHeader != encryptedMessage))
            return null;

        //Fetch encrypted data
        //plain length + 16 bytes of IV
        int encryptedDataLength = AES.getLengthWithPKCSPadding(length);
        byte[] encryptedData = new byte[encryptedDataLength];
        byte[] ivVector = new byte[IV_VECTOR_LENGTH];

        int encryptedRead = socket.readFromSocket(encryptedData,0,encryptedDataLength);
        if (encryptedRead != encryptedDataLength)
            return null;
        int ivRead = socket.readFromSocket(ivVector,0,IV_VECTOR_LENGTH);
        if (ivRead != IV_VECTOR_LENGTH)
            return null;
        //try to decrypt the data
        byte[] decryptedData = AES.getInstance().decryptBytes(encryptedData,ivVector);
        //check if not null (Bad padding or block size)
        if (decryptedData == null)
            return null;
        //check if decrypted length equals length from prefix
        if (decryptedData.length != length)
            return null;
        ByteBuffer packetBytes = ByteBuffer.allocate(length);
        packetBytes.order(ByteOrder.LITTLE_ENDIAN);
        packetBytes.put(decryptedData,0,length);
        packetBytes.flip();
        return PacketsFactory.buildPacketFromBytes(packetBytes);
    }
}
