package ca.hackercat.playground.io.sound;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.io.PGFileUtils;
import ca.hackercat.playground.math.PGMath;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PGSoundEventManager {

    private static final Logger LOGGER = Logger.get(PGSoundEventManager.class);

    private PGSoundEvent[] events;

    private Map<String, byte[]> soundDataMap = new HashMap<>();

    public PGSound createSoundEvent(String name) {
        PGSound sound = getSound(name);
        if (sound == null) {
            return null;
        }

        sound.play();
        return sound;
    }
    public void invokeSoundEvent(PGSoundEvent event) {
        PGSound sound = getSound(event);

        if (sound == null) {
            return;
        }

        LOGGER.log("Playing sound...");

        sound.play();
    }

    public PGSoundEventManager(String path) {
        events = new Gson().fromJson(PGFileUtils.getContents(path), PGSoundEvent[].class);
    }

    private PGSound getSound(String name) {
        if (events == null) {
            LOGGER.warn("Sound events not initialized!");
            return null;
        }

        for (PGSoundEvent event : events) {
            if (event.event.equals(name)) {
                return getSound(event);
            }
        }
        return null;
    }

    private PGSound getSound(PGSoundEvent event) {
        int index = (int) (PGMath.randomf() * event.sounds.length);
        String path = event.sounds[index];

        byte[] data;

        if (event.stream) {
            InputStream is = PGFileUtils.getInputStream(path);

            try {
                data = is.readAllBytes();
            }
            catch (IOException e) {
                LOGGER.error(e);
                return null;
            }
        }
        else {

            if (soundDataMap.containsKey(path)) {
                data = soundDataMap.get(path);
            } else {
                InputStream is = PGFileUtils.getInputStream(path);

                if (is == null) {
                    return null;
                }
                try {
                    data = is.readAllBytes();
                }
                catch (IOException e) {
                    LOGGER.error(e);
                    return null;
                }
                soundDataMap.put(path, data);
            }
        }

        return new PGSound(data, path, event.loop);
    }
}
