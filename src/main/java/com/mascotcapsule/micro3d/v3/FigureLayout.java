/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.AffineTrans;

public class FigureLayout {
    AffineTrans[] affineArray;
    AffineTrans affine;
    int scaleX;
    int scaleY;
    int centerX;
    int centerY;
    int parallelWidth;
    int parallelHeight;
    int near;
    int far;
    int angle;
    int perspectiveWidth;
    int perspectiveHeight;
    int projection;

    public FigureLayout() {
        this(null, 512, 512, 0, 0);
    }

    public FigureLayout(AffineTrans trans, int sx, int sy, int cx, int cy) {
        this.setAffineTrans(trans);
        this.centerX = cx;
        this.centerY = cy;
        this.setScale(sx, sy);
    }

    public final AffineTrans getAffineTrans() {
        return this.affine;
    }

    public final int getCenterX() {
        return this.centerX;
    }

    public final int getCenterY() {
        return this.centerY;
    }

    public final int getParallelHeight() {
        return this.parallelHeight;
    }

    public final int getParallelWidth() {
        return this.parallelWidth;
    }

    public final int getScaleX() {
        return this.scaleX;
    }

    public final int getScaleY() {
        return this.scaleY;
    }

    public final void selectAffineTrans(int idx) {
        if (this.affineArray == null || idx < 0 || idx >= this.affineArray.length) {
            throw new IllegalArgumentException();
        }
        this.affine = this.affineArray[idx];
    }

    public final void setAffineTrans(AffineTrans trans) {
        if (trans == null) {
            trans = new AffineTrans(4096, 0, 0, 0, 0, 4096, 0, 0, 0, 0, 4096, 0);
        }
        if (this.affineArray == null) {
            this.affineArray = new AffineTrans[1];
            this.affineArray[0] = trans;
        }
        this.affine = trans;
    }

    public final void setAffineTrans(AffineTrans[] trans) {
        if (trans == null) {
            throw new NullPointerException();
        }
        for (AffineTrans tran : trans) {
            if (tran != null) continue;
            throw new NullPointerException();
        }
        this.affineArray = trans;
    }

    @Deprecated
    public final void setAffineTransArray(AffineTrans[] trans) {
        this.setAffineTrans(trans);
    }

    public final void setCenter(int cx, int cy) {
        this.centerX = cx;
        this.centerY = cy;
    }

    public final void setParallelSize(int w, int h) {
        if (w < 0 || h < 0) {
            throw new IllegalArgumentException();
        }
        this.parallelWidth = w;
        this.parallelHeight = h;
        this.projection = -1862270976;
    }

    public final void setPerspective(int zNear, int zFar, int angle) {
        if (zNear >= zFar || zNear < 1 || zFar > Short.MAX_VALUE || angle < 1 || angle > 2047) {
            throw new IllegalArgumentException();
        }
        this.near = zNear;
        this.far = zFar;
        this.angle = angle;
        this.projection = -1845493760;
    }

    public final void setPerspective(int zNear, int zFar, int width, int height) {
        if (zNear >= zFar || zNear < 1 || zFar > Short.MAX_VALUE || width < 0 || height < 0) {
            throw new IllegalArgumentException();
        }
        this.near = zNear;
        this.far = zFar;
        this.perspectiveWidth = width;
        this.perspectiveHeight = height;
        this.projection = -1828716544;
    }

    public final void setScale(int sx, int sy) {
        this.scaleX = sx;
        this.scaleY = sy;
        this.projection = -1879048192;
    }
}



