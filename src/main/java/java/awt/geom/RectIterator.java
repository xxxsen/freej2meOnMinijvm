package java.awt.geom;

final class RectIterator implements PathIterator {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final AffineTransform transform;
    private int index;

    RectIterator(Rectangle2D rectangle, AffineTransform transform) {
        x = rectangle.getX();
        y = rectangle.getY();
        width = rectangle.getWidth();
        height = rectangle.getHeight();
        this.transform = transform;
        if (width < 0 || height < 0) index = 6;
    }

    public int getWindingRule() {
        return WIND_NON_ZERO;
    }

    public boolean isDone() {
        return index > 5;
    }

    public void next() {
        index++;
    }

    public int currentSegment(float[] coordinates) {
        double[] values = new double[2];
        int type = segment(values);
        coordinates[0] = (float) values[0];
        coordinates[1] = (float) values[1];
        return type;
    }

    public int currentSegment(double[] coordinates) {
        return segment(coordinates);
    }

    private int segment(double[] coordinates) {
        if (isDone()) throw new java.util.NoSuchElementException("rectangle iterator out of bounds");
        if (index == 5) return SEG_CLOSE;

        coordinates[0] = x;
        coordinates[1] = y;
        if (index == 1 || index == 2) coordinates[0] += width;
        if (index == 2 || index == 3) coordinates[1] += height;
        if (transform != null) transform.transform(coordinates, 0, coordinates, 0, 1);
        return index == 0 ? SEG_MOVETO : SEG_LINETO;
    }
}
