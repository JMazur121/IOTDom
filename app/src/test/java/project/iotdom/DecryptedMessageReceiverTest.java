package project.iotdom;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.MockitoRule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import project.iotdom.connection.AbstractMessage;
import project.iotdom.connection.ClientSocket;
import project.iotdom.connection.MessageReceiver;
import project.iotdom.crypt.AES;
import project.iotdom.packets.AbstractPacket;
import project.iotdom.packets.AckPacket;
import project.iotdom.packets.DescPacket;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DecryptedMessageReceiverTest {
    @Mock
    Socket socket;
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    final int port = 1234;
    InetAddress address = null;
    @Before
    public void setUp() {
        try {
            //Mockito.doNothing().when(socket).connect(socketAddress,connectionTimeout);
            Mockito.when(socket.getOutputStream()).thenReturn(byteArrayOutputStream);
        } catch (IOException e) {
            fail("Exception");
        }
    }
    @Test
    public void givenLessThanExpectedAndNullReturn() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(200);
        buffer.put(MessageReceiver.encryptedMessage);
        byte[] emptyPayload = new byte[95];
        buffer.put(emptyPayload);
        buffer.flip();
        byte[] message = buffer.array();
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message);
        try {
            when(socket.getInputStream()).thenReturn(byteArrayInputStream);
        } catch (IOException e) {
            fail("Exception");
        }
        ClientSocket clientSocket = new ClientSocket(address,port) {
            @Override
            protected Socket createSocket() {
                return socket;
            }
        };
        try {
            clientSocket.connect();
        } catch (Exception e) {
            fail("Exception");
        }
        AbstractPacket packet = MessageReceiver.decryptedPacket(clientSocket);
        assertNull(packet);
        clientSocket.close();
    }

    @Test
    public void givenWrongPrefixAndNull() {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(100);
        buffer.put(MessageReceiver.plainMessage);
        buffer.flip();
        byte[] message = buffer.array();
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message);
        try {
            when(socket.getInputStream()).thenReturn(byteArrayInputStream);
        } catch (IOException e) {
            fail("Exception");
        }
        ClientSocket clientSocket = new ClientSocket(address,port) {
            @Override
            protected Socket createSocket() {
                return socket;
            }
        };
        try {
            clientSocket.connect();
        } catch (Exception e) {
            fail("Exception");
        }
        AbstractPacket packet = MessageReceiver.decryptedPacket(clientSocket);
        assertNull(packet);
        clientSocket.close();
    }

    @Test
    public void givenACKMessageAndACKPacketReturned() {
        ByteBuffer buffer = ByteBuffer.allocate(5+2*16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(AbstractPacket.ACK_LENGTH);
        buffer.put(MessageReceiver.encryptedMessage);
        AckPacket packet = new AckPacket((byte)0xAB);
        byte[] encData = AES.getInstance().encryptBytes(packet.getPacketBytes());

        assertEquals(encData.length,32);

        buffer.put(encData);
        buffer.flip();
        byte[] message = buffer.array();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message);
        try {
            when(socket.getInputStream()).thenReturn(byteArrayInputStream);
        } catch (IOException e) {
            fail("Exception");
        }
        ClientSocket clientSocket = new ClientSocket(address,port) {
            @Override
            protected Socket createSocket() {
                return socket;
            }
        };
        try {
            clientSocket.connect();
        } catch (Exception e) {
            fail("Exception");
        }

        AbstractPacket abstractPacket = MessageReceiver.decryptedPacket(clientSocket);

        assertNotNull(abstractPacket);
        assertEquals(abstractPacket.getPacketHeader(),AbstractPacket.HEADER_ACK);

        AckPacket ackPacket = (AckPacket)abstractPacket;
        assertEquals(ackPacket.getVal(),(byte)0xAB);
        clientSocket.close();
    }

    @Test
    public void givenDescMessageAndDescPacketReturned() {
        DescPacket descPacket = new DescPacket((byte)0x03, (byte)0x05,"Nazwa","Unit",2.5f,10f);
        byte[] packetBytes = descPacket.getPacketBytes();
        ByteBuffer buffer = ByteBuffer.allocate(5 + AES.getLengthWithPKCSPadding(packetBytes.length)+16);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(packetBytes.length);
        buffer.put(MessageReceiver.encryptedMessage);
        buffer.put(AES.getInstance().encryptBytes(packetBytes));

        buffer.flip();
        byte[] message = buffer.array();

        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(message);
        try {
            when(socket.getInputStream()).thenReturn(byteArrayInputStream);
        } catch (IOException e) {
            fail("Exception");
        }
        ClientSocket clientSocket = new ClientSocket(address,port) {
            @Override
            protected Socket createSocket() {
                return socket;
            }
        };
        try {
            clientSocket.connect();
        } catch (Exception e) {
            fail("Exception");
        }

        AbstractPacket abstractPacket = MessageReceiver.decryptedPacket(clientSocket);

        assertNotNull(abstractPacket);

        assertEquals(abstractPacket.getPacketHeader(),AbstractPacket.HEADER_DESC);

        DescPacket descPacket1 = (DescPacket)abstractPacket;

        assertEquals(descPacket.getDeviceClass(),(byte)0x03);

        assertEquals(descPacket.getServiceID(), (byte)0x05);

        assertTrue(descPacket1.getHumanReadableName().equals("Nazwa"));

        assertTrue(descPacket1.getUnitName().equals("Unit"));

        assertTrue(Float.compare(2.5f,descPacket1.getMinValue()) == 0);

        assertTrue(Float.compare(10f,descPacket1.getMaxValue()) == 0);

        clientSocket.close();
    }
}
