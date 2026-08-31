package javax.sound.sampled;

import java.io.IOException;
import java.io.InputStream;

/**
 * Small in-memory AudioInputStream used by the miniJVM audio adapter.
 */
public class AudioInputStream extends InputStream {
    private final byte[] data;
    private int position;

    public AudioInputStream() {
        this(new byte[0]);
    }

    public AudioInputStream(byte[] data) {
        this.data = data == null ? new byte[0] : data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public int read() throws IOException {
        return position < data.length ? data[position++] & 0xff : -1;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (buffer == null) throw new NullPointerException();
        if (offset < 0 || length < 0 || offset + length > buffer.length) {
            throw new IndexOutOfBoundsException();
        }
        if (position >= data.length) return -1;
        int count = Math.min(length, data.length - position);
        System.arraycopy(data, position, buffer, offset, count);
        position += count;
        return count;
    }

    @Override
    public int available() {
        return data.length - position;
    }

    @Override
    public synchronized void reset() {
        position = 0;
    }
}
