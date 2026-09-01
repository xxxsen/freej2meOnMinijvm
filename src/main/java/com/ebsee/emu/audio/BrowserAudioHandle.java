package com.ebsee.emu.audio;

import org.mini.glfw.Glfw;
import org.recompile.mobile.MiniJvmAudioBackend;

/** Web Audio handle whose decoding and playback live on the browser main thread. */
final class BrowserAudioHandle implements MiniJvmAudioBackend.Handle {
    static {
        Glfw.loadLib();
    }

    private int handle;
    private final long durationMicros;

    BrowserAudioHandle(byte[] media, long durationMicros) {
        this.durationMicros = durationMicros;
        handle = create(media, durationMicros);
        if (handle == 0) throw new RuntimeException("Unable to create browser audio");
    }

    public void start() { if (handle != 0) start(handle); }
    public void stop() { if (handle != 0) stop(handle); }
    public void close() {
        if (handle == 0) return;
        close(handle);
        handle = 0;
    }
    public void setLoopCount(int count) { if (handle != 0) setLoopCount(handle, count); }
    public long setMediaTime(long value) {
        return handle == 0 ? value : setMediaTime(handle, value);
    }
    public long getMediaTime() { return handle == 0 ? 0 : getMediaTime(handle); }
    public long getDuration() {
        if (handle == 0) return durationMicros;
        long value = getDuration(handle);
        return value >= 0 ? value : durationMicros;
    }
    public boolean isRunning() { return handle != 0 && isRunning(handle); }
    public void setVolume(int level) {
        if (handle != 0) setVolume(handle, Math.max(0, Math.min(100, level)) / 100.0f);
    }

    private static native int create(byte[] media, long durationMicros);
    private static native void start(int handle);
    private static native void stop(int handle);
    private static native void close(int handle);
    private static native void setLoopCount(int handle, int count);
    private static native long setMediaTime(int handle, long value);
    private static native long getMediaTime(int handle);
    private static native long getDuration(int handle);
    private static native boolean isRunning(int handle);
    private static native void setVolume(int handle, float volume);
}
