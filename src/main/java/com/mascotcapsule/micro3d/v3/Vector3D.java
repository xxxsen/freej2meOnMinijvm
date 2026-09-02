/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.base.MathUtil;

public class Vector3D {
    public int x;
    public int y;
    public int z;

    public Vector3D() {
    }

    public Vector3D(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public final int getX() {
        return this.x;
    }

    public final int getY() {
        return this.y;
    }

    public final int getZ() {
        return this.z;
    }

    public final int innerProduct(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public static int innerProduct(Vector3D v1, Vector3D v2) {
        if (v1 == null) {
            throw new NullPointerException();
        }
        return v1.innerProduct(v2);
    }

    public final void outerProduct(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        int x = this.x;
        int y = this.y;
        int z = this.z;
        this.x = y * v.z - z * v.y;
        this.y = z * v.x - x * v.z;
        this.z = x * v.y - y * v.x;
    }

    public static Vector3D outerProduct(Vector3D v1, Vector3D v2) {
        if (v1 == null || v2 == null) {
            throw new NullPointerException();
        }
        int x = v1.y * v2.z - v1.z * v2.y;
        int y = v1.z * v2.x - v1.x * v2.z;
        int z = v1.x * v2.y - v1.y * v2.x;
        return new Vector3D(x, y, z);
    }

    public final void set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public final void set(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public final void setX(int x) {
        this.x = x;
    }

    public final void setY(int y) {
        this.y = y;
    }

    public final void setZ(int z) {
        this.z = z;
    }

    public final void unit() {
        int x = this.x;
        int y = this.y;
        int z = this.z;
        int shift = Integer.numberOfLeadingZeros(Math.abs(x) | Math.abs(y) | Math.abs(z)) - 17;
        if (shift > 0) {
            x <<= shift;
            y <<= shift;
            z <<= shift;
        } else if (shift < 0) {
            shift = -shift;
            x >>= shift;
            y >>= shift;
            z >>= shift;
        }
        int i = MathUtil.uSqrt(x * x + y * y + z * z);
        if (i != 0) {
            this.x = (x << 12) / i;
            this.y = (y << 12) / i;
            this.z = (z << 12) / i;
        } else {
            this.x = 0;
            this.y = 0;
            this.z = 4096;
        }
    }

    public void cross(Vector3D value) {
        outerProduct(value);
    }

    public static Vector3D cross(Vector3D left, Vector3D right) {
        return outerProduct(left, right);
    }

    public int dot(Vector3D value) {
        return innerProduct(value);
    }

    public static int dot(Vector3D left, Vector3D right) {
        return innerProduct(left, right);
    }

    public void normalize() {
        unit();
    }
}


