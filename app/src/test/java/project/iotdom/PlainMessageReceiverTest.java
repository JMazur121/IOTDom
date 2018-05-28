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
import project.iotdom.packets.AbstractPacket;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PlainMessageReceiverTest {
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
        byte[] emptyPayload = new byte[100];
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(emptyPayload);
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

        AbstractPacket packet = MessageReceiver.plainPacket(clientSocket);
        assertNull(packet);
        clientSocket.close();
    }

    @Test
    public void givenEnoughButWrongPrefix() {
        byte[] emptyPayload = new byte[262];
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(emptyPayload);
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

        AbstractPacket packet = MessageReceiver.plainPacket(clientSocket);
        assertNull(packet);
        clientSocket.close();
    }

    @Test
    public void givenCorrectMessageAndGetsPacket() {
        ByteBuffer buffer = ByteBuffer.allocate(262);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(257);
        buffer.put(AbstractMessage.plainMessage);
        byte[] bytes = new byte[257];
        bytes[0] = AbstractPacket.HEADER_CHALL_RESP;
        buffer.put(bytes);
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

        AbstractPacket packet = MessageReceiver.plainPacket(clientSocket);
        assertNotNull(packet);
        assertEquals(packet.getPacketHeader(),AbstractPacket.HEADER_CHALL_RESP);
        clientSocket.close();
    }


}
