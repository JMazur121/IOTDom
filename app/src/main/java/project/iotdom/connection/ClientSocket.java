package project.iotdom.connection;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

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
        this.socket = createSocket();
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

    public int readFromSocket(byte[] destination, int offset, int bytesToRead) {
        int bytes = -1;
        try {
            socket.setSoTimeout(readTimeout);
            Log.i("Timeot","Ustawilem timeout");
            bytes = inputStream.read(destination,offset,bytesToRead);
            Log.i("read","zrobilem czytanie");
        } catch (SocketException se) {
            Log.i("wyjatek","zlapalem socketexception w czytaniu");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            Log.i("wyjatek2","Zlapalem io exception");
        }
        return bytes;
    }

    public boolean writeToSocket(byte[] bytesToWrite) {
        try {
            outputStream.write(bytesToWrite);
            outputStream.flush();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void close() {
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception e) {}
    }

    protected Socket createSocket() {
        return new Socket();
    }
}
