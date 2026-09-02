/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.Light;
import com.mascotcapsule.micro3d.v3.base.Model;
import com.mascotcapsule.micro3d.v3.base.TextureImpl;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Vector;

public final class FrameState {
    public final float[] viewMatrix = new float[12];
    public final float[] projMatrix = new float[16];
    public int projection;
    public float near;
    public int centerX;
    public int centerY;
    public int width;
    public int height;
    public int attrs;
    public final Light light = new Light();
    public final TextureImpl[] textures = new TextureImpl[16];
    public int texturesLen;
    public int textureIdx;
    public TextureImpl specular;
    public int toonThreshold;
    public int toonHigh;
    public int toonLow;
    public final Vector<DrawItem> items = new Vector();

    public TextureImpl getTexture() {
        if (this.textureIdx < 0 || this.textureIdx >= this.texturesLen) {
            return null;
        }
        return this.textures[this.textureIdx];
    }

    public void reset(int width, int height) {
        this.width = width;
        this.height = height;
        this.texturesLen = 0;
        this.textureIdx = 0;
        this.specular = null;
        this.items.clear();
    }

    public static abstract class DrawItem {
        public final float[] viewMatrix = new float[12];
        public final float[] projMatrix = new float[16];
        public int attrs;
        public Light light;
        public TextureImpl specular;
        public int toonThreshold;
        public int toonHigh;
        public int toonLow;

        void capture(FrameState env) {
            System.arraycopy(env.viewMatrix, 0, this.viewMatrix, 0, 12);
            System.arraycopy(env.projMatrix, 0, this.projMatrix, 0, 16);
            this.attrs = env.attrs;
            if (this.light == null) {
                this.light = new Light(env.light);
            } else {
                this.light.set(env.light.ambIntensity, env.light.dirIntensity, env.light.x, env.light.y, env.light.z);
            }
            this.specular = env.specular;
            this.toonThreshold = env.toonThreshold;
            this.toonHigh = env.toonHigh;
            this.toonLow = env.toonLow;
        }
    }

    public static final class FigureItem
    extends DrawItem {
        public final Model model;
        public final TextureImpl[] textures;
        public final FloatBuffer vertices;
        public final FloatBuffer normals;

        public FigureItem(Model model, TextureImpl[] textures, FloatBuffer vertices, FloatBuffer normals) {
            this.model = model;
            this.textures = textures;
            this.vertices = vertices;
            this.normals = normals;
        }
    }

    public static final class PrimitiveItem
    extends DrawItem {
        public final int command;
        public final FloatBuffer vertices;
        public final FloatBuffer normals;
        public final ByteBuffer texCoords;
        public final ByteBuffer colors;
        public final TextureImpl texture;

        public PrimitiveItem(int command, FloatBuffer vertices, FloatBuffer normals, ByteBuffer texCoords, ByteBuffer colors, TextureImpl texture) {
            this.command = command;
            this.vertices = vertices;
            this.normals = normals;
            this.texCoords = texCoords;
            this.colors = colors;
            this.texture = texture;
        }

        public int blendMode() {
            if ((this.attrs & 8) == 0) {
                return 0;
            }
            int blend = this.command & 0x60;
            if (blend == 96) {
                return 6;
            }
            if (blend == 64) {
                return 4;
            }
            if (blend == 32) {
                return 2;
            }
            return 0;
        }
    }
}



