package ca.hackercat.playground.io.net;

public interface PGConnection {
    void addPacketListener(PacketListener listener);
    void sendPacket(String packet);
}
