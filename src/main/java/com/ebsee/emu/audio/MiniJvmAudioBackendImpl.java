package com.ebsee.emu.audio;

import java.io.File;
import java.io.FileInputStream;

import org.recompile.mobile.MiniJvmAudioBackend;

/** miniJVM media backend backed by TinySoundFont and browser Web Audio. */
public final class MiniJvmAudioBackendImpl implements MiniJvmAudioBackend {
    public MiniJvmAudioBackendImpl() {
        /* miniJVM links the MIDI renderer and its deferred handle lazily. That
           first linkage is expensive enough to block some old MIDlets in
           createPlayer(), so pay it once while the backend is installed. */
        try {
            create(new byte[] { 'M', 'T', 'h', 'd' }, false).close();
        } catch (Exception failure) {
            System.out.println("[audio] MIDI backend warm-up failed: " + failure);
        }
    }

    public Handle create(byte[] data) throws Exception {
        return create(data, true);
    }

    private Handle create(byte[] data, boolean prepare) throws Exception {
        final byte[] media = data;
        if (!isMidi(media)) return new BrowserAudioHandle(media, -1);
        return new DeferredAudioHandle(new DeferredAudioHandle.Renderer() {
            public Handle render() throws Exception {
                SoundFontSynth.Result rendered = SoundFontSynth.render(media);
                System.out.println("[audio] SoundFont MIDI rendered");
                return new BrowserAudioHandle(rendered.waveData, rendered.durationMicros);
            }
        }, prepare);
    }

    public Handle createFile(final String path) throws Exception {
        byte[] media = readFile(path);
        new File(path).delete();
        return create(media);
    }

    public void playTone(int note, int duration, int volume) throws Exception {
        new BrowserAudioHandle(createToneWave(note, duration, volume), duration * 1000L).start();
    }

    private static boolean isMidi(byte[] data) {
        return data.length >= 4 && data[0] == 'M' && data[1] == 'T'
                && data[2] == 'h' && data[3] == 'd';
    }

    private static byte[] readFile(String path) throws Exception {
        File file = new File(path);
        long length = file.length();
        if (length <= 0 || length > Integer.MAX_VALUE) {
            throw new java.io.IOException("Invalid media file length: " + length);
        }
        FileInputStream input = new FileInputStream(file);
        try {
            return ExactLengthReader.read(input, (int) length);
        } finally {
            input.close();
        }
    }

    private static byte[] createToneWave(int note, int duration, int volume) {
        final int sampleRate = 22050;
        int frames = Math.max(1, duration * sampleRate / 1000);
        int dataSize = frames * 2;
        byte[] wave = new byte[44 + dataSize];
        writeAscii(wave, 0, "RIFF");
        writeIntLE(wave, 4, 36 + dataSize);
        writeAscii(wave, 8, "WAVEfmt ");
        writeIntLE(wave, 16, 16);
        writeShortLE(wave, 20, 1);
        writeShortLE(wave, 22, 1);
        writeIntLE(wave, 24, sampleRate);
        writeIntLE(wave, 28, sampleRate * 2);
        writeShortLE(wave, 32, 2);
        writeShortLE(wave, 34, 16);
        writeAscii(wave, 36, "data");
        writeIntLE(wave, 40, dataSize);
        double frequency = 440.0 * Math.pow(2.0, (note - 69) / 12.0);
        double gain = Math.max(0, Math.min(100, volume)) / 100.0 * 0.28;
        for (int i = 0; i < frames; i++) {
            double envelope = Math.min(1.0, (frames - i) / (sampleRate * 0.015));
            short sample = (short) (Math.sin(i * frequency * Math.PI * 2.0 / sampleRate) * 32767.0 * gain * envelope);
            writeShortLE(wave, 44 + i * 2, sample);
        }
        return wave;
    }

    private static void writeAscii(byte[] target, int offset, String value) {
        for (int i = 0; i < value.length(); i++) target[offset + i] = (byte) value.charAt(i);
    }

    private static void writeShortLE(byte[] target, int offset, int value) {
        target[offset] = (byte) value;
        target[offset + 1] = (byte) (value >>> 8);
    }

    private static void writeIntLE(byte[] target, int offset, int value) {
        target[offset] = (byte) value;
        target[offset + 1] = (byte) (value >>> 8);
        target[offset + 2] = (byte) (value >>> 16);
        target[offset + 3] = (byte) (value >>> 24);
    }

}
