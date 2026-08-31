package com.ebsee.emu.audio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Compact Standard MIDI File renderer for the browser port. It deliberately
 * uses a small General-MIDI-style oscillator bank so the runtime stays fully
 * self-contained and does not need a multi-megabyte SoundFont download.
 */
public final class MidiSynth {
    private static final int SAMPLE_RATE = 22050;
    private static final int TABLE_SIZE = 2048;
    private static final int MAX_SECONDS = 180;
    private static final short[] SINE = new short[TABLE_SIZE];

    static {
        for (int i = 0; i < TABLE_SIZE; i++) {
            SINE[i] = (short) (Math.sin(i * Math.PI * 2.0 / TABLE_SIZE) * 32767.0);
        }
    }

    private MidiSynth() {
    }

    public static Result render(InputStream input) throws IOException {
        byte[] data = readAll(input);
        Cursor cursor = new Cursor(data);
        if (cursor.readInt() != 0x4d546864) throw new IOException("Invalid MIDI header");
        int headerLength = cursor.readInt();
        if (headerLength < 6) throw new IOException("Invalid MIDI header length");
        cursor.readUnsignedShort(); // format
        int trackCount = cursor.readUnsignedShort();
        int division = cursor.readUnsignedShort();
        cursor.skip(headerLength - 6);
        if ((division & 0x8000) != 0 || division == 0) {
            throw new IOException("SMPTE MIDI timing is not supported");
        }

        List<RawEvent> events = new ArrayList<RawEvent>();
        long lastTrackTick = 0;
        int order = 0;
        for (int track = 0; track < trackCount && cursor.remaining() >= 8; track++) {
            int chunk = cursor.readInt();
            int length = cursor.readInt();
            if (chunk != 0x4d54726b || length < 0 || length > cursor.remaining()) {
                throw new IOException("Invalid MIDI track");
            }
            int end = cursor.position + length;
            long tick = 0;
            int runningStatus = 0;
            while (cursor.position < end) {
                tick += cursor.readVariable(end);
                if (tick > lastTrackTick) lastTrackTick = tick;
                int status = cursor.readUnsignedByte(end);
                if (status < 0x80) {
                    if (runningStatus == 0) throw new IOException("Invalid MIDI running status");
                    cursor.position--;
                    status = runningStatus;
                } else if (status < 0xf0) {
                    runningStatus = status;
                }

                if (status == 0xff) {
                    int type = cursor.readUnsignedByte(end);
                    int metaLength = cursor.readVariable(end);
                    if (metaLength < 0 || cursor.position + metaLength > end) throw new IOException("Invalid MIDI meta event");
                    if (type == 0x51 && metaLength == 3) {
                        int tempo = (cursor.data[cursor.position] & 0xff) << 16 |
                                (cursor.data[cursor.position + 1] & 0xff) << 8 |
                                (cursor.data[cursor.position + 2] & 0xff);
                        events.add(new RawEvent(tick, RawEvent.TEMPO, 0, tempo, 0, order++));
                    }
                    cursor.position += metaLength;
                    if (type == 0x2f) break;
                    continue;
                }
                if (status == 0xf0 || status == 0xf7) {
                    int sysexLength = cursor.readVariable(end);
                    cursor.skipWithin(sysexLength, end);
                    continue;
                }
                if (status >= 0xf0) {
                    // Rare system-common messages are ignored conservatively.
                    int systemLength = status == 0xf1 || status == 0xf3 ? 1 : status == 0xf2 ? 2 : 0;
                    cursor.skipWithin(systemLength, end);
                    continue;
                }

                int command = status & 0xf0;
                int channel = status & 0x0f;
                int first = cursor.readUnsignedByte(end);
                int second = command == 0xc0 || command == 0xd0 ? 0 : cursor.readUnsignedByte(end);
                if (command == 0x80 || (command == 0x90 && second == 0)) {
                    events.add(new RawEvent(tick, RawEvent.NOTE_OFF, channel, first, second, order++));
                } else if (command == 0x90) {
                    events.add(new RawEvent(tick, RawEvent.NOTE_ON, channel, first, second, order++));
                } else if (command == 0xc0) {
                    events.add(new RawEvent(tick, RawEvent.PROGRAM, channel, first, 0, order++));
                }
            }
            cursor.position = end;
        }

        Collections.sort(events, new Comparator<RawEvent>() {
            public int compare(RawEvent left, RawEvent right) {
                if (left.tick < right.tick) return -1;
                if (left.tick > right.tick) return 1;
                int leftPriority = left.kind == RawEvent.NOTE_OFF ? 0 : left.kind == RawEvent.TEMPO ? 1 : left.kind == RawEvent.PROGRAM ? 2 : 3;
                int rightPriority = right.kind == RawEvent.NOTE_OFF ? 0 : right.kind == RawEvent.TEMPO ? 1 : right.kind == RawEvent.PROGRAM ? 2 : 3;
                if (leftPriority != rightPriority) return leftPriority - rightPriority;
                return left.order - right.order;
            }
        });

        long tempo = 500000;
        long currentTick = 0;
        long currentMicros = 0;
        for (int i = 0; i < events.size(); i++) {
            RawEvent event = events.get(i);
            currentMicros += (event.tick - currentTick) * tempo / division;
            currentTick = event.tick;
            event.micros = currentMicros;
            if (event.kind == RawEvent.TEMPO && event.a > 0) tempo = event.a;
        }
        long endMicros = currentMicros + (lastTrackTick - currentTick) * tempo / division;

        int[] programs = new int[16];
        ActiveNote[] active = new ActiveNote[16 * 128];
        List<Note> notes = new ArrayList<Note>();
        for (int i = 0; i < events.size(); i++) {
            RawEvent event = events.get(i);
            if (event.kind == RawEvent.PROGRAM) {
                programs[event.channel] = event.a;
            } else if (event.kind == RawEvent.NOTE_ON) {
                int key = event.channel * 128 + event.a;
                if (active[key] != null) finishNote(notes, active[key], event.micros);
                active[key] = new ActiveNote(event.micros, event.channel, event.a, event.b, programs[event.channel]);
            } else if (event.kind == RawEvent.NOTE_OFF) {
                int key = event.channel * 128 + event.a;
                if (active[key] != null) {
                    finishNote(notes, active[key], event.micros);
                    active[key] = null;
                }
            }
        }
        for (int i = 0; i < active.length; i++) {
            if (active[i] != null) finishNote(notes, active[i], endMicros);
        }

        long limitedMicros = Math.min(Math.max(endMicros + 120000, 250000), MAX_SECONDS * 1000000L);
        int sampleCount = (int) (limitedMicros * SAMPLE_RATE / 1000000L);
        int[] mix = new int[sampleCount];
        for (int i = 0; i < notes.size(); i++) renderNote(mix, notes.get(i));
        byte[] wave = createWave(mix);
        return new Result(wave, lastTrackTick, limitedMicros);
    }

