package project.iotdom.connection;

import android.util.Log;

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

    /**
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
    }**/

    private static int readTillDone(ClientSocket socket, byte[] buffer, int toRead) {
        int received = 0;
        int remaining = toRead;
        boolean receiving = true;
        while (receiving) {
            int currentPart = socket.readFromSocket(buffer,received,remaining);
            Log.i("partrec","Odebralem czesc : "+String.valueOf(currentPart));
            if (currentPart == -1) {
                receiving = false;
                received = -1;
            }
            else {
                //mamy jakies bajty
                received += currentPart;
                remaining -= currentPart;
                if (remaining == 0)
                    receiving = false;
            }
        }
        return received;
    }

    /**
     * Method for receiving packet with plain header : CHALL_RESP
     * @param socket
     * @return created CHALL_RESP Packet; null otherwise
     */
    public static AbstractPacket plainPacket(ClientSocket socket) {
        Log.i("Plain","Zaczynam odbiranie pakietu plain");

        int expected = AbstractPacket.CHALL_RESP_LENGTH + MESSAGE_PREFIX_LENGTH;
        byte[] buff = new byte[expected];

        //int read = socket.readFromSocket(buff,0,expected);
        int read = readTillDone(socket,buff,expected);

        Log.i("plength",String.valueOf(read));
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
        Log.i("crypted","Zaczynam odbieranie zaszyfrowanego");
        byte[] prefixBuffer = new byte[MESSAGE_PREFIX_LENGTH];

        //int prefixRead = socket.readFromSocket(prefixBuffer,0,MESSAGE_PREFIX_LENGTH);
        int prefixRead = readTillDone(socket,prefixBuffer,MESSAGE_PREFIX_LENGTH);
        Log.i("prefix","przecytalem prefix dlugosci : "+String.valueOf(prefixRead));

        if (prefixRead != MESSAGE_PREFIX_LENGTH)
            return null;

        ByteBuffer buffer = ByteBuffer.allocate(MESSAGE_PREFIX_LENGTH);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(prefixBuffer,0,MESSAGE_PREFIX_LENGTH);
        buffer.flip();
        int length = buffer.getInt();
        Log.i("declength", "Deklarowana dlugosc plain : "+String.valueOf(length));
        byte encryptionHeader = buffer.get();
        if ((length < 0) || (encryptionHeader != encryptedMessage))
            return null;

        //Fetch encrypted data
        //plain length + 16 bytes of IV
        int encryptedDataLength = AES.getLengthWithPKCSPadding(length);
        Log.i("doodebrania","Powinienem odebrac "+String.valueOf(encryptedDataLength)+" zaszyfrowanego");

        byte[] encryptedData = new byte[encryptedDataLength];
        byte[] ivVector = new byte[IV_VECTOR_LENGTH];

        //int encryptedRead = socket.readFromSocket(encryptedData,0,encryptedDataLength);
        int encryptedRead = readTillDone(socket,encryptedData,encryptedDataLength);
        Log.i("decreceived","Odczytano "+String.valueOf(encryptedRead)+" bajtow encrypted");

        if (encryptedRead != encryptedDataLength)
            return null;

        //int ivRead = socket.readFromSocket(ivVector,0,IV_VECTOR_LENGTH);
        int ivRead = readTillDone(socket,ivVector,IV_VECTOR_LENGTH);
        Log.i("ivreceived","Odebralem "+String.valueOf(ivRead)+" bajtow wektora");
        if (ivRead != IV_VECTOR_LENGTH)
            return null;
        //try to decrypt the data
        Log.i("decrytping","Podejmuje probe dekryptowania");
        byte[] decryptedData = AES.getInstance().decryptBytes(encryptedData,ivVector);
        //check if not null (Bad padding or block size)
        if (decryptedData == null)
            return null;
        //check if decrypted length equals length from prefix
        if (decryptedData.length != length)
            return null;
        Log.i("decrypted","Dekrypcja przebiegla pomyslnie");
        ByteBuffer packetBytes = ByteBuffer.allocate(length);
        packetBytes.order(ByteOrder.LITTLE_ENDIAN);
        packetBytes.put(decryptedData,0,length);
        packetBytes.flip();
        return PacketsFactory.buildPacketFromBytes(packetBytes);
    }
}
