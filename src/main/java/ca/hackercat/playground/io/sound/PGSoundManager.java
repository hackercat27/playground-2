package ca.hackercat.playground.io.sound;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.io.PGFileUtils;
import ca.hackercat.playground.math.PGMath;
import com.google.gson.Gson;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PGSoundManager {

    private static class SoundCache {

        public SoundCache(String path, byte[] data) {
            this.path = path;
            this.data = data;
        }

        String path;
        byte[] data;
    }

    private static final Logger LOGGER = Logger.get(PGSoundManager.class);

    private List<SoundCache> caches = new ArrayList<>();
    private PGSound[] sounds;



    public PGSoundManager(String soundConfigPath) {
        String json = PGFileUtils.getContents(soundConfigPath);
        sounds = new Gson().fromJson(json, PGSound[].class);
    }

    public void createSoundEvent(String event) {

        for (PGSound sound : sounds) {
            if (sound.event.equals(event)) {

                String path = sound.sounds[(int) (PGMath.random() * sound.sounds.length)];

                for (SoundCache cache : caches) {
                    if (cache.path.equals(path)) {
                        try {
                            play(new ByteArrayInputStream(cache.data), path);
                        }
                        catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                            LOGGER.error(e);
                        }
                        return;
                    }
                }

                try {
                    byte[] data = PGFileUtils.getInputStream(path).readAllBytes();
                    caches.add(new SoundCache(path, data));
                    play(new ByteArrayInputStream(data), path);
                }
                catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                    LOGGER.error(e);
                }

            }
        }

    }

    private void play(InputStream audioStream, String path)
            throws UnsupportedAudioFileException, IOException, LineUnavailableException {

        AudioInputStream ais = getAudioInputStream(audioStream, path);
        Clip clip = AudioSystem.getClip();
        clip.open(ais);

        clip.start();
    }

    private AudioInputStream getAudioInputStream(InputStream audioStream, String path)
            throws UnsupportedAudioFileException, IOException {

//        if (path.matches(".*\\.ogg")) {
//
//        }
//        else {
            return AudioSystem.getAudioInputStream(audioStream);
//        }

    }
}
