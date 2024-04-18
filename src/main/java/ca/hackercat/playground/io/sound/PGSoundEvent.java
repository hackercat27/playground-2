package ca.hackercat.playground.io.sound;

public class PGSoundEvent {
    public String event;
    public String[] sounds;
    public boolean stream;
    public boolean loop;

    public PGSoundEvent(boolean stream, boolean loop, String... sounds) {
        this.event = "";
        this.sounds = sounds;
        this.stream = stream;
        this.loop = loop;
    }
}
