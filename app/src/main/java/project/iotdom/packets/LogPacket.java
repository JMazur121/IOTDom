package project.iotdom.packets;

import java.nio.charset.StandardCharsets;

public class LogPacket extends AbstractPacket {

    private String login;
    private String password;

    public LogPacket(String login, String password) {
        this.login = login;
        this.password = password;
        packetHeader = HEADER_LOG;
    }

    @Override
    public byte[] getPacketBytes() {
        byte[] bytes = new byte[63];
        bytes[0] = HEADER_LOG;
        byte[] loginBytes = login.getBytes(StandardCharsets.US_ASCII);
        byte[] passwordBytes = password.getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(loginBytes,0,bytes,1,loginBytes.length);
        System.arraycopy(passwordBytes,0,bytes,32,password.length());
        return bytes;
    }
}
