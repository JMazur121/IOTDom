package project.iotdom.connection;

import project.iotdom.packets.AbstractPacket;

public class MessageProvider {

    /**
     * Method for creating abstract message. It checks if packet should be encrypted and
     * creates a suitable AbstractMessage subclass instance.
     * @param packet Packet to send
     * @param ssid Used only with encrypted packets, discarded otherwise
     * @return message ready to send
     */
    public static AbstractMessage buildMessage(AbstractPacket packet, byte ssid) {
        byte header = packet.getPacketHeader();
        if (header == AbstractPacket.HEADER_CHALL || header == AbstractPacket.HEADER_KEY) {
            return new PlainMessage(packet);
        }
        else {
            return new EncryptedMessage(packet, ssid);
        }
    }
}
