/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.Light;
import com.mascotcapsule.micro3d.v3.Texture;

public class Effect3D {
    public static final int NORMAL_SHADING = 0;
    public static final int TOON_SHADING = 1;
    Light light;
    Texture texture;
    int shading;
    int toonHigh;
    int toonLow;
    int toonThreshold;
    boolean isTransparency;

    public Effect3D() {
        this.shading = 0;
        this.isTransparency = true;
    }

    public Effect3D(Light light, int shading, boolean isEnableTrans, Texture tex) {
        this.setShadingType(shading);
        this.setSphereTexture(tex);
        this.setLight(light);
        this.isTransparency = isEnableTrans;
    }

    public final Light getLight() {
        return this.light;
    }

    @Deprecated
    public final int getShading() {
        return this.shading;
    }

    public final int getShadingType() {
        return this.shading;
    }

    @Deprecated
    public final Texture getSphereMap() {
        return this.texture;
    }

    public final Texture getSphereTexture() {
        return this.texture;
    }

    @Deprecated
    public final int getThreshold() {
        return this.toonThreshold;
    }

    @Deprecated
    public final int getThresholdHigh() {
        return this.toonHigh;
    }

    @Deprecated
    public final int getThresholdLow() {
        return this.toonLow;
    }

    public final int getToonHigh() {
        return this.toonHigh;
    }

    public final int getToonLow() {
        return this.toonLow;
    }

    public final int getToonThreshold() {
        return this.toonThreshold;
    }

    @Deprecated
    public final boolean isSemiTransparentEnabled() {
        return this.isTransparency;
    }

    public final boolean isTransparency() {
        return this.isTransparency;
    }

    public final void setLight(Light light) {
        this.light = light;
    }

    @Deprecated
    public final void setSemiTransparentEnabled(boolean isEnable) {
        this.isTransparency = isEnable;
    }

    @Deprecated
    public final void setShading(int shading) {
        this.setShadingType(shading);
    }

    public final void setShadingType(int shading) {
        if ((shading & 0xFFFFFFFE) != 0) {
            throw new IllegalArgumentException();
        }
        this.shading = shading;
    }

    @Deprecated
    public final void setSphereMap(Texture tex) {
        this.setSphereTexture(tex);
    }

    public final void setSphereTexture(Texture tex) {
        if (tex != null && tex.isForModel) {
            throw new IllegalArgumentException();
        }
        this.texture = tex;
    }

    @Deprecated
    public final void setThreshold(int threshold, int high, int low) {
        this.setToonParams(threshold, high, low);
    }

    public final void setToonParams(int threshold, int high, int low) {
        if ((threshold & 0xFFFFFF00 | high & 0xFFFFFF00 | low & 0xFFFFFF00) != 0) {
            throw new IllegalArgumentException();
        }
        this.toonThreshold = threshold;
        this.toonHigh = high;
        this.toonLow = low;
    }

    public final void setTransparency(boolean isEnable) {
        this.isTransparency = isEnable;
    }
}



