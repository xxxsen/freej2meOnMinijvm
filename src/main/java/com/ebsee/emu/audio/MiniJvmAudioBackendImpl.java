package com.ebsee.emu.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;

import org.mini.fs.InnerFile;
import org.mini.net.SocketNative;
import org.recompile.mobile.MiniJvmAudioBackend;

/** miniJVM media backend backed by TinySoundFont and miniaudio. */
public final class MiniJvmAudioBackendImpl implements MiniJvmAudioBackend {
    private static int nextFileId;

    public Handle create(InputStream stream) throws Exception {
        String sourcePath = copyToTemp(stream, ".media");
        if (!isMidi(sourcePath)) return new ClipHandle(sourcePath, -1, true);
        try {
            SoundFontSynth.Result rendered = SoundFontSynth.renderFile(sourcePath);
            return new ClipHandle(rendered.wavePath, rendered.durationMicros, true);
        } finally {
            new File(sourcePath).delete();
        }
    }

    public void playTone(int note, int duration, int volume) throws Exception {
        new ClipHandle(createToneWave(note, duration, volume)).start();
    }

    private static synchronized int nextId() {
        return nextFileId++;
    }

    private static String copyToTemp(InputStream input, String suffix) throws Exception {
        String path = "/tmp/j2me-audio-" + nextId() + suffix;
        long output = InnerFile.openFile(SocketNative.toCStyle(path), SocketNative.toCStyle("wb"));
        if (output == 0) throw new IOException("cannot create media file: " + path);
        try {
            // Some MIDlet resource streams inherit InputStream's byte-at-a-time
            // bulk read. Keep each call below miniJVM's compact operand stack.
            byte[] buffer = new byte[256];
            while (true) {
                // ByteArrayInputStream and JAR resource streams report the
                // exact remaining length. Stop there because a few old MIDlet
                // streams return zero bytes instead of -1 after their slice.
                int remaining = input.available();
                if (remaining <= 0) break;
                int count = input.read(buffer, 0, Math.min(buffer.length, remaining));
                if (count <= 0) break;
                if (count > buffer.length) throw new IOException("invalid media read length: " + count);
                int offset = 0;
                while (offset < count) {
                    int wrote = InnerFile.writebuf(output, buffer, offset, count - offset);
                    if (wrote <= 0) throw new IOException("cannot write media file: " + path);
                    offset += wrote;
                }
            }
        } finally {
            InnerFile.closeFile(output);
        }
        return path;
    }

    private static boolean isMidi(String path) throws Exception {
        FileInputStream input = new FileInputStream(path);
        try {
            return input.read() == 'M' && input.read() == 'T' && input.read() == 'h' && input.read() == 'd';
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
