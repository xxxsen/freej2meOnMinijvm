package java.awt.geom;

import java.awt.Shape;

public abstract class Line2D implements Shape, Cloneable {
    protected Line2D() {
    }

    public abstract double getX1();
    public abstract double getY1();
    public abstract double getX2();
    public abstract double getY2();
}
