package com.ebsee.emu.audio;

import java.io.File;
import java.io.FileInputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;

import org.recompile.mobile.MiniJvmAudioBackend;

/** miniJVM media backend backed by TinySoundFont and miniaudio. */
public final class MiniJvmAudioBackendImpl implements MiniJvmAudioBackend {
    public Handle create(byte[] data) throws Exception {
        if (!isMidi(data)) return new ClipHandle(data);
        SoundFontSynth.Result rendered = SoundFontSynth.renderToFile(data);
        System.out.println("[audio] SoundFont MIDI rendered");
        return new ClipHandle(rendered.wavePath, rendered.durationMicros, true);
    }

    public Handle createFile(String path) throws Exception {
        if (!isMidi(path)) return new ClipHandle(path, -1, true);
        try {
            SoundFontSynth.Result rendered = SoundFontSynth.renderFile(path);
            System.out.println("[audio] SoundFont MIDI rendered");
            return new ClipHandle(rendered.wavePath, rendered.durationMicros, true);
        } finally {
            new File(path).delete();
        }
    }

    public void playTone(int note, int duration, int volume) throws Exception {
        new ClipHandle(createToneWave(note, duration, volume)).start();
    }

    private static boolean isMidi(byte[] data) {
        return data.length >= 4 && data[0] == 'M' && data[1] == 'T'
                && data[2] == 'h' && data[3] == 'd';
    }

    private static boolean isMidi(String path) throws Exception {
        FileInputStream input = new FileInputStream(path);
        try {
            return input.read() == 'M' && input.read() == 'T'
                    && input.read() == 'h' && input.read() == 'd';
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

    private static final class ClipHandle implements Handle {
        private final Clip clip = new Clip();
        private final String ownedPath;
        private final long durationMicros;

        ClipHandle(byte[] data) throws Exception {
            ownedPath = null;
            durationMicros = -1;
            clip.open(new AudioInputStream(data));
        }

        ClipHandle(String path, long durationMicros, boolean ownFile) throws Exception {
            this.ownedPath = ownFile ? path : null;
            this.durationMicros = durationMicros;
            clip.open(path);
        }

        public void start() { clip.start(); }
        public void stop() { clip.stop(); }
        public void close() {
            clip.close();
            if (ownedPath != null) new File(ownedPath).delete();
        }
        public void setLoopCount(int count) { clip.loop(count < 0 ? Clip.LOOP_CONTINUOUSLY : Math.max(0, count - 1)); }
        public long setMediaTime(long value) { clip.setMicrosecondPosition(value); return clip.getMicrosecondPosition(); }
        public long getMediaTime() { return clip.getMicrosecondPosition(); }
        public long getDuration() { return durationMicros >= 0 ? durationMicros : clip.getMicrosecondLength(); }
        public boolean isRunning() { return clip.isRunning(); }
        public void setVolume(int level) { clip.setVolume(level / 100.0f); }
    }
}
