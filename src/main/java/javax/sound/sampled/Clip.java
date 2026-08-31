package javax.sound.sampled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mini.media.MaDecoder;
import org.mini.media.engine.MaEngine;
import org.mini.media.engine.MaSound;

/**
 * javax.sound Clip compatibility layer backed by miniJVM's miniaudio binding.
 */
public class Clip {
    public static final int LOOP_CONTINUOUSLY = -1;

    private static MaEngine sharedEngine;

    private byte[] audioData;
    private String audioPath;
    private MaDecoder decoder;
    private MaSound sound;
    private int loopCount;
    private int frameLength;
    private int sampleRate = 22050;
    private long durationMicros;
    private long baseMicros;
    private long startedAtMillis;

    private static synchronized MaEngine getEngine() {
        if (sharedEngine == null || sharedEngine.getHandle() == 0) {
            sharedEngine = new MaEngine();
            if (sharedEngine.getHandle() == 0) {
                throw new RuntimeException("Web Audio initialization failed");
            }
        }
        return sharedEngine;
    }

    public void open() {
        if (audioData != null) initializeSound();
    }

    public void open(AudioInputStream stream) throws IOException {
        open((InputStream) stream);
    }

    public void open(InputStream stream) throws IOException {
        if (stream instanceof AudioInputStream) {
            audioData = ((AudioInputStream) stream).getData();
        } else {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int count;
            while ((count = stream.read(buffer)) >= 0) {
                if (count > 0) output.write(buffer, 0, count);
            }
            audioData = output.toByteArray();
        }
        readWaveMetadata();
        initializeSound();
    }

    public void open(String path) {
        audioData = null;
        audioPath = path;
        initializeSound();
    }

    private void initializeSound() {
        if ((audioData == null || audioData.length == 0) && audioPath == null) return;
        if (sound != null) sound.stop();
        decoder = audioPath != null ? new MaDecoder(audioPath) : new MaDecoder(audioData);
        if (decoder.getHandle() == 0) {
            throw new RuntimeException("Unsupported or invalid audio data");
        }
        sound = new MaSound(getEngine(), decoder);
        if (sound.getHandle() == 0) {
            throw new RuntimeException("Unable to create Web Audio sound");
        }
        sound.setSpatialization(false);
        sound.setLooping(loopCount != 0);
        baseMicros = 0;
    }

    public void close() {
        stop();
        audioData = null;
        audioPath = null;
        sound = null;
        decoder = null;
    }

    public int getFramePosition() {
        return (int) Math.min(Integer.MAX_VALUE, getMicrosecondPosition() * sampleRate / 1000000L);
    }

    public void setFramePosition(int frames) {
        setMicrosecondPosition(frames * 1000000L / Math.max(1, sampleRate));
    }

    public int getFrameLength() {
        return frameLength;
    }

    public long getMicrosecondLength() {
        return durationMicros;
    }

    public void setMicrosecondPosition(long microseconds) {
        boolean restart = isRunning();
        stop();
        baseMicros = Math.max(0, Math.min(microseconds, durationMicros));

        // The exposed miniaudio API has no seek binding. J2ME players most often
        // rewind to zero, so recreate the data source for that operation.
        if (baseMicros == 0 && (audioData != null || audioPath != null)) initializeSound();
        if (restart) start();
    }

    public long getMicrosecondPosition() {
        long position = baseMicros;
        if (isRunning()) position += (System.currentTimeMillis() - startedAtMillis) * 1000L;
        if (durationMicros > 0 && loopCount == 0) position = Math.min(position, durationMicros);
        return position;
    }

    public void start() {
        if (sound == null && (audioData != null || audioPath != null)) initializeSound();
        if (sound == null || sound.isPlaying()) return;
        sound.setLooping(loopCount != 0);
        startedAtMillis = System.currentTimeMillis();
        sound.start();
    }

    public void stop() {
        if (sound == null || !sound.isPlaying()) return;
        baseMicros = getMicrosecondPosition();
        sound.stop();
    }

    public boolean isRunning() {
        return sound != null && sound.isPlaying();
    }

    public void setVolume(float volume) {
        if (sound != null) sound.setVolume(Math.max(0.0f, Math.min(1.0f, volume)));
    }

    public void loop(int count) {
        loopCount = count;
        if (sound != null) sound.setLooping(count != 0);
    }

    private void readWaveMetadata() {
        if (audioData == null || audioData.length < 44 ||
                readIntBE(audioData, 0) != 0x52494646 || readIntBE(audioData, 8) != 0x57415645) {
            return;
        }

        int offset = 12;
        int channels = 1;
        int bitsPerSample = 16;
        int dataLength = 0;
        while (offset + 8 <= audioData.length) {
            int id = readIntBE(audioData, offset);
            int length = readIntLE(audioData, offset + 4);
            int body = offset + 8;
            if (length < 0 || body + length > audioData.length) break;
            if (id == 0x666d7420 && length >= 16) {
                channels = readShortLE(audioData, body + 2);
                sampleRate = readIntLE(audioData, body + 4);
                bitsPerSample = readShortLE(audioData, body + 14);
            } else if (id == 0x64617461) {
                dataLength = length;
                break;
            }
            offset = body + length + (length & 1);
        }
        int bytesPerFrame = Math.max(1, channels * Math.max(1, bitsPerSample / 8));
        frameLength = dataLength / bytesPerFrame;
        durationMicros = frameLength * 1000000L / Math.max(1, sampleRate);
    }

    private static int readShortLE(byte[] data, int offset) {
        return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
    }

    private static int readIntLE(byte[] data, int offset) {
        return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8) |
                ((data[offset + 2] & 0xff) << 16) | ((data[offset + 3] & 0xff) << 24);
    }

    private static int readIntBE(byte[] data, int offset) {
        return ((data[offset] & 0xff) << 24) | ((data[offset + 1] & 0xff) << 16) |
                ((data[offset + 2] & 0xff) << 8) | (data[offset + 3] & 0xff);
    }
}
