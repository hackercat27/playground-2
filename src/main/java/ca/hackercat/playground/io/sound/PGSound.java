package ca.hackercat.playground.io.sound;

import ca.hackercat.logging.Logger;
import ca.hackercat.playground.PGObject;
import ca.hackercat.playground.io.PGFileUtils;
import ca.hackercat.playground.math.PGMath;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class PGSound {

    private static final Logger LOGGER = Logger.get(PGSound.class);

    private int bufferID;
    private int sourceID;

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
        // allocate space
        stackPush();
        IntBuffer channelsBuffer = stackMallocInt(1);
        stackPush();
        IntBuffer sampleRateBuffer = stackMallocInt(1);

        ByteBuffer inBuffer = BufferUtils.createByteBuffer(data.length);
        inBuffer.put(data).flip();

        ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(inBuffer, channelsBuffer, sampleRateBuffer);

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

        bufferID = alGenBuffers();
        alBufferData(bufferID, format, rawAudioBuffer, sampleRate);

        sourceID = alGenSources();

        alSourcei(sourceID, AL_BUFFER, bufferID);
        alSourcei(sourceID, AL_LOOPING, loops ? 1 : 0);
        alSourcei(sourceID, AL_POSITION, 0);
        alSourcef(sourceID, AL_GAIN, 1f);

        free(rawAudioBuffer);

        PGSoundManager.register(this);
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

    public void close() {
        alDeleteSources(sourceID);
        alDeleteBuffers(bufferID);
    }

    public void play() {
        if (isStopped()) {
            alSourcei(sourceID, AL_POSITION, 0);
//            alSourcei(sourceID, AL_GAIN, 1);
            alSourcePlay(sourceID);
        }
    }

    public void stop() {
        if (isPlaying()) {
            alSourceStop(sourceID);
        }
    }

    public boolean isPlaying() {
        int state = alGetSourcei(sourceID, AL_SOURCE_STATE);
        return state == AL_PLAYING;
    }
    public boolean isStopped() {
        return !isPlaying();
    }
    public boolean isPaused() {
        int state = alGetSourcei(sourceID, AL_SOURCE_STATE);
        return state == AL_PAUSED;
    }
    public boolean isInitial() {
        int state = alGetSourcei(sourceID, AL_SOURCE_STATE);
        return state == AL_INITIAL;
    }

    private void logErrors() {
//        int err = alGetError();
//        if (err != 0) {
//            LOGGER.error("OpenAL error " + err);
//        }
    }
}
