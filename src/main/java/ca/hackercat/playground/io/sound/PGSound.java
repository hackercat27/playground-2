package ca.hackercat.playground.io.sound;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.io.PGFileUtils;
import ca.hackercat.playground.math.PGMath;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class PGSound {

    private static final Logger LOGGER = Logger.get(PGSound.class);

    private int buffer;
    private int source;

    private float currentVolume = -1;
    private float currentX;
    private float currentY;
    private float currentZ;

    private final Object sourceLock = new Object();

    public PGSound(byte[] data, String path, boolean loops) {
        init(data, path, loops);
    }
    public PGSound(byte[] data, boolean loops) {
        init(data, "memory", loops);
    }
    public PGSound(String path, boolean loops) {
        InputStream is = PGFileUtils.getInputStream(path);
        byte[] data;

        try {
            data = is.readAllBytes();
        }
        catch (IOException e) {
            LOGGER.error(e);
            return;
        }

        init(data, path, loops);
    }

    private void init(byte[] data, String path, boolean loops) {

        if (path.matches(".*\\.ogg")) {
            initVorbis(data, path, loops);
        }
        else {
            LOGGER.errorf("Unkown format for file '%s'", path);
        }

        logErrors();
    }


    private void initVorbis(byte[] data, String path, boolean loops) {

        synchronized (sourceLock) {
            LOGGER.log("start of init");
            logErrors();

            // allocate space
            stackPush();
            IntBuffer channelsBuffer = stackMallocInt(1);
            stackPush();
            IntBuffer sampleRateBuffer = stackMallocInt(1);

            ByteBuffer inBuffer = BufferUtils.createByteBuffer(data.length);
            inBuffer.put(data).flip();

            ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(inBuffer, channelsBuffer, sampleRateBuffer);

//        List<Short> shorts = new ArrayList<>();
//
//        try {
//            for (;;) {
//                shorts.add(rawAudioBuffer.get());
//            }
//        }
//        catch (BufferUnderflowException ignored) {
//
//        }
//
//        File out = new File("out.wav");
//        try {
//            OutputStream fw = new FileOutputStream(out);
//
//            for (short s : shorts) {
//
//                int hi = (s >>> 8) & 0xFF;
//                int low = s & 0xFF;
//
//                fw.write(low);
//                fw.write(hi);
//
//            }
//            fw.close();
//        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return;


            if (rawAudioBuffer == null) {
                LOGGER.error("Error getting audio data for '" + path + "'");
                return;
            }

            int channels = channelsBuffer.get();
            int sampleRate = sampleRateBuffer.get();

            stackPop();
            stackPop();

            int format = -1;

            if (channels == 1) {
                format = AL_FORMAT_MONO16;
            }
            else if (channels == 2) {
                format = AL_FORMAT_STEREO16;
            }
            else {
                LOGGER.warn("bad channel count '" + channels + "'");
            }

            buffer = alGenBuffers();
            alBufferData(buffer, format, rawAudioBuffer, sampleRate);


            source = alGenSources();

            alSourcei(source, AL_BUFFER, buffer);
            alSourcei(source, AL_LOOPING, loops? 1 : 0);
            setVolume(1f);
            setPosition(0f, 0f, 0f);

            alDistanceModel(AL_EXPONENT_DISTANCE_CLAMPED);


            free(rawAudioBuffer);

            PGSoundManager.register(this);
            logErrors();
        }
    }

    private static float toDB(float linear) {
        return 10 * PGMath.log10(linear);
    }
    private static float toLinear(float db) {
        return PGMath.pow(10, db / 10);
    }

    public boolean isDisposable() {
        return !isPlaying();
    }

    public void dispose() {
        stop();
        close();
    }

    public void setVolume(float volume) {
        float v = PGMath.clamp(volume, 0f, 1f);
        if (v == currentVolume) {
            return;
        }
        currentVolume = v;
        alSourcef(source, AL_GAIN, v);
    }

    public void setPosition(float x, float y, float z) {
        if (currentX == x && currentY == y && currentZ == z) {
            // no change
            return;
        }
        currentX = x;
        currentY = y;
        currentZ = z;
        alSource3f(source, AL_POSITION, x, y, z);
    }

    public void close() {
        alDeleteSources(source);
        alDeleteBuffers(buffer);
    }

    public void play() {
        if (isStopped()) {
            alSourcePlay(source);
        }
    }

    public void stop() {
        if (isPlaying()) {
            alSourceStop(source);
        }
    }

    public boolean isPlaying() {
        int state = alGetSourcei(source, AL_SOURCE_STATE);
        return state == AL_PLAYING;
    }
    public boolean isStopped() {
        return !isPlaying();
    }
    public boolean isPaused() {
        int state = alGetSourcei(source, AL_SOURCE_STATE);
        return state == AL_PAUSED;
    }
    public boolean isInitial() {
        int state = alGetSourcei(source, AL_SOURCE_STATE);
        return state == AL_INITIAL;
    }

    private void logErrors() {
        int err = alGetError();
        if (err != 0) {
            LOGGER.error("OpenAL error " + err);
        }
    }
}
