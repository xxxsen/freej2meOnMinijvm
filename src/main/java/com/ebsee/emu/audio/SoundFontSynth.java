package com.ebsee.emu.audio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.mini.glfw.Glfw;

/** Renders Standard MIDI files with TinySoundFont and the bundled GM bank. */
public final class SoundFontSynth {
    private static final String SOUNDFONT_PATH = "/lib/TimGM6mb.sf2";
    private static int nextFileId;

    static {
        Glfw.loadLib();
    }

    private SoundFontSynth() {
    }

    public static Result render(byte[] midiData) throws IOException {
        String wavePath = "/tmp/j2me-soundfont-" + nextId() + ".wav";
        File waveFile = new File(wavePath);
        int durationMillis = renderToWave(midiData, cString(SOUNDFONT_PATH), cString(wavePath));
        if (durationMillis < 0) {
            throw new IOException("SoundFont MIDI renderer failed with code " + durationMillis);
        }

        try {
            FileInputStream input = new FileInputStream(waveFile);
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream((int) Math.min(Integer.MAX_VALUE, waveFile.length()));
                byte[] buffer = new byte[16384];
                int count;
                while ((count = input.read(buffer)) >= 0) {
                    if (count > 0) output.write(buffer, 0, count);
                }
                return new Result(output.toByteArray(), durationMillis);
            } finally {
                input.close();
            }
        } finally {
            waveFile.delete();
        }
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

    public static final class Result {
        public final byte[] waveData;
        public final long durationMicros;

        Result(byte[] waveData, int durationMillis) {
            this.waveData = waveData;
            this.durationMicros = durationMillis * 1000L;
        }
    }
}
