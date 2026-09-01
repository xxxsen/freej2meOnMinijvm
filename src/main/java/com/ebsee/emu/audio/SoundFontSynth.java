package com.ebsee.emu.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import org.mini.glfw.Glfw;

/** Renders Standard MIDI files with TinySoundFont and the bundled GM bank. */
public final class SoundFontSynth {
    private static final String SOUNDFONT_PATH = "/lib/TimGM6mb.sf2";
    private static final int MAX_CACHE_BYTES = 16 * 1024 * 1024;
    private static final Hashtable<MidiKey, CacheEntry> CACHE = new Hashtable<MidiKey, CacheEntry>();
    private static int nextFileId;
    private static int cachedBytes;

    static {
        Glfw.loadLib();
    }

    private SoundFontSynth() {
    }

    public static Result render(byte[] midiData) throws IOException {
        MidiKey key = new MidiKey(midiData);
        CacheEntry entry;
        boolean owner = false;
        synchronized (CACHE) {
            entry = CACHE.get(key);
            if (entry == null) {
                entry = new CacheEntry();
                CACHE.put(key, entry);
                owner = true;
            }
        }

        if (owner) {
            try {
                Result result = renderUncached(midiData);
                synchronized (entry) {
                    entry.result = result;
                    entry.ready = true;
                    entry.notifyAll();
                }
                retainCompleted(key, result.waveData.length);
                return result;
            } catch (Throwable problem) {
                IOException failure = problem instanceof IOException
                        ? (IOException) problem
                        : new IOException("SoundFont MIDI renderer failed: " + problem);
                synchronized (entry) {
                    entry.failure = failure;
                    entry.ready = true;
                    entry.notifyAll();
                }
                synchronized (CACHE) { CACHE.remove(key); }
                throw failure;
            }
        }

        synchronized (entry) {
            while (!entry.ready) {
                try {
                    entry.wait();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while waiting for shared MIDI rendering");
                }
            }
            if (entry.failure != null) throw entry.failure;
            return entry.result;
        }
    }

    private static Result renderUncached(byte[] midiData) throws IOException {
        String wavePath = "/tmp/j2me-soundfont-" + nextId() + ".wav";
        File waveFile = new File(wavePath);
        int durationMillis = renderToWave(midiData, cString(SOUNDFONT_PATH), cString(wavePath));
        if (durationMillis < 0) {
            throw new IOException("SoundFont MIDI renderer failed with code " + durationMillis);
        }

        try {
            FileInputStream input = new FileInputStream(waveFile);
            try {
                long length = waveFile.length();
                if (length <= 0 || length > Integer.MAX_VALUE) {
                    throw new IOException("Invalid rendered wave length: " + length);
                }
                byte[] waveData = ExactLengthReader.read(input, (int) length);
                return new Result(waveData, null, durationMillis);
            } finally {
                input.close();
            }
        } finally {
            waveFile.delete();
        }
    }

    private static void retainCompleted(MidiKey keep, int bytes) {
        synchronized (CACHE) {
            cachedBytes += bytes;
            if (cachedBytes <= MAX_CACHE_BYTES) return;
            Enumeration<MidiKey> keys = CACHE.keys();
            while (keys.hasMoreElements() && cachedBytes > MAX_CACHE_BYTES) {
                MidiKey key = keys.nextElement();
                if (key.equals(keep)) continue;
                CacheEntry entry = CACHE.get(key);
                if (entry != null && entry.ready && entry.result != null && CACHE.remove(key) != null) {
                    cachedBytes -= entry.result.waveData.length;
                }
            }
        }
    }

    public static Result renderToFile(byte[] midiData) throws IOException {
        String wavePath = "/tmp/j2me-soundfont-" + nextId() + ".wav";
        int durationMillis = renderToWave(midiData, cString(SOUNDFONT_PATH), cString(wavePath));
        if (durationMillis < 0) {
            new File(wavePath).delete();
            throw new IOException("SoundFont MIDI renderer failed with code " + durationMillis);
        }
        return new Result(null, wavePath, durationMillis);
    }

    public static Result renderFile(String midiPath) throws IOException {
        String wavePath = "/tmp/j2me-soundfont-" + nextId() + ".wav";
        int durationMillis = renderFileToWave(cString(midiPath), cString(SOUNDFONT_PATH), cString(wavePath));
        if (durationMillis < 0) {
            new File(wavePath).delete();
            throw new IOException("SoundFont MIDI renderer failed with code " + durationMillis);
        }
        return new Result(null, wavePath, durationMillis);
    }

    private static synchronized int nextId() {
        return nextFileId++;
    }

    private static byte[] cString(String value) throws IOException {
        byte[] encoded = value.getBytes("UTF-8");
        byte[] terminated = new byte[encoded.length + 1];
        System.arraycopy(encoded, 0, terminated, 0, encoded.length);
        return terminated;
    }

    private static native int renderToWave(byte[] midiData, byte[] soundfontPath, byte[] wavePath);
    private static native int renderFileToWave(byte[] midiPath, byte[] soundfontPath, byte[] wavePath);

    public static final class Result {
        public final byte[] waveData;
        public final String wavePath;
        public final long durationMicros;

        Result(byte[] waveData, String wavePath, int durationMillis) {
            this.waveData = waveData;
            this.wavePath = wavePath;
            this.durationMicros = durationMillis * 1000L;
        }
    }

    private static final class CacheEntry {
        Result result;
        IOException failure;
        boolean ready;
    }

    private static final class MidiKey {
        private final byte[] data;
        private final int hashCode;

        MidiKey(byte[] data) {
            this.data = data.clone();
            this.hashCode = Arrays.hashCode(this.data);
        }

        public int hashCode() { return hashCode; }

        public boolean equals(Object value) {
            return value instanceof MidiKey && Arrays.equals(data, ((MidiKey) value).data);
        }
    }
}
