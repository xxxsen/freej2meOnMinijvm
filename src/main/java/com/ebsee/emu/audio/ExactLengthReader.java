package com.ebsee.emu.audio;

import java.io.IOException;
import java.io.InputStream;

/** Reads a known byte range without probing miniJVM's unreliable EOF boundary. */
final class ExactLengthReader {
    private ExactLengthReader() {
    }

    static byte[] read(InputStream input, int length) throws IOException {
        if (input == null || length <= 0) throw new IllegalArgumentException("input/length");
        byte[] bytes = new byte[length];
        int offset = 0;
        while (offset < bytes.length) {
            int count = input.read(bytes, offset, bytes.length - offset);
            if (count <= 0) throw new IOException("Unexpected end of media file");
            offset += count;
        }
        return bytes;
    }
}
