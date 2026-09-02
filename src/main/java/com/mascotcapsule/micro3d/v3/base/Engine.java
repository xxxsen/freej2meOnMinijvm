/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.BufferUtils;
import com.mascotcapsule.micro3d.v3.base.FigureImpl;
import com.mascotcapsule.micro3d.v3.base.FrameState;
import com.mascotcapsule.micro3d.v3.base.MathUtil;
import com.mascotcapsule.micro3d.v3.base.Micro3dBackend;
import com.mascotcapsule.micro3d.v3.base.Model;
import com.mascotcapsule.micro3d.v3.base.TextureImpl;
import com.mascotcapsule.micro3d.v3.base.Utils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Engine {
    private static final int PDATA_COLOR_MASK = 3072;
    private static final int PDATA_COLOR_PER_VERTEX = 3072;
    private static final int PDATA_NORMAL_MASK = 768;
    private static final int PDATA_TEXCOORD_MASK = 12288;
    private static final int[] PRIMITIVE_SIZES = new int[]{0, 1, 2, 3, 4, 1};
    public final FrameState env = new FrameState();
    private final Micro3dBackend backend;
    private float[] matrices;

    public Engine(Micro3dBackend backend) {
        this.backend = backend;
    }

    public Micro3dBackend getBackend() {
        return this.backend;
    }

    public float[] getViewMatrix() {
        return this.env.viewMatrix;
    }

    public void setTexture(TextureImpl tex) {
        if (tex == null) {
            return;
        }
        this.env.textures[0] = tex;
        this.env.textureIdx = 0;
        this.env.texturesLen = 1;
    }

    public void setTextureArray(TextureImpl[] tex) {
        if (tex == null) {
            return;
        }
        int len = Math.min(tex.length, this.env.textures.length);
        System.arraycopy(tex, 0, this.env.textures, 0, len);
        this.env.texturesLen = len;
    }

    public void setViewTransArray(float[] m) {
        this.matrices = m;
    }

    public void selectAffineTrans(int n) {
        if (this.matrices != null && this.matrices.length >= (n + 1) * 12) {
            System.arraycopy(this.matrices, n * 12, this.env.viewMatrix, 0, 12);
        }
    }

    public void setCenter(int cx, int cy) {
        this.env.centerX = cx;
        this.env.centerY = cy;
    }

    public void setLight(int ambIntensity, int dirIntensity, int x, int y, int z) {
        this.env.light.set(ambIntensity, dirIntensity, x, y, z);
    }

    public int getAttributes() {
        return this.env.attrs;
    }

    public void setAttribute(int attrs) {
        this.env.attrs = attrs;
    }

    public void setSphereTexture(TextureImpl tex) {
        if (tex != null) {
            this.env.specular = tex;
        }
    }

    public void setToonParam(int threshold, int high, int low) {
        this.env.toonThreshold = threshold;
        this.env.toonHigh = high;
        this.env.toonLow = low;
    }

    public void setOrthographicScale(int scaleX, int scaleY) {
        this.env.projection = -1879048192;
        float vw = this.env.width;
        float vh = this.env.height;
        float w = vw * (4096.0f / (float)scaleX);
        float h = vh * (4096.0f / (float)scaleY);
        float sx = 2.0f / w;
        float sy = 2.0f / h;
        float sz = 1.5258789E-5f;
        float tx = 2.0f * (float)this.env.centerX / vw - 1.0f;
        float ty = 2.0f * (float)this.env.centerY / vh - 1.0f;
        float tz = 0.0f;
        float[] pm = this.env.projMatrix;
        pm[0] = sx;
        pm[4] = 0.0f;
        pm[8] = 0.0f;
        pm[12] = tx;
        pm[1] = 0.0f;
        pm[5] = sy;
        pm[9] = 0.0f;
        pm[13] = ty;
        pm[2] = 0.0f;
        pm[6] = 0.0f;
        pm[10] = sz;
        pm[14] = tz;
        pm[3] = 0.0f;
        pm[7] = 0.0f;
        pm[11] = 0.0f;
        pm[15] = 1.0f;
    }

    public void setOrthographicWH(int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        this.env.projection = -1862270976;
        float sx = 2.0f / (float)w;
        float sy = 2.0f / (float)h;
        float sz = 1.5258789E-5f;
        float tx = 2.0f * (float)this.env.centerX / (float)this.env.width - 1.0f;
        float ty = 2.0f * (float)this.env.centerY / (float)this.env.height - 1.0f;
        float tz = 0.0f;
        float[] pm = this.env.projMatrix;
        pm[0] = sx;
        pm[4] = 0.0f;
        pm[8] = 0.0f;
        pm[12] = tx;
        pm[1] = 0.0f;
        pm[5] = sy;
        pm[9] = 0.0f;
        pm[13] = ty;
        pm[2] = 0.0f;
        pm[6] = 0.0f;
        pm[10] = sz;
        pm[14] = tz;
        pm[3] = 0.0f;
        pm[7] = 0.0f;
        pm[11] = 0.0f;
        pm[15] = 1.0f;
    }

    public void setPerspectiveFov(int near, int far, int angle) {
        if (near <= 0 || far <= 0 || near >= far) {
            return;
        }
        angle = MathUtil.clamp(angle, 2, 2046);
        this.env.projection = -1845493760;
        this.env.near = near;
        float rd = 1.0f / (float)(near - far);
        float sx = 1.0f / (float)Math.tan((double)((float)angle * 2.4414062E-4f) * Math.PI);
        float vw = this.env.width;
        float vh = this.env.height;
        float sy = sx * (vw / vh);
        float sz = (float)(-(far + near)) * rd;
        float tx = 2.0f * (float)this.env.centerX / vw - 1.0f;
        float ty = 2.0f * (float)this.env.centerY / vh - 1.0f;
        float tz = 2.0f * (float)far * (float)near * rd;
        float[] pm = this.env.projMatrix;
        pm[0] = sx;
        pm[4] = 0.0f;
        pm[8] = tx;
        pm[12] = 0.0f;
        pm[1] = 0.0f;
        pm[5] = sy;
        pm[9] = ty;
        pm[13] = 0.0f;
        pm[2] = 0.0f;
        pm[6] = 0.0f;
        pm[10] = sz;
        pm[14] = tz;
        pm[3] = 0.0f;
        pm[7] = 0.0f;
        pm[11] = 1.0f;
        pm[15] = 0.0f;
    }

    public void setPerspectiveWH(int near, int far, int w, int h) {
        if (near <= 0 || far <= 0 || near >= far || w == 0 || h == 0) {
            return;
        }
        this.env.projection = -1828716544;
        this.env.near = near;
        float rd = 1.0f / (float)(near - far);
        float sx = 2.0f * (float)near / ((float)w * 2.4414062E-4f);
        float sy = 2.0f * (float)near / ((float)h * 2.4414062E-4f);
        float sz = (float)(-(near + far)) * rd;
        float tx = 2.0f * (float)this.env.centerX / (float)this.env.width - 1.0f;
        float ty = 2.0f * (float)this.env.centerY / (float)this.env.height - 1.0f;
        float tz = 2.0f * (float)far * (float)near * rd;
        float[] pm = this.env.projMatrix;
        pm[0] = sx;
        pm[4] = 0.0f;
        pm[8] = tx;
        pm[12] = 0.0f;
        pm[1] = 0.0f;
        pm[5] = sy;
        pm[9] = ty;
        pm[13] = 0.0f;
        pm[2] = 0.0f;
        pm[6] = 0.0f;
        pm[10] = sz;
        pm[14] = tz;
        pm[3] = 0.0f;
        pm[7] = 0.0f;
        pm[11] = 1.0f;
        pm[15] = 0.0f;
    }

    public void postFigure(FigureImpl figure) {
        Model model = figure.model;
        FloatBuffer vertices = BufferUtils.createFloatBuffer(model.vertexArrayCapacity);
        FloatBuffer normals = null;
        if (model.originalNormals != null) {
            normals = BufferUtils.createFloatBuffer(model.vertexArrayCapacity);
        }
        figure.fillBuffers(vertices, normals);
        TextureImpl[] texs = null;
        int len = this.env.texturesLen;
        if (len > 0) {
            texs = new TextureImpl[len];
            System.arraycopy(this.env.textures, 0, texs, 0, len);
        }
        FrameState.FigureItem item = new FrameState.FigureItem(model, texs, vertices, normals);
        item.capture(this.env);
        this.env.items.addElement(item);
    }

    public void postPrimitives(int command, int[] vertices, int vo, int[] normals, int no, int[] textureCoords, int to, int[] colors, int co) {
        FloatBuffer vcBuf;
        if (command < 0) {
            throw new IllegalArgumentException();
        }
        int numPrimitives = command >> 16 & 0xFF;
        FloatBuffer ncBuf = null;
        ByteBuffer tcBuf = null;
        ByteBuffer colorBuf = null;
        switch (command & 0x7000000) {
            case 0x1000000: {
                int i;
                int vcLen = numPrimitives * 3;
                vcBuf = BufferUtils.createFloatBuffer(vcLen);
                for (i = 0; i < vcLen; ++i) {
                    vcBuf.put(vertices[vo++]);
                }
                if ((command & 0xC00) == 1024) {
                    colorBuf = BufferUtils.createByteBuffer(3);
                    int color = colors[co];
                    colorBuf.put((byte)(color >> 16 & 0xFF));
                    colorBuf.put((byte)(color >> 8 & 0xFF));
                    colorBuf.put((byte)(color & 0xFF));
                    break;
                }
                if ((command & 0xC00) != 0) {
                    colorBuf = BufferUtils.createByteBuffer(vcLen);
                    for (i = 0; i < numPrimitives; ++i) {
                        int color = colors[co++];
                        colorBuf.put((byte)(color >> 16 & 0xFF));
                        colorBuf.put((byte)(color >> 8 & 0xFF));
                        colorBuf.put((byte)(color & 0xFF));
                    }
                    break;
                }
                return;
            }
            case 0x2000000: {
                int i;
                int vcLen = numPrimitives * 2 * 3;
                vcBuf = BufferUtils.createFloatBuffer(vcLen);
                for (i = 0; i < vcLen; ++i) {
                    vcBuf.put(vertices[vo++]);
                }
                if ((command & 0xC00) == 1024) {
                    colorBuf = BufferUtils.createByteBuffer(3);
                    int color = colors[co];
                    colorBuf.put((byte)(color >> 16 & 0xFF));
                    colorBuf.put((byte)(color >> 8 & 0xFF));
                    colorBuf.put((byte)(color & 0xFF));
                    break;
                }
                if ((command & 0xC00) != 0) {
                    colorBuf = BufferUtils.createByteBuffer(vcLen);
                    for (i = 0; i < numPrimitives; ++i) {
                        int color = colors[co++];
                        byte r = (byte)(color >> 16 & 0xFF);
                        byte g = (byte)(color >> 8 & 0xFF);
                        byte b = (byte)(color & 0xFF);
                        colorBuf.put(r).put(g).put(b);
                        colorBuf.put(r).put(g).put(b);
                    }
                    break;
                }
                return;
            }
            case 0x3000000: {
                boolean hasTexCoords;
                int end;
                int vcLen = numPrimitives * 3 * 3;
                vcBuf = BufferUtils.createFloatBuffer(vcLen);
                for (int i = 0; i < vcLen; ++i) {
                    vcBuf.put(vertices[vo++]);
                }
                if ((command & 0x300) == 512) {
                    ncBuf = BufferUtils.createFloatBuffer(vcLen);
                    end = no + numPrimitives * 3;
                    while (no < end) {
                        float x = normals[no++];
                        float y = normals[no++];
                        float z = normals[no++];
                        ncBuf.put(x).put(y).put(z);
                        ncBuf.put(x).put(y).put(z);
                        ncBuf.put(x).put(y).put(z);
                    }
                } else if ((command & 0x300) == 768) {
                    ncBuf = BufferUtils.createFloatBuffer(vcLen);
                    end = no + vcLen;
                    while (no < end) {
                        ncBuf.put(normals[no++]);
                    }
                }
                boolean bl = hasTexCoords = (command & 0x3000) == 12288;
                if (hasTexCoords) {
                    if (this.env.getTexture() == null) {
                        return;
                    }
                    int tcLen = numPrimitives * 3 * 2;
                    tcBuf = BufferUtils.createByteBuffer(tcLen);
                    for (int i = 0; i < tcLen; ++i) {
                        tcBuf.put((byte)textureCoords[to++]);
                    }
                }
                if ((command & 0xC00) == 1024) {
                    colorBuf = BufferUtils.createByteBuffer(3);
                    int color = colors[co];
                    colorBuf.put((byte)(color >> 16 & 0xFF));
                    colorBuf.put((byte)(color >> 8 & 0xFF));
                    colorBuf.put((byte)(color & 0xFF));
                    break;
                }
                if ((command & 0xC00) != 0) {
                    colorBuf = BufferUtils.createByteBuffer(vcLen);
                    for (int i = 0; i < numPrimitives; ++i) {
                        int color = colors[co++];
                        byte r = (byte)(color >> 16 & 0xFF);
                        byte g = (byte)(color >> 8 & 0xFF);
                        byte b = (byte)(color & 0xFF);
                        colorBuf.put(r).put(g).put(b);
                        colorBuf.put(r).put(g).put(b);
                        colorBuf.put(r).put(g).put(b);
                    }
                    break;
                }
                if (hasTexCoords) break;
                return;
            }
            case 0x4000000: {
                boolean hasTexCoords;
                int pos;
                int offset;
                int i;
                vcBuf = BufferUtils.createFloatBuffer(numPrimitives * 6 * 3);
                for (i = 0; i < numPrimitives; ++i) {
                    pos = offset = vo + i * 4 * 3;
                    vcBuf.put(vertices[pos++]).put(vertices[pos++]).put(vertices[pos++]);
                    vcBuf.put(vertices[pos++]).put(vertices[pos++]).put(vertices[pos++]);
                    vcBuf.put(vertices[pos++]).put(vertices[pos++]).put(vertices[pos++]);
                    vcBuf.put(vertices[pos++]).put(vertices[pos++]).put(vertices[pos]);
                    pos = offset;
                    vcBuf.put(vertices[pos++]).put(vertices[pos++]).put(vertices[pos]);
                    pos = offset + 6;
                    vcBuf.put(vertices[pos++]).put(vertices[pos++]).put(vertices[pos]);
                }
                if ((command & 0x300) == 512) {
                    ncBuf = BufferUtils.createFloatBuffer(numPrimitives * 6 * 3);
                    int end = no + numPrimitives * 3;
                    while (no < end) {
                        float x = normals[no++];
                        float y = normals[no++];
                        float z = normals[no++];
                        ncBuf.put(x).put(y).put(z);
                        ncBuf.put(x).put(y).put(z);
                        ncBuf.put(x).put(y).put(z);
                        ncBuf.put(x).put(y).put(z);
                        ncBuf.put(x).put(y).put(z);
                        ncBuf.put(x).put(y).put(z);
                    }
                } else if ((command & 0x300) == 768) {
                    ncBuf = BufferUtils.createFloatBuffer(numPrimitives * 6 * 3);
                    for (i = 0; i < numPrimitives; ++i) {
                        pos = offset = no + i * 4 * 3;
                        ncBuf.put(normals[pos++]).put(normals[pos++]).put(normals[pos++]);
                        ncBuf.put(normals[pos++]).put(normals[pos++]).put(normals[pos++]);
                        ncBuf.put(normals[pos++]).put(normals[pos++]).put(normals[pos++]);
                        ncBuf.put(normals[pos++]).put(normals[pos++]).put(normals[pos]);
                        pos = offset;
                        ncBuf.put(normals[pos++]).put(normals[pos++]).put(normals[pos]);
                        pos = offset + 6;
                        ncBuf.put(normals[pos++]).put(normals[pos++]).put(normals[pos]);
                    }
                }
                boolean bl = hasTexCoords = (command & 0x3000) == 12288;
                if (hasTexCoords) {
                    if (this.env.getTexture() == null) {
                        return;
                    }
                    tcBuf = BufferUtils.createByteBuffer(numPrimitives * 6 * 2);
                    for (int i2 = 0; i2 < numPrimitives; ++i2) {
                        int offset2;
                        int pos2 = offset2 = to + i2 * 4 * 2;
                        tcBuf.put((byte)textureCoords[pos2++]).put((byte)textureCoords[pos2++]);
                        tcBuf.put((byte)textureCoords[pos2++]).put((byte)textureCoords[pos2++]);
                        tcBuf.put((byte)textureCoords[pos2++]).put((byte)textureCoords[pos2++]);
                        tcBuf.put((byte)textureCoords[pos2++]).put((byte)textureCoords[pos2]);
                        pos2 = offset2;
                        tcBuf.put((byte)textureCoords[pos2++]).put((byte)textureCoords[pos2]);
                        pos2 = offset2 + 4;
                        tcBuf.put((byte)textureCoords[pos2++]).put((byte)textureCoords[pos2]);
                    }
                }
                if ((command & 0xC00) == 1024) {
                    colorBuf = BufferUtils.createByteBuffer(3);
                    int color = colors[co];
                    colorBuf.put((byte)(color >> 16 & 0xFF));
                    colorBuf.put((byte)(color >> 8 & 0xFF));
                    colorBuf.put((byte)(color & 0xFF));
                    break;
                }
                if ((command & 0xC00) != 0) {
                    colorBuf = BufferUtils.createByteBuffer(numPrimitives * 6 * 3);
                    for (int i3 = 0; i3 < numPrimitives; ++i3) {
                        int color = colors[co++];
                        byte r = (byte)(color >> 16 & 0xFF);
                        byte g = (byte)(color >> 8 & 0xFF);
                        byte b = (byte)(color & 0xFF);
                        colorBuf.put(r).put(g).put(b);
                        colorBuf.put(r).put(g).put(b);
                        colorBuf.put(r).put(g).put(b);
                        colorBuf.put(r).put(g).put(b);
                        colorBuf.put(r).put(g).put(b);
                        colorBuf.put(r).put(g).put(b);
                    }
                    break;
                }
                if (hasTexCoords) break;
                return;
            }
            case 0x5000000: {
                if (this.env.getTexture() == null) {
                    return;
                }
                int psParams = command & 0x3000;
                if (psParams == 0) {
                    return;
                }
                float[] vertex = new float[24];
                vcBuf = BufferUtils.createFloatBuffer(numPrimitives * 6 * 4);
                tcBuf = BufferUtils.createByteBuffer(numPrimitives * 6 * 2);
                int angle = 0;
                float halfWidth = 0.0f;
                float halfHeight = 0.0f;
                byte tx0 = 0;
                byte ty0 = 0;
                byte tx1 = 0;
                byte ty1 = 0;
                float[] mvp = new float[16];
                MathUtil.multiplyMM(mvp, this.env.projMatrix, this.env.viewMatrix);
                for (int i = 0; i < numPrimitives; ++i) {
                    vertex[4] = vertices[vo++];
                    vertex[5] = vertices[vo++];
                    vertex[6] = vertices[vo++];
                    vertex[7] = 1.0f;
                    Utils.multiplyMV(vertex, mvp);
                    if (psParams != 4096 || i == 0) {
                        float width = textureCoords[to++];
                        float height = textureCoords[to++];
                        angle = textureCoords[to++];
                        tx0 = (byte)textureCoords[to++];
                        ty0 = (byte)textureCoords[to++];
                        tx1 = (byte)(textureCoords[to++] - 1);
                        ty1 = (byte)(textureCoords[to++] - 1);
                        switch (textureCoords[to++]) {
                            case 0: {
                                halfWidth = width * this.env.projMatrix[0] * 0.5f;
                                halfHeight = height * this.env.projMatrix[5] * 0.5f;
                                break;
                            }
                            case 1: {
                                if (this.env.projection <= -1862270976) {
                                    halfWidth = width / (float)this.env.width;
                                    halfHeight = height / (float)this.env.height;
                                    break;
                                }
                                halfWidth = width / (float)this.env.width * this.env.near;
                                halfHeight = height / (float)this.env.height * this.env.near;
                                break;
                            }
                            case 2: {
                                if (this.env.projection <= -1862270976) {
                                    halfWidth = width * this.env.projMatrix[0] * 0.5f;
                                    halfHeight = height * this.env.projMatrix[5] * 0.5f;
                                    break;
                                }
                                float near = this.env.near;
                                halfWidth = width * this.env.projMatrix[0] / near * 0.5f * vertex[3];
                                halfHeight = height * this.env.projMatrix[5] / near * 0.5f * vertex[3];
                                break;
                            }
                            case 3: {
                                halfWidth = width / (float)this.env.width * vertex[3];
                                halfHeight = height / (float)this.env.height * vertex[3];
                                break;
                            }
                            default: {
                                throw new IllegalArgumentException();
                            }
                        }
                    }
                    Utils.getSpriteVertex(vertex, angle, halfWidth, halfHeight);
                    vcBuf.put(vertex);
                    tcBuf.put(tx0).put(ty1);
                    tcBuf.put(tx0).put(ty0);
                    tcBuf.put(tx1).put(ty1);
                    tcBuf.put(tx1).put(ty1);
                    tcBuf.put(tx0).put(ty0);
                    tcBuf.put(tx1).put(ty0);
                }
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
        TextureImpl texture = this.env.getTexture();
        FrameState.PrimitiveItem item = new FrameState.PrimitiveItem(command, vcBuf, ncBuf, tcBuf, colorBuf, texture);
        item.capture(this.env);
        this.env.items.addElement(item);
    }

    public void resetQueue() {
        this.env.items.clear();
    }

    public void flushItems() {
        if (!this.env.items.isEmpty()) {
            this.backend.flushItems(this.env);
        }
    }

    public void flushFrame() {
        this.backend.flushFrame(this.env);
    }

    public void resetEnvironmentSize() {
        int w = this.backend.getTargetWidth();
        int h = this.backend.getTargetHeight();
        if (w > 0 && h > 0) {
            this.env.width = w;
            this.env.height = h;
        }
    }

    public void drawCommandList(int[] cmds) {
        if (-33554431 != cmds[0]) {
            throw new IllegalArgumentException("Unsupported command list version: " + cmds[0]);
        }
        int i = 1;
        block17: while (i < cmds.length) {
            int cmd = cmds[i++];
            switch (cmd & 0xFF000000) {
                case -2030043136: {
                    this.selectAffineTrans(cmd & 0xFFFFFF);
                    continue block17;
                }
                case -1610612736: {
                    this.env.light.ambIntensity = cmds[i++];
                    continue block17;
                }
                case -2097152000: {
                    this.env.attrs = cmd & 0xFFFFFF;
                    continue block17;
                }
                case -2063597568: {
                    this.setCenter(cmds[i++], cmds[i++]);
                    continue block17;
                }
                case -2080374784: {
                    i += 4;
                    continue block17;
                }
                case -1593835520: {
                    this.env.light.x = cmds[i++];
                    this.env.light.y = cmds[i++];
                    this.env.light.z = cmds[i++];
                    this.env.light.dirIntensity = cmds[i++];
                    continue block17;
                }
                case -2113929216: {
                    this.flushItems();
                    this.resetQueue();
                    continue block17;
                }
                case -2130706432: {
                    i += cmd & 0xFFFFFF;
                    continue block17;
                }
                case -1879048192: {
                    this.setOrthographicScale(cmds[i++], cmds[i++]);
                    continue block17;
                }
                case -1862270976: {
                    this.setOrthographicWH(cmds[i++], cmds[i++]);
                    continue block17;
                }
                case -1845493760: {
                    this.setPerspectiveFov(cmds[i++], cmds[i++], cmds[i++]);
                    continue block17;
                }
                case -1828716544: {
                    this.setPerspectiveWH(cmds[i++], cmds[i++], cmds[i++], cmds[i++]);
                    continue block17;
                }
                case -2046820352: {
                    int tid = cmd & 0xFFFFFF;
                    if (tid >= 16) continue block17;
                    this.env.textureIdx = tid;
                    continue block17;
                }
                case -1358954496: {
                    this.setToonParam(cmds[i++], cmds[i++], cmds[i++]);
                    continue block17;
                }
                case -2147483648: {
                    return;
                }
            }
            int type = cmd & 0x7000000;
            if (type == 0 || cmd < 0) {
                throw new IllegalArgumentException();
            }
            int num = cmd >> 16 & 0xFF;
            int sizeOf = PRIMITIVE_SIZES[type >> 24];
            int len = num * 3 * sizeOf;
            int vo = i;
            int no = i += len;
            if ((cmd & 0x300) == 512) {
                i += num * 3;
            } else if ((cmd & 0x300) == 768) {
                i += len;
            }
            int to = i;
            if (type == 0x5000000) {
                if ((cmd & 0x3000) == 4096) {
                    i += 8;
                } else if ((cmd & 0x3000) != 0) {
                    i += num * 8;
                }
            } else if ((cmd & 0x3000) == 12288) {
                i += num * 2 * sizeOf;
            }
            int co = i++;
            if ((cmd & 0xC00) != 1024) {
                if ((cmd & 0xC00) == 2048) {
                    i += num;
                } else if ((cmd & 0xC00) == 3072) {
                    i += num * sizeOf;
                }
            }
            if (i > cmds.length) {
                throw new IllegalArgumentException();
            }
            this.postPrimitives(cmd, cmds, vo, cmds, no, cmds, to, cmds, co);
        }
    }
}



