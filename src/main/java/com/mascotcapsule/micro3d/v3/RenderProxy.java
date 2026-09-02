/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3;

import com.mascotcapsule.micro3d.v3.AffineTrans;
import com.mascotcapsule.micro3d.v3.Effect3D;
import com.mascotcapsule.micro3d.v3.FigureLayout;
import com.mascotcapsule.micro3d.v3.Light;
import com.mascotcapsule.micro3d.v3.Texture;
import com.mascotcapsule.micro3d.v3.Vector3D;
import com.mascotcapsule.micro3d.v3.base.Engine;
import com.mascotcapsule.micro3d.v3.base.TextureImpl;

class RenderProxy {
    RenderProxy() {
    }

    static void getViewTrans(AffineTrans a, float[] out, int n) {
        int offset = n * 12;
        out[offset++] = (float)a.m00 * 2.4414062E-4f;
        out[offset++] = (float)a.m10 * 2.4414062E-4f;
        out[offset++] = (float)a.m20 * 2.4414062E-4f;
        out[offset++] = (float)a.m01 * 2.4414062E-4f;
        out[offset++] = (float)a.m11 * 2.4414062E-4f;
        out[offset++] = (float)a.m21 * 2.4414062E-4f;
        out[offset++] = (float)a.m02 * 2.4414062E-4f;
        out[offset++] = (float)a.m12 * 2.4414062E-4f;
        out[offset++] = (float)a.m22 * 2.4414062E-4f;
        out[offset++] = a.m03;
        out[offset++] = a.m13;
        out[offset] = a.m23;
    }

    static void setTextureArray(Engine engine, Texture[] textures) {
        int len;
        if (textures != null && (len = textures.length) > 0) {
            if (len > 16) {
                len = 16;
            }
            TextureImpl[] texArray = new TextureImpl[len];
            for (int i = 0; i < len; ++i) {
                Texture texture = textures[i];
                if (texture == null) {
                    throw new NullPointerException();
                }
                texArray[i] = texture.impl;
            }
            engine.setTextureArray(texArray);
        }
    }

    static void setEffects(Engine engine, Effect3D effect) {
        int attrs = engine.getAttributes();
        Light light = effect.light;
        if (light != null) {
            int ambIntensity = light.ambIntensity;
            int dirIntensity = light.dirIntensity;
            Vector3D dir = light.direction;
            engine.setLight(ambIntensity, dirIntensity, dir.x, dir.y, dir.z);
            attrs |= 1;
        } else {
            attrs &= 0xFFFFFFFE;
        }
        int shading = effect.shading;
        if (shading == 1) {
            attrs |= 4;
            engine.setToonParam(effect.toonThreshold, effect.toonHigh, effect.toonLow);
        } else {
            attrs &= 0xFFFFFFFB;
        }
        boolean isBlend = effect.isTransparency;
        attrs = isBlend ? (attrs |= 8) : (attrs &= 0xFFFFFFF7);
        Texture specular = effect.texture;
        if (specular != null) {
            attrs |= 2;
            engine.setSphereTexture(specular.impl);
        } else {
            attrs &= 0xFFFFFFFD;
        }
        engine.setAttribute(attrs);
    }

    static void setProjection(Engine engine, FigureLayout layout) {
        switch (layout.projection) {
            case -1879048192: {
                engine.setOrthographicScale(layout.scaleX, layout.scaleY);
                break;
            }
            case -1862270976: {
                engine.setOrthographicWH(layout.parallelWidth, layout.parallelHeight);
                break;
            }
            case -1845493760: {
                engine.setPerspectiveFov(layout.near, layout.far, layout.angle);
                break;
            }
            case -1828716544: {
                engine.setPerspectiveWH(layout.near, layout.far, layout.perspectiveWidth, layout.perspectiveHeight);
            }
        }
    }

    static void setAffineArray(Engine engine, AffineTrans[] affineArray) {
        if (affineArray != null) {
            int len = affineArray.length;
            float[] transArray = new float[len * 12];
            for (int i = 0; i < len; ++i) {
                RenderProxy.getViewTrans(affineArray[i], transArray, i);
            }
            engine.setViewTransArray(transArray);
        }
    }
}



