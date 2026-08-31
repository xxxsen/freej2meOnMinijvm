package java.awt.image;

public final class DataBufferInt extends DataBuffer {
    private final int[] data;

    public DataBufferInt(int size) {
        this(new int[size], size);
    }

    public DataBufferInt(int[] data, int size) {
        super(TYPE_INT, size);
        this.data = data;
    }

    public int[] getData() {
        return data;
    }
}
