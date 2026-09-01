package com.ebsee.emu.audio;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.recompile.mobile.MiniJvmAudioBackend;

public final class DeferredAudioHandleTest {
    public static void main(String[] args) throws Exception {
        final CountDownLatch rendering = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final RecordingHandle rendered = new RecordingHandle();

        long startedAt = System.currentTimeMillis();
        DeferredAudioHandle handle = new DeferredAudioHandle(new DeferredAudioHandle.Renderer() {
            public MiniJvmAudioBackend.Handle render() throws Exception {
                rendering.countDown();
                if (!release.await(5, TimeUnit.SECONDS)) throw new AssertionError("renderer timeout");
                return rendered;
            }
        });
        assertTrue(System.currentTimeMillis() - startedAt < 250, "construction must not wait for rendering");
        assertTrue(rendering.await(2, TimeUnit.SECONDS), "renderer did not start");

        handle.setLoopCount(-1);
        handle.setVolume(64);
        handle.setMediaTime(1234L);
        handle.start();
        assertTrue(handle.isRunning(), "queued playback must remain logically running");
        assertTrue(rendered.startCalls == 0, "playback started before rendering finished");

        release.countDown();
        assertTrue(rendered.started.await(2, TimeUnit.SECONDS), "queued playback did not start");
        assertTrue(rendered.loopCount == -1, "loop state was not restored");
        assertTrue(rendered.volume == 64, "volume state was not restored");
        assertTrue(rendered.mediaTime == 1234L, "media time was not restored");
        assertTrue(rendered.startCalls == 1, "queued playback started more than once");

        handle.stop();
        assertTrue(rendered.stopCalls == 1, "stop was not forwarded");
        handle.close();
        assertTrue(rendered.closeCalls == 1, "close was not forwarded");
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static final class RecordingHandle implements MiniJvmAudioBackend.Handle {
        final CountDownLatch started = new CountDownLatch(1);
        int startCalls;
        int stopCalls;
        int closeCalls;
        int loopCount;
        int volume;
        long mediaTime;

        public void start() { startCalls++; started.countDown(); }
        public void stop() { stopCalls++; }
        public void close() { closeCalls++; }
        public void setLoopCount(int count) { loopCount = count; }
        public long setMediaTime(long value) { mediaTime = value; return value; }
        public long getMediaTime() { return mediaTime; }
        public long getDuration() { return 9876L; }
        public boolean isRunning() { return startCalls > stopCalls; }
        public void setVolume(int level) { volume = level; }
    }
}