    private static void finishNote(List<Note> notes, ActiveNote active, long endMicros) {
        long safeEnd = Math.max(active.startMicros + 1000, endMicros);
        notes.add(new Note(active.startMicros, safeEnd, active.channel, active.key, active.velocity, active.program));
    }

    private static void renderNote(int[] mix, Note note) {
        int start = (int) Math.min(mix.length, note.startMicros * SAMPLE_RATE / 1000000L);
        int end = (int) Math.min(mix.length, note.endMicros * SAMPLE_RATE / 1000000L);
        if (end <= start) return;
        int length = end - start;
        int attack = Math.min(length / 4, SAMPLE_RATE / 80);
        int release = Math.min(length / 3, SAMPLE_RATE / 12);
        double frequency = 440.0 * Math.pow(2.0, (note.key - 69) / 12.0);
        double phase = 0;
        double step = frequency * TABLE_SIZE / SAMPLE_RATE;
        int amplitude = note.velocity * (note.channel == 9 ? 28 : 22);
        int noise = note.key * 1103515245 + 12345;

        for (int i = 0; i < length; i++) {
            int envelope = 1024;
            if (attack > 0 && i < attack) envelope = i * 1024 / attack;
            if (release > 0 && i > length - release) envelope = (length - i) * 1024 / release;
            int value;
            if (note.channel == 9) {
                noise = noise * 1103515245 + 12345;
                value = (noise >> 16) & 0xffff;
                value -= 32768;
            } else {
                int index = ((int) phase) & (TABLE_SIZE - 1);
                int family = note.program >> 3;
                if (family == 4 || family == 5 || family == 6) {
                    int triangle = index < TABLE_SIZE / 2 ? index : TABLE_SIZE - index;
                    value = triangle * 64 - 32768;
                } else if (family == 3 || family == 10) {
                    value = index < TABLE_SIZE / 2 ? 24576 : -24576;
                } else {
                    value = SINE[index];
                    if (family == 0 || family == 1) value = (value * 3 + SINE[(index * 2) & (TABLE_SIZE - 1)]) / 4;
                }
                phase += step;
            }
            // Keep the intermediate in 64 bits. At full MIDI velocity the
            // 16-bit oscillator * gain * envelope product exceeds Integer.MAX_VALUE;
            // overflowing here turns a tone into broadband noise.
            mix[start + i] += (int) ((long) value * amplitude * envelope / (32768L * 1024L));
        }
    }

