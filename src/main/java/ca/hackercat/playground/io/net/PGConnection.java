package ca.hackercat.playground.io.net;

import ca.hackercat.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

public class PGConnection {
    private static final Logger LOGGER = Logger.get(PGConnection.class);

    public static final int DEFAULT_PORT = PGServer.DEFAULT_PORT;
    public static final int PACKET_TERMINATOR = PGServer.PACKET_TERMINATOR;

    private PacketListener[] listeners = new PacketListener[0x100];

    private Socket socket;
    private InputStream in;
    private OutputStream out;

    public PGConnection(String ip) throws IOException {
        this(ip, DEFAULT_PORT);
    }
    public PGConnection(String ip, int port) throws IOException {
        socket = new Socket(ip, port > 10? port : DEFAULT_PORT);
        in = socket.getInputStream();
        out = socket.getOutputStream();

        start();
    }

    private void start() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                StringBuilder packet = new StringBuilder();

                try {
                    // while loop condition should probably be better but "oh well"
                    while (in != null) {
                        int i = in.read();
                        if (i == -1) {
                            return;
                        }
                        if (i == PACKET_TERMINATOR) {
                            alertPacketListeners(packet.toString());
                            packet.setLength(0);
                        }
                        else {
                            packet.append((char) i);
                        }
                    }
                }
                catch (IOException e) {
                    LOGGER.error(e);
                }

            }
        }, "pg-connection").start();
    }

    private void alertPacketListeners(String packet) {
        for (PacketListener listener : listeners) {
            if (listener == null) {
                continue;
            }
            listener.onReceivePacket(packet);
        }
    }

    public void addPacketListener(PacketListener listener) {
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] == null) {
                listeners[i] = listener;
                return;
            }
        }
    }

    public void sendPacket(String packet) {
        if (out == null) {
            LOGGER.log("Couldn't send packet, connection has not been established!");
            return;
        }
        LOGGER.log("Sending [" + packet + "] to " + socket.getInetAddress().toString() + ":" + socket.getPort());
        new PrintStream(out).print(packet + (char) PACKET_TERMINATOR);
    }
}
