package java.awt.image;

public class Raster {
    protected DataBuffer dataBuffer;

    protected Raster(DataBuffer dataBuffer) {
        this.dataBuffer = dataBuffer;
    }

    public DataBuffer getDataBuffer() {
        return dataBuffer;
    }
}
