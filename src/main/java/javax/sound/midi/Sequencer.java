package javax.sound.midi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.ebsee.emu.audio.MidiSynth;
import com.ebsee.emu.audio.SoundFontSynth;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;

public class Sequencer {
    public static final int LOOP_CONTINUOUSLY = -1;

    private Clip clip;
    private long tickLength;
    private long durationMicros;
    private int loopCount;

    public void open() {
    }

    public void start() {
        if (clip != null) clip.start();
    }

    public void stop() {
        if (clip != null) clip.stop();
    }

    public void close() {
        if (clip != null) clip.close();
        clip = null;
    }

    public boolean isRunning() {
        return clip != null && clip.isRunning();
    }

    public void setLoopCount(int count) {
        loopCount = count;
        if (clip != null) clip.loop(count);
    }

    public void setSequence(Sequence sequence) {
        // FreeJ2ME provides streams; the object form is retained for API parity.
    }

    public void setSequence(InputStream stream) throws IOException {
        byte[] midiData = readAll(stream);
        byte[] waveData;
        try {
            SoundFontSynth.Result result = SoundFontSynth.render(midiData);
            durationMicros = result.durationMicros;
            // TinySoundFont renders from a millisecond event timeline. Keeping
            // this value monotonic preserves seeking for the J2ME media bridge.
            tickLength = Math.max(1, durationMicros / 1000L);
            waveData = result.waveData;
        } catch (Throwable failure) {
            System.out.println("[audio] SoundFont unavailable, using oscillator fallback: " + failure);
            MidiSynth.Result result = MidiSynth.render(new ByteArrayInputStream(midiData));
            tickLength = result.tickLength;
            durationMicros = result.durationMicros;
            waveData = result.waveData;
        }
        clip = new Clip();
        clip.open(new AudioInputStream(waveData));
        clip.loop(loopCount);
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

    public long getTickPosition() {
        if (clip == null || durationMicros <= 0) return 0;
        return Math.min(tickLength, clip.getMicrosecondPosition() * tickLength / durationMicros);
    }

    public long getTickLength() {
        return tickLength;
    }

    public void setTickPosition(long tick) {
        if (clip == null || tickLength <= 0) return;
        clip.setMicrosecondPosition(Math.max(0, Math.min(tickLength, tick)) * durationMicros / tickLength);
    }
}
