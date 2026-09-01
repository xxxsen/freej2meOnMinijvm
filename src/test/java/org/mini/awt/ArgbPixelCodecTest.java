package org.mini.awt;

public final class ArgbPixelCodecTest {
    public static void main(String[] args) {
        byte[] nativeRgba = new byte[8];
        ArgbPixelCodec.write(nativeRgba, 0, 0xffd12a73);
        assertByte(nativeRgba[0], 0xd1, "red");
        assertByte(nativeRgba[1], 0x2a, "green");
        assertByte(nativeRgba[2], 0x73, "blue");
        assertByte(nativeRgba[3], 0xff, "alpha");
        if (ArgbPixelCodec.read(nativeRgba, 0) != 0xffd12a73) {
            throw new AssertionError("RGBA bytes must round-trip as ARGB");
        }
    }

    private static void assertByte(byte actual, int expected, String channel) {
        if ((actual & 0xff) != expected) {
            throw new AssertionError(channel + " channel: expected " + expected + ", got " + (actual & 0xff));
        }
    }
}
