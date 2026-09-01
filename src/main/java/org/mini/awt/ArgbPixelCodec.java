package org.mini.awt;

/** Converts Java's packed ARGB integers to miniJVM ImageMutable RGBA bytes. */
public final class ArgbPixelCodec {
    private ArgbPixelCodec() { }

    public static int read(byte[] rgba, int offset) {
        return ((rgba[offset + 3] & 0xff) << 24)
                | ((rgba[offset] & 0xff) << 16)
                | ((rgba[offset + 1] & 0xff) << 8)
                | (rgba[offset + 2] & 0xff);
    }

    public static void write(byte[] rgba, int offset, int argb) {
        rgba[offset] = (byte) ((argb >>> 16) & 0xff);
        rgba[offset + 1] = (byte) ((argb >>> 8) & 0xff);
        rgba[offset + 2] = (byte) (argb & 0xff);
        rgba[offset + 3] = (byte) ((argb >>> 24) & 0xff);
    }
}
