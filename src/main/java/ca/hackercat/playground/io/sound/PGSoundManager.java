package ca.hackercat.playground.io.sound;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PGSoundManager {

    private PGSoundManager() {}

    private static List<PGSound> sounds = new LinkedList<>();

    public static void register(PGSound sound) {
        sounds.add(sound);
    }

    public static void cleanSounds() {

        List<PGSound> list = new LinkedList<>(sounds);

        List<PGSound> disposed = new LinkedList<>();

        for (PGSound sound : list) {
            if (sound.isDisposable()) {
                sound.dispose();
                disposed.add(sound);
            }
        }

        sounds.removeAll(disposed);


    }

}
