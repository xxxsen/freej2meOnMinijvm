/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.Vector3D;

public class Light {
    Vector3D direction;
    int dirIntensity;
    int ambIntensity;

    public Light() {
        this.direction = new Vector3D(0, 0, 4096);
        this.dirIntensity = 4096;
        this.ambIntensity = 0;
    }

    public Light(Vector3D dir, int dirIntensity, int ambIntensity) {
        if (dir == null) {
            throw new NullPointerException();
        }
        this.direction = dir;
        this.dirIntensity = dirIntensity;
        this.ambIntensity = ambIntensity;
    }

    public final int getAmbientIntensity() {
        return this.ambIntensity;
    }

    @Deprecated
    public final int getAmbIntensity() {
        return this.ambIntensity;
    }

    @Deprecated
    public Vector3D getDirection() {
        return this.direction;
    }

    @Deprecated
    public final int getDirIntensity() {
        return this.dirIntensity;
    }

    public final Vector3D getParallelLightDirection() {
        return this.direction;
    }

    public final int getParallelLightIntensity() {
        return this.dirIntensity;
    }

    public final void setAmbientIntensity(int p) {
        this.ambIntensity = p;
    }

    @Deprecated
    public final void setAmbIntensity(int p) {
        this.ambIntensity = p;
    }

    @Deprecated
    public final void setDirection(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        this.direction = v;
    }

    @Deprecated
    public final void setDirIntensity(int p) {
        this.dirIntensity = p;
    }

    public final void setParallelLightDirection(Vector3D v) {
        if (v == null) {
            throw new NullPointerException();
        }
        this.direction = v;
    }

    public final void setParallelLightIntensity(int p) {
        this.dirIntensity = p;
    }

    public final void setParallelLightDirection(com.jblend.graphics.j3d.Vector3D v) {
        setParallelLightDirection((Vector3D)v);
    }

    public final void setParallelLightDirection(com.motorola.graphics.j3d.Vector3D v) {
        setParallelLightDirection((Vector3D)v);
    }

    public final void setParallelLightDirection(com.vodafone.v10.graphics.j3d.Vector3D v) {
        setParallelLightDirection((Vector3D)v);
    }

    public final void setDirection(com.jblend.graphics.j3d.Vector3D v) {
        setDirection((Vector3D)v);
    }

    public final void setDirection(com.motorola.graphics.j3d.Vector3D v) {
        setDirection((Vector3D)v);
    }

    public final void setDirection(com.vodafone.v10.graphics.j3d.Vector3D v) {
        setDirection((Vector3D)v);
    }
}


