package com.ebsee.emu.audio;

import org.recompile.mobile.MiniJvmAudioBackend;

public final class DeferredAudioHandleTest {
    public static void main(String[] args) throws Exception {
        final Calls calls = new Calls();
        DeferredAudioHandle handle = new DeferredAudioHandle(new DeferredAudioHandle.Renderer() {
            public MiniJvmAudioBackend.Handle render() {
                calls.render++;
                return new FakeHandle(calls);
            }
        });

        Thread.sleep(20);
        if (calls.render != 0) throw new AssertionError("rendering must be lazy until Player.start()");
        handle.start();
        long deadline = System.currentTimeMillis() + 1000;
        while (calls.start == 0 && System.currentTimeMillis() < deadline) Thread.sleep(5);
        if (calls.render != 1 || calls.start != 1) {
            throw new AssertionError("one start must render once and start the installed delegate");
        }
    }

    private static final class FakeHandle implements MiniJvmAudioBackend.Handle {
        private final Calls calls;

        FakeHandle(Calls calls) { this.calls = calls; }
        public void start() { calls.start++; }
        public void stop() { }
        public void close() { }
        public void setLoopCount(int count) { }
        public long setMediaTime(long value) { return value; }
        public long getMediaTime() { return 0; }
        public long getDuration() { return 1; }
        public boolean isRunning() { return calls.start > 0; }
        public void setVolume(int level) { }
    }

    private static final class Calls {
        volatile int render;
        volatile int start;
    }
}
