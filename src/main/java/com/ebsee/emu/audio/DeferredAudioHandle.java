package com.ebsee.emu.audio;

import org.recompile.mobile.MiniJvmAudioBackend;

/**
 * Renders expensive media away from the MIDlet thread while preserving all
 * player operations issued before the rendered handle becomes available.
 */
final class DeferredAudioHandle implements MiniJvmAudioBackend.Handle {
    interface Renderer {
        MiniJvmAudioBackend.Handle render() throws Exception;
    }

    private MiniJvmAudioBackend.Handle delegate;
    private boolean startRequested;
    private boolean closed;
    private int loopCount;
    private int volume = 100;
    private long mediaTime;

    DeferredAudioHandle(final Renderer renderer) {
        Thread worker = new Thread(new Runnable() {
            public void run() {
                try {
                    install(renderer.render());
                } catch (Throwable failure) {
                    System.out.println("[audio] asynchronous media setup failed: " + failure);
                }
            }
        }, "j2me-audio-renderer");
        worker.setDaemon(true);
        worker.start();
    }

    private synchronized void install(MiniJvmAudioBackend.Handle rendered) {
        if (rendered == null) return;
        if (closed) {
            rendered.close();
            return;
        }
        delegate = rendered;
        delegate.setLoopCount(loopCount);
        delegate.setVolume(volume);
        delegate.setMediaTime(mediaTime);
        if (startRequested) delegate.start();
    }

    public synchronized void start() {
        startRequested = true;
        if (delegate != null) delegate.start();
    }

    public synchronized void stop() {
        startRequested = false;
        if (delegate != null) delegate.stop();
    }

    public synchronized void close() {
        if (closed) return;
        closed = true;
        startRequested = false;
        if (delegate != null) delegate.close();
        delegate = null;
    }

    public synchronized void setLoopCount(int count) {
        loopCount = count;
        if (delegate != null) delegate.setLoopCount(count);
    }

    public synchronized long setMediaTime(long value) {
        mediaTime = value;
        return delegate == null ? value : delegate.setMediaTime(value);
    }

    public synchronized long getMediaTime() {
        return delegate == null ? mediaTime : delegate.getMediaTime();
    }

    public synchronized long getDuration() {
        return delegate == null ? -1L : delegate.getDuration();
    }

    public synchronized boolean isRunning() {
        return delegate == null ? startRequested : delegate.isRunning();
    }

    public synchronized void setVolume(int level) {
        volume = level;
        if (delegate != null) delegate.setVolume(level);
    }
}
