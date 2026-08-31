package java.awt.image;

public abstract class DataBuffer {
    public static final int TYPE_INT = 3;

    protected final int dataType;
    protected final int size;

    protected DataBuffer(int dataType, int size) {
        this.dataType = dataType;
        this.size = size;
    }

    public int getDataType() {
        return dataType;
    }

    public int getSize() {
        return size;
    }
}
