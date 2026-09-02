/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.Light;
import com.mascotcapsule.micro3d.v3.base.Micro3dSurface;
import com.mascotcapsule.micro3d.v3.base.TextureData;
import java.awt.Rectangle;
import java.nio.ByteBuffer;

public final class Micro3dRasterizer {
    public static final int BLEND_NORMAL = 0;
    public static final int BLEND_HALF = 2;
    public static final int BLEND_ADD = 4;
    public static final int BLEND_SUB = 6;
    private static final boolean LINEAR_FILTER = Micro3dRasterizer.textureFilterEnabled();
    private final Micro3dSurface surface;
    private final Rectangle clip;
    private final float[] depthBuffer;
    private final int surfaceW;
    private final int surfaceH;

    public Micro3dRasterizer(Micro3dSurface surface, Rectangle clip, float[] depthBuffer) {
        this.surface = surface;
        this.clip = clip;
        this.depthBuffer = depthBuffer;
        this.surfaceW = surface.getWidth();
        this.surfaceH = surface.getHeight();
    }

    public void rasterTriangle(Vertex v0, Vertex v1, Vertex v2, Shading shading, boolean depthTest, boolean depthWrite) {
        if (!(v0.visible && v1.visible && v2.visible)) {
            return;
        }
        float area = Micro3dRasterizer.edge(v0.x, v0.y, v1.x, v1.y, v2.x, v2.y);
        if (Math.abs(area) <= 1.0E-6f) {
            return;
        }
        if (shading.cullBack && area > 0.0f) {
            return;
        }
        if (shading.cullFront && area < 0.0f) {
            return;
        }
        float minXf = Math.min(v0.x, Math.min(v1.x, v2.x));
        float maxXf = Math.max(v0.x, Math.max(v1.x, v2.x));
        float minYf = Math.min(v0.y, Math.min(v1.y, v2.y));
        float maxYf = Math.max(v0.y, Math.max(v1.y, v2.y));
        int minX = Micro3dRasterizer.clamp((int)Math.floor(minXf), this.clip.x, this.clip.x + this.clip.width - 1);
        int maxX = Micro3dRasterizer.clamp((int)Math.ceil(maxXf), this.clip.x, this.clip.x + this.clip.width - 1);
        int minY = Micro3dRasterizer.clamp((int)Math.floor(minYf), this.clip.y, this.clip.y + this.clip.height - 1);
        int maxY = Micro3dRasterizer.clamp((int)Math.ceil(maxYf), this.clip.y, this.clip.y + this.clip.height - 1);
        if (minX > maxX || minY > maxY) {
            return;
        }
        float invArea = 1.0f / area;
        boolean textured = shading.texture != null;
        ByteBuffer raster = textured ? shading.texture.getRaster() : null;
        int tw = textured ? shading.texture.width : 0;
        int th = textured ? shading.texture.height : 0;
        boolean perspCorrect = textured && v0.invW > 0.0f && v1.invW > 0.0f && v2.invW > 0.0f;
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                int srcAlpha;
                int baseColor;
                float px = (float)x + 0.5f;
                float py = (float)y + 0.5f;
                float w0 = Micro3dRasterizer.edge(v1.x, v1.y, v2.x, v2.y, px, py) * invArea;
                float w1 = Micro3dRasterizer.edge(v2.x, v2.y, v0.x, v0.y, px, py) * invArea;
                float w2 = 1.0f - w0 - w1;
                if ((w0 < 0.0f || w1 < 0.0f || w2 < 0.0f) && (w0 > 0.0f || w1 > 0.0f || w2 > 0.0f)) continue;
                float depth = w0 * v0.z + w1 * v1.z + w2 * v2.z;
                int depthIndex = y * this.surfaceW + x;
                if (depthTest && this.depthBuffer != null && depthIndex >= 0 && depthIndex < this.depthBuffer.length && depth > this.depthBuffer[depthIndex]) continue;
                if (shading.flatShading) {
                    baseColor = Micro3dRasterizer.color(v0);
                } else {
                    float rr = w0 * v0.r + w1 * v1.r + w2 * v2.r;
                    float gg = w0 * v0.g + w1 * v1.g + w2 * v2.g;
                    float bb = w0 * v0.b + w1 * v1.b + w2 * v2.b;
                    float aa = w0 * v0.a + w1 * v1.a + w2 * v2.a;
                    baseColor = Micro3dRasterizer.toColor(rr, gg, bb, aa);
                }
                int litColor = baseColor;
                if (shading.light != null && shading.enableLighting) {
                    float nz;
                    float ny;
                    float nx;
                    if (shading.flatShading) {
                        nx = v0.nx;
                        ny = v0.ny;
                        nz = v0.nz;
                    } else {
                        nx = w0 * v0.nx + w1 * v1.nx + w2 * v2.nx;
                        ny = w0 * v0.ny + w1 * v1.ny + w2 * v2.ny;
                        nz = w0 * v0.nz + w1 * v1.nz + w2 * v2.nz;
                    }
                    litColor = Micro3dRasterizer.applyLight(baseColor, nx, ny, nz, shading);
                }
                int finalColor = litColor;
                if (textured) {
                    float v;
                    float u;
                    if (perspCorrect) {
                        float denom = w0 * v0.invW + w1 * v1.invW + w2 * v2.invW;
                        if (Math.abs(denom) <= 1.0E-6f) continue;
                        u = (w0 * v0.u * v0.invW + w1 * v1.u * v1.invW + w2 * v2.u * v2.invW) / denom;
                        v = (w0 * v0.v * v0.invW + w1 * v1.v * v1.invW + w2 * v2.v * v2.invW) / denom;
                    } else {
                        u = w0 * v0.u + w1 * v1.u + w2 * v2.u;
                        v = w0 * v0.v + w1 * v1.v + w2 * v2.v;
                    }
                    int texel = Micro3dRasterizer.sampleTexture(raster, tw, th, u, v);
                    if (shading.colorKey && (texel >>> 24 & 0xFF) < 128) continue;
                    finalColor = shading.useTextureAlpha ? Micro3dRasterizer.modulate(litColor, texel) : Micro3dRasterizer.modulateOpaque(litColor, texel);
                }
                if (shading.sphere != null && shading.enableLighting) {
                    float ny;
                    float nx;
                    if (shading.flatShading) {
                        nx = v0.nx;
                        ny = v0.ny;
                    } else {
                        nx = w0 * v0.nx + w1 * v1.nx + w2 * v2.nx;
                        ny = w0 * v0.ny + w1 * v1.ny + w2 * v2.ny;
                    }
                    finalColor = Micro3dRasterizer.addSphere(finalColor, shading.sphere, nx, ny);
                }
                if ((srcAlpha = finalColor >>> 24 & 0xFF) < shading.alphaThreshold) continue;
                int dstColor = this.surface.getPixel(x, y);
                int composited = Micro3dRasterizer.applyBlend(dstColor, finalColor, shading.blendMode);
                if (composited != dstColor) {
                    this.surface.setPixel(x, y, composited);
                }
                if (!depthWrite || this.depthBuffer == null || srcAlpha <= 0 || depthIndex < 0 || depthIndex >= this.depthBuffer.length) continue;
                this.depthBuffer[depthIndex] = depth;
            }
        }
    }

    public void rasterPoint(Vertex v, int color, int blendMode, boolean depthTest, boolean depthWrite) {
        if (!v.visible) {
            return;
        }
        int x = Math.round(v.x);
        int y = Math.round(v.y);
        this.plotPixel(x, y, v.z, color, blendMode, depthTest, depthWrite);
    }

    public void rasterLine(Vertex v0, Vertex v1, int c0, int c1, int blendMode, boolean depthTest, boolean depthWrite) {
        if (!v0.visible || !v1.visible) {
            return;
        }
        float dx = v1.x - v0.x;
        float dy = v1.y - v0.y;
        int steps = Math.max(Math.abs(Math.round(dx)), Math.abs(Math.round(dy)));
        if (steps <= 0) {
            this.rasterPoint(v0, c0, blendMode, depthTest, depthWrite);
            return;
        }
        for (int i = 0; i <= steps; ++i) {
            float t = (float)i / (float)steps;
            float x = v0.x + dx * t;
            float y = v0.y + dy * t;
            float z = v0.z + (v1.z - v0.z) * t;
            int color = Micro3dRasterizer.lerpColor(c0, c1, t);
            this.plotPixel(Math.round(x), Math.round(y), z, color, blendMode, depthTest, depthWrite);
        }
    }

    private static int applyLight(int baseColor, float nx, float ny, float nz, Shading s) {
        float light;
        float len = (float)Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len <= 1.0E-6f) {
            return baseColor;
        }
        nx /= len;
        ny /= len;
        nz /= len;
        float lx = s.light.x;
        float ly = s.light.y;
        float lz = s.light.z;
        float dlen = (float)Math.sqrt(lx * lx + ly * ly + lz * lz);
        if (dlen <= 1.0E-6f) {
            lx = 0.0f;
            ly = 0.0f;
            lz = 1.0f;
        } else {
            lx /= dlen;
            ly /= dlen;
            lz /= dlen;
        }
        float lambert = nx * lx + ny * ly + nz * lz;
        if (lambert < 0.0f) {
            lambert = 0.0f;
        }
        float amb = (float)s.light.ambIntensity * 2.4414062E-4f;
        float dir = (float)s.light.dirIntensity * 2.4414062E-4f * lambert;
        if (dir > 4.0f) {
            dir = 4.0f;
        }
        if ((light = amb + dir) < 0.0f) {
            light = 0.0f;
        }
        if (light > 1.0f) {
            light = 1.0f;
        }
        if (s.toon) {
            light = light * 255.0f < (float)s.toonThreshold ? (float)s.toonLow / 255.0f : (float)s.toonHigh / 255.0f;
        }
        int r = (int)((float)(baseColor >>> 16 & 0xFF) * light);
        int g = (int)((float)(baseColor >>> 8 & 0xFF) * light);
        int b = (int)((float)(baseColor & 0xFF) * light);
        int a = baseColor >>> 24 & 0xFF;
        return a << 24 | Micro3dRasterizer.clamp(r) << 16 | Micro3dRasterizer.clamp(g) << 8 | Micro3dRasterizer.clamp(b);
    }

    private static int addSphere(int color, TextureData sphere, float nx, float ny) {
        int sw = sphere.width;
        int sh = sphere.height;
        if (sw <= 0 || sh <= 0) {
            return color;
        }
        float u = nx / 128.0f + 32.0f;
        float v = ny / 128.0f + 32.0f;
        int tx = Micro3dRasterizer.clamp((int)u, 0, sw - 1);
        int ty = Micro3dRasterizer.clamp((int)v, 0, sh - 1);
        ByteBuffer r = sphere.getRaster();
        int p = (ty * sw + tx) * 4;
        if (p < 0 || p + 2 >= r.capacity()) {
            return color;
        }
        int sr = r.get(p) & 0xFF;
        int sg = r.get(p + 1) & 0xFF;
        int sb = r.get(p + 2) & 0xFF;
        int cr = color >>> 16 & 0xFF;
        int cg = color >>> 8 & 0xFF;
        int cb = color & 0xFF;
        int a = color >>> 24 & 0xFF;
        return a << 24 | Micro3dRasterizer.clamp(cr + sr) << 16 | Micro3dRasterizer.clamp(cg + sg) << 8 | Micro3dRasterizer.clamp(cb + sb);
    }

    private static int sampleTexture(ByteBuffer raster, int tw, int th, float u, float v) {
        if (raster == null || tw <= 0 || th <= 0) {
            return 0;
        }
        if (LINEAR_FILTER) {
            return Micro3dRasterizer.sampleTextureLinear(raster, tw, th, u, v);
        }
        int cap = raster.capacity();
        int maxU = tw - 1;
        int maxV = th - 1;
        if (u < 0.0f) {
            u = 0.0f;
        } else if (u > (float)maxU) {
            u = maxU;
        }
        if (v < 0.0f) {
            v = 0.0f;
        } else if (v > (float)maxV) {
            v = maxV;
        }
        float nu = u / (float)tw + 1.5258789E-5f;
        float nv = v / (float)th - 1.5258789E-5f;
        if (nu > 1.0f) {
            nu = 1.0f;
        }
        if (nv > 1.0f) {
            nv = 1.0f;
        }
        int tx = Micro3dRasterizer.clamp((int)Math.floor(nu * (float)tw), 0, maxU);
        int ty = Micro3dRasterizer.clamp((int)Math.floor(nv * (float)th), 0, maxV);
        int p = (ty * tw + tx) * 4;
        if (p < 0 || p + 3 >= cap) {
            return 0;
        }
        int r = raster.get(p) & 0xFF;
        int g = raster.get(p + 1) & 0xFF;
        int b = raster.get(p + 2) & 0xFF;
        int a = raster.get(p + 3) & 0xFF;
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static int sampleTextureLinear(ByteBuffer raster, int tw, int th, float u, float v) {
        int maxU = tw - 1;
        int maxV = th - 1;
        if (u < 0.0f) {
            u = 0.0f;
        } else if (u > (float)maxU) {
            u = maxU;
        }
        if (v < 0.0f) {
            v = 0.0f;
        } else if (v > (float)maxV) {
            v = maxV;
        }
        int x0 = Micro3dRasterizer.clamp((int)Math.floor(u), 0, maxU);
        int y0 = Micro3dRasterizer.clamp((int)Math.floor(v), 0, maxV);
        int x1 = Micro3dRasterizer.clamp(x0 + 1, 0, maxU);
        int y1 = Micro3dRasterizer.clamp(y0 + 1, 0, maxV);
        float tx = u - (float)x0;
        float ty = v - (float)y0;
        int c00 = Micro3dRasterizer.texelAt(raster, tw, th, x0, y0);
        int c10 = Micro3dRasterizer.texelAt(raster, tw, th, x1, y0);
        int c01 = Micro3dRasterizer.texelAt(raster, tw, th, x0, y1);
        int c11 = Micro3dRasterizer.texelAt(raster, tw, th, x1, y1);
        return Micro3dRasterizer.bilerpColor(c00, c10, c01, c11, tx, ty);
    }

    private static int modulate(int litColor, int texel) {
        int lr = litColor >>> 16 & 0xFF;
        int lg = litColor >>> 8 & 0xFF;
        int lb = litColor & 0xFF;
        int la = litColor >>> 24 & 0xFF;
        int tr = texel >>> 16 & 0xFF;
        int tg = texel >>> 8 & 0xFF;
        int tb = texel & 0xFF;
        int ta = texel >>> 24 & 0xFF;
        int a = la * ta / 255;
        return a << 24 | lr * tr / 255 << 16 | lg * tg / 255 << 8 | lb * tb / 255;
    }

    private static int modulateOpaque(int litColor, int texel) {
        int lr = litColor >>> 16 & 0xFF;
        int lg = litColor >>> 8 & 0xFF;
        int lb = litColor & 0xFF;
        int tr = texel >>> 16 & 0xFF;
        int tg = texel >>> 8 & 0xFF;
        int tb = texel & 0xFF;
        return 0xFF000000 | lr * tr / 255 << 16 | lg * tg / 255 << 8 | lb * tb / 255;
    }

    private static int applyBlend(int dst, int src, int blendMode) {
        if (blendMode == 0) {
            int srcA = src >>> 24 & 0xFF;
            if (srcA >= 255) {
                return src | 0xFF000000;
            }
            if (srcA <= 0) {
                return dst;
            }
            return Micro3dRasterizer.blendOver(dst, src, srcA);
        }
        int dr = dst >>> 16 & 0xFF;
        int dg = dst >>> 8 & 0xFF;
        int db = dst & 0xFF;
        int sr = src >>> 16 & 0xFF;
        int sg = src >>> 8 & 0xFF;
        int sb = src & 0xFF;
        switch (blendMode) {
            case 2: {
                return 0xFF000000 | sr + dr >> 1 << 16 | sg + dg >> 1 << 8 | sb + db >> 1;
            }
            case 4: {
                return 0xFF000000 | Micro3dRasterizer.clamp(sr + dr) << 16 | Micro3dRasterizer.clamp(sg + dg) << 8 | Micro3dRasterizer.clamp(sb + db);
            }
            case 6: {
                return 0xFF000000 | Micro3dRasterizer.clamp(dr - sr) << 16 | Micro3dRasterizer.clamp(dg - sg) << 8 | Micro3dRasterizer.clamp(db - sb);
            }
        }
        return src | 0xFF000000;
    }

    private static int blendOver(int dst, int src, int srcA) {
        int invA = 255 - srcA;
        int dr = dst >>> 16 & 0xFF;
        int dg = dst >>> 8 & 0xFF;
        int db = dst & 0xFF;
        int sr = src >>> 16 & 0xFF;
        int sg = src >>> 8 & 0xFF;
        int sb = src & 0xFF;
        int r = (sr * srcA + dr * invA) / 255;
        int g = (sg * srcA + dg * invA) / 255;
        int b = (sb * srcA + db * invA) / 255;
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    private static int color(Vertex v) {
        return Micro3dRasterizer.clamp((int)v.a) << 24 | Micro3dRasterizer.clamp((int)v.r) << 16 | Micro3dRasterizer.clamp((int)v.g) << 8 | Micro3dRasterizer.clamp((int)v.b);
    }

    private void plotPixel(int x, int y, float depth, int color, int blendMode, boolean depthTest, boolean depthWrite) {
        if (x < this.clip.x || x >= this.clip.x + this.clip.width || y < this.clip.y || y >= this.clip.y + this.clip.height) {
            return;
        }
        if (x < 0 || x >= this.surfaceW || y < 0 || y >= this.surfaceH) {
            return;
        }
        int depthIndex = y * this.surfaceW + x;
        if (depthTest && this.depthBuffer != null && depthIndex >= 0 && depthIndex < this.depthBuffer.length && depth > this.depthBuffer[depthIndex]) {
            return;
        }
        int srcAlpha = color >>> 24 & 0xFF;
        if (srcAlpha <= 0) {
            return;
        }
        int dstColor = this.surface.getPixel(x, y);
        int composited = Micro3dRasterizer.applyBlend(dstColor, color, blendMode);
        if (composited != dstColor) {
            this.surface.setPixel(x, y, composited);
        }
        if (depthWrite && this.depthBuffer != null && depthIndex >= 0 && depthIndex < this.depthBuffer.length) {
            this.depthBuffer[depthIndex] = depth;
        }
    }

    private static int lerpColor(int c0, int c1, float t) {
        int a0 = c0 >>> 24 & 0xFF;
        int r0 = c0 >>> 16 & 0xFF;
        int g0 = c0 >>> 8 & 0xFF;
        int b0 = c0 & 0xFF;
        int a1 = c1 >>> 24 & 0xFF;
        int r1 = c1 >>> 16 & 0xFF;
        int g1 = c1 >>> 8 & 0xFF;
        int b1 = c1 & 0xFF;
        int a = Math.round((float)a0 + (float)(a1 - a0) * t);
        int r = Math.round((float)r0 + (float)(r1 - r0) * t);
        int g = Math.round((float)g0 + (float)(g1 - g0) * t);
        int b = Math.round((float)b0 + (float)(b1 - b0) * t);
        return Micro3dRasterizer.clamp(a) << 24 | Micro3dRasterizer.clamp(r) << 16 | Micro3dRasterizer.clamp(g) << 8 | Micro3dRasterizer.clamp(b);
    }

    private static int texelAt(ByteBuffer raster, int tw, int th, int x, int y) {
        if (x < 0 || x >= tw || y < 0 || y >= th) {
            return 0;
        }
        int p = (y * tw + x) * 4;
        if (p < 0 || p + 3 >= raster.capacity()) {
            return 0;
        }
        int r = raster.get(p) & 0xFF;
        int g = raster.get(p + 1) & 0xFF;
        int b = raster.get(p + 2) & 0xFF;
        int a = raster.get(p + 3) & 0xFF;
        return a << 24 | r << 16 | g << 8 | b;
    }

    private static int bilerpColor(int c00, int c10, int c01, int c11, float tx, float ty) {
        float w00 = (1.0f - tx) * (1.0f - ty);
        float w10 = tx * (1.0f - ty);
        float w01 = (1.0f - tx) * ty;
        float w11 = tx * ty;
        float a = (float)(c00 >>> 24 & 0xFF) * w00 + (float)(c10 >>> 24 & 0xFF) * w10 + (float)(c01 >>> 24 & 0xFF) * w01 + (float)(c11 >>> 24 & 0xFF) * w11;
        float r = (float)(c00 >>> 16 & 0xFF) * w00 + (float)(c10 >>> 16 & 0xFF) * w10 + (float)(c01 >>> 16 & 0xFF) * w01 + (float)(c11 >>> 16 & 0xFF) * w11;
        float g = (float)(c00 >>> 8 & 0xFF) * w00 + (float)(c10 >>> 8 & 0xFF) * w10 + (float)(c01 >>> 8 & 0xFF) * w01 + (float)(c11 >>> 8 & 0xFF) * w11;
        float b = (float)(c00 & 0xFF) * w00 + (float)(c10 & 0xFF) * w10 + (float)(c01 & 0xFF) * w01 + (float)(c11 & 0xFF) * w11;
        return Micro3dRasterizer.toColor(r, g, b, a);
    }

    private static boolean textureFilterEnabled() {
        String mode = System.getProperty("freej2me.micro3d.textureFilter", "").trim();
        if (mode.length() == 0) {
            mode = System.getProperty("mascotTextureFilter", "").trim();
        }
        return "linear".equalsIgnoreCase(mode) || "true".equalsIgnoreCase(mode) || "1".equals(mode);
    }

    private static int toColor(float r, float g, float b, float a) {
        return Micro3dRasterizer.clamp(Math.round(a)) << 24 | Micro3dRasterizer.clamp(Math.round(r)) << 16 | Micro3dRasterizer.clamp(Math.round(g)) << 8 | Micro3dRasterizer.clamp(Math.round(b));
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : (v > max ? max : v);
    }

    private static float edge(float ax, float ay, float bx, float by, float px, float py) {
        return (px - ax) * (by - ay) - (py - ay) * (bx - ax);
    }

    public static final class Shading {
        public final TextureData texture;
        public final TextureData sphere;
        public final Light light;
        public final boolean enableLighting;
        public final boolean toon;
        public final int toonThreshold;
        public final int toonHigh;
        public final int toonLow;
        public final int blendMode;
        public final boolean colorKey;
        public final boolean flatShading;
        public final int alphaThreshold;
        public final boolean cullBack;
        public final boolean cullFront;
        public final boolean useTextureAlpha;

        public Shading(TextureData texture, TextureData sphere, Light light, boolean enableLighting, boolean toon, int toonThreshold, int toonHigh, int toonLow, int blendMode, boolean colorKey, boolean flatShading, int alphaThreshold, boolean cullBack, boolean cullFront, boolean useTextureAlpha) {
            this.texture = texture;
            this.sphere = sphere;
            this.light = light;
            this.enableLighting = enableLighting;
            this.toon = toon;
            this.toonThreshold = toonThreshold;
            this.toonHigh = toonHigh;
            this.toonLow = toonLow;
            this.blendMode = blendMode;
            this.colorKey = colorKey;
            this.flatShading = flatShading;
            this.alphaThreshold = alphaThreshold;
            this.cullBack = cullBack;
            this.cullFront = cullFront;
            this.useTextureAlpha = useTextureAlpha;
        }
    }

    public static final class Vertex {
        public float x;
        public float y;
        public float z;
        public float invW;
        public float r;
        public float g;
        public float b;
        public float a;
        public float u;
        public float v;
        public float nx;
        public float ny;
        public float nz;
        public boolean visible;
    }
}



