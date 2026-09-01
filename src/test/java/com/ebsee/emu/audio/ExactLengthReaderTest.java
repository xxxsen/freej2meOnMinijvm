package com.ebsee.emu.audio;

import java.io.IOException;
import java.io.InputStream;

public final class ExactLengthReaderTest {
    public static void main(String[] args) throws Exception {
        BoundaryStream stream = new BoundaryStream(new byte[] { 1, 2, 3, 4 });
        byte[] bytes = ExactLengthReader.read(stream, 4);
        if (bytes.length != 4 || bytes[0] != 1 || bytes[3] != 4 || stream.readCalls != 2) {
            throw new AssertionError("reader must stop at the known file boundary");
        }

        try {
            ExactLengthReader.read(new BoundaryStream(new byte[] { 1, 2 }), 3);
            throw new AssertionError("truncated files must fail closed");
        } catch (IOException expected) {
            // Expected.
        }
    }

    private static final class BoundaryStream extends InputStream {
        private final byte[] bytes;
        private int position;
        private int readCalls;

        BoundaryStream(byte[] bytes) { this.bytes = bytes; }

        public int read() throws IOException {
            throw new AssertionError("single-byte EOF probing is not allowed");
        }

        public int read(byte[] target, int offset, int length) {
            readCalls++;
            if (position >= bytes.length) return -1;
            int count = Math.min(2, Math.min(length, bytes.length - position));
            System.arraycopy(bytes, position, target, offset, count);
            position += count;
            return count;
        }
    }
}
