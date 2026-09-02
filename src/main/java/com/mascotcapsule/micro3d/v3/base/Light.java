/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

public class Light {
    int ambIntensity;
    int dirIntensity;
    int x;
    int y;
    int z;

    Light() {
        this.ambIntensity = 4096;
        this.dirIntensity = 0;
        this.x = 0;
        this.y = 0;
        this.z = 4096;
    }

    Light(Light light) {
        this.ambIntensity = light.ambIntensity;
        this.dirIntensity = light.dirIntensity;
        this.x = light.x;
        this.y = light.y;
        this.z = light.z;
    }

    void set(int ambIntensity, int dirIntensity, int x, int y, int z) {
        this.ambIntensity = ambIntensity;
        this.dirIntensity = dirIntensity;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}



