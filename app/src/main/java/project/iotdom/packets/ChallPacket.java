package project.iotdom.packets;

import java.security.SecureRandom;

public class ChallPacket extends AbstractPacket {

    private byte[] packetBytes;

    public ChallPacket() {
        SecureRandom random = new SecureRandom();
        packetBytes = new byte[9];
        random.nextBytes(packetBytes);
        packetBytes[0] = HEADER_CHALL;
        packetHeader = HEADER_CHALL;
    }

    @Override
    public byte[] getPacketBytes() {
        return packetBytes;
    }

}
