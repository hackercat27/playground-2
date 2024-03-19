package ca.hackercat.playground.io.net;

import ca.hackercat.logging.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

public class PGServer {

    private static final Logger LOGGER = Logger.get(PGServer.class);

    public static final int DEFAULT_PORT = 4054;
    public static final int PACKET_TERMINATOR = '\0';

    private ServerSocket server;
    // arbitrary size, using an array instead of a list for thread safety
    private PacketParser parser, exclusiveParser;
    private Socket[] clients = new Socket[0x1000];

    public PGServer() throws IOException  {
        this(DEFAULT_PORT);
    }

    public PGServer(int port) throws IOException {
        server = new ServerSocket(port);
        listenForConnections();
    }

    private void listenForConnections() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        Socket socket = server.accept();
                        LOGGER.log("Client connected from " + socket.getInetAddress().toString());
                        listenForPackets(socket);

                    }
                    catch (IOException e) {
                        LOGGER.log(e);
                    }
                }

            }
        }, "pg-connection-listener").start();
    }

    private void listenForPackets(Socket client) {
        final int slot = addClient(client);

        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder packet = new StringBuilder();

                try {
                    InputStream in = client.getInputStream();

                    LOGGER.log("Listening for packets on client slot " + slot);
                    // while loop condition should probably be better but "oh well"
                    while (in != null) {
                        int i = in.read();
                        if (i == -1) {
                            break;
                        }
                        if (i == PACKET_TERMINATOR) {
                            String exclusiveResponse = getExclusiveReturnPacket(packet.toString());
                            if (exclusiveResponse != null) {
                                sendPacketTo(exclusiveResponse, slot);
                            }
                            else {
                                // mirror the packet to all other clients except the one that sent it to us

                                String response = getReturnPacket(packet.toString());

                                if (response != null) {
                                    sendPacketExcepting(response, slot);
                                }
                            }

                            // clear the string builder
                            packet.setLength(0);
                        }
                        else {
                            packet.append((char) i);
                        }
                    }

                }
                catch (IOException ignored) {
                    LOGGER.log("Client " + slot + " disconnected.");
                    try {
                        client.close();
                    }
                    catch (IOException e) {
                        LOGGER.log(e);
                    }
                    clients[slot] = null;
                }
            }
        }, "pg-packet-listener" + slot).start();

    }

    private int addClient(Socket client) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                clients[i] = client;
                return i;
            }
        }
        return -1;
    }

    private void pruneDisconnectedClients() {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] == null) {
                continue;
            }
            if (clients[i].isClosed()) {
                clients[i] = null;
            }
        }
    }

    private void sendPacketExcepting(String packet, int exception) {
        LOGGER.log("Mirroring packet [" + packet + "] except to " + exception);
        for (int i = 0; i < clients.length; i++) {
            if (i == exception) {
                continue;
            }
            Socket client = clients[i];
            if (client == null) {
                continue;
            }
            sendPacket(client, packet);
        }
    }

    private void sendPacket(String packet) {
        LOGGER.log("Mirroring packet [" + packet + "]");
        for (Socket client : clients) {
            if (client == null) {
                continue;
            }
            sendPacket(client, packet);
        }
    }

    private void sendPacketTo(String packet, int slot) {
        if (slot < 0 || slot >= clients.length) {
            return;
        }
        Socket client = clients[slot];
        if (client == null) {
            return;
        }
        sendPacket(client, packet);
    }

    private void sendPacket(Socket recipient, String packet) {
        try {
            PrintStream out = new PrintStream(recipient.getOutputStream());
            out.print(packet + (char) PACKET_TERMINATOR);
        }
        catch (IOException ignored) {}
    }

    private String getReturnPacket(String packet) {
        if (parser == null) {
            return packet;
        }
        return parser.respondTo(packet);
    }

    private String getExclusiveReturnPacket(String packet) {
        if (exclusiveParser == null) {
            return null;
        }
        return exclusiveParser.respondTo(packet);
    }

    // TODO: actually encrypt the encoded data.
    //  Until this method is properly written, all sent data is unencrypted!!!
    private static String encrypt(String packet) {
        // need to encrypt, then base64 encode to ensure that no
        // null byte exists (since its used as a terminator byte)
        return Base64.getEncoder().encodeToString(packet.getBytes(StandardCharsets.UTF_8));
    }

    private static String decrypt(String packet) {
        return new String(Base64.getDecoder().decode(packet), StandardCharsets.UTF_8);
    }

    /**
     * Returns all currently connected clients in an array.
     *
     * @return All currently connected clients, as a Socket[].
     */
    public Socket[] getConnectedClients() {
        List<Socket> sockets = new LinkedList<>();

        for (Socket client : clients) {
            if (client == null) {
                continue;
            }
            sockets.add(client);
        }

        return sockets.toArray(new Socket[0]);
    }

    /**
     * Sets the packetParser.
     * <p>
     * Use a PacketParser to modify packets on-the-fly before they are
     * mirrored to the other clients. If the PacketParser returns a null
     * value, then no packet will be mirrored and it will go ignored.
     *
     * @param parser The PacketParser to use for parsing
     */
    public void setParser(PacketParser parser) {
        this.parser = parser;
    }

    /**
     * Sets the exclusive packetParser.
     * <p>
     * Use an exclusive PacketParser to respond exclusively to the client
     * that sent the packet to the parser. If the PacketParser returns a
     * non-null value, then the resulting parsed packet will be returned
     * exclusively to the client who originally sent the data. Otherwise,
     * for a null value, the original packet will be mirrored to all other
     * clients as normal.
     *
     * @param exclusiveParser The PacketParser to use for exclusive parsing
     */
    public void setExclusiveParser(PacketParser exclusiveParser) {
        this.exclusiveParser = exclusiveParser;
    }
}