    private static byte[] createWave(int[] mix) {
        byte[] output = new byte[44 + mix.length * 2];
        writeAscii(output, 0, "RIFF");
        writeIntLE(output, 4, output.length - 8);
        writeAscii(output, 8, "WAVE");
        writeAscii(output, 12, "fmt ");
        writeIntLE(output, 16, 16);
        writeShortLE(output, 20, 1);
        writeShortLE(output, 22, 1);
        writeIntLE(output, 24, SAMPLE_RATE);
        writeIntLE(output, 28, SAMPLE_RATE * 2);
        writeShortLE(output, 32, 2);
        writeShortLE(output, 34, 16);
        writeAscii(output, 36, "data");
        writeIntLE(output, 40, mix.length * 2);
        for (int i = 0; i < mix.length; i++) {
            int sample = Math.max(-32768, Math.min(32767, mix[i]));
            writeShortLE(output, 44 + i * 2, sample);
        }
        return output;
    }

    private static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = input.read(buffer)) >= 0) {
            if (count > 0) output.write(buffer, 0, count);
        }
        return output.toByteArray();
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

    public static final class Result {
        public final byte[] waveData;
        public final long tickLength;
        public final long durationMicros;

        Result(byte[] waveData, long tickLength, long durationMicros) {
            this.waveData = waveData;
            this.tickLength = tickLength;
            this.durationMicros = durationMicros;
        }
    }

    private static final class RawEvent {
        static final int TEMPO = 0;
        static final int PROGRAM = 1;
        static final int NOTE_ON = 2;
        static final int NOTE_OFF = 3;
        final long tick;
        final int kind;
        final int channel;
        final int a;
        final int b;
        final int order;
        long micros;

        RawEvent(long tick, int kind, int channel, int a, int b, int order) {
            this.tick = tick;
            this.kind = kind;
            this.channel = channel;
            this.a = a;
            this.b = b;
            this.order = order;
        }
    }

    private static class ActiveNote {
        final long startMicros;
        final int channel;
        final int key;
        final int velocity;
        final int program;

        ActiveNote(long startMicros, int channel, int key, int velocity, int program) {
            this.startMicros = startMicros;
            this.channel = channel;
            this.key = key;
            this.velocity = velocity;
            this.program = program;
        }
    }

    private static final class Note extends ActiveNote {
        final long endMicros;

        Note(long startMicros, long endMicros, int channel, int key, int velocity, int program) {
            super(startMicros, channel, key, velocity, program);
            this.endMicros = endMicros;
        }
    }

    private static final class Cursor {
        final byte[] data;
        int position;

        Cursor(byte[] data) {
            this.data = data;
        }

        int remaining() {
            return data.length - position;
        }

        int readInt() throws IOException {
            if (remaining() < 4) throw new IOException("Unexpected end of MIDI data");
            return (data[position++] & 0xff) << 24 | (data[position++] & 0xff) << 16 |
                    (data[position++] & 0xff) << 8 | data[position++] & 0xff;
        }

        int readUnsignedShort() throws IOException {
            if (remaining() < 2) throw new IOException("Unexpected end of MIDI data");
            return (data[position++] & 0xff) << 8 | data[position++] & 0xff;
        }

        int readUnsignedByte(int end) throws IOException {
            if (position >= end || position >= data.length) throw new IOException("Unexpected end of MIDI track");
            return data[position++] & 0xff;
        }

        int readVariable(int end) throws IOException {
            int value = 0;
            for (int i = 0; i < 4; i++) {
                int next = readUnsignedByte(end);
                value = (value << 7) | (next & 0x7f);
                if ((next & 0x80) == 0) return value;
            }
            throw new IOException("Invalid MIDI variable-length value");
        }

        void skip(int count) throws IOException {
            if (count < 0 || count > remaining()) throw new IOException("Unexpected end of MIDI data");
            position += count;
        }

        void skipWithin(int count, int end) throws IOException {
            if (count < 0 || position + count > end) throw new IOException("Unexpected end of MIDI track");
            position += count;
        }
    }
}
