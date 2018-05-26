package project.iotdom.connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocket {

    private static final int connectionTimeout = 10 * 1000;
    private static final int readTimeout = 5 * 1000;

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private InetAddress address;
    private int port;
    private boolean isConnected;

    public ClientSocket(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.socket = new Socket();
        this.isConnected = false;
    }

    public void connect() throws Exception{
        socket.connect(new InetSocketAddress(address,port),connectionTimeout);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
        isConnected = true;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public int readFromSocket(byte[] destination, int offset, int bytesToRead) throws IOException {
        socket.setSoTimeout(readTimeout);
        int bytes = inputStream.read(destination,offset,bytesToRead);
        return bytes;
    }

    public void writeToSocket(byte[] bytesToWrite) throws IOException {
        outputStream.write(bytesToWrite);
        outputStream.flush();
    }

    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
