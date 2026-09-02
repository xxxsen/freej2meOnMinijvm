/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.FrameState;
import com.mascotcapsule.micro3d.v3.base.Mat4;
import com.mascotcapsule.micro3d.v3.base.MathUtil;
import com.mascotcapsule.micro3d.v3.base.Micro3dBackend;
import com.mascotcapsule.micro3d.v3.base.Micro3dRasterizer;
import com.mascotcapsule.micro3d.v3.base.Micro3dSurface;
import com.mascotcapsule.micro3d.v3.base.Model;
import com.mascotcapsule.micro3d.v3.base.TextureData;
import com.mascotcapsule.micro3d.v3.base.TextureImpl;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import javax.microedition.lcdui.Graphics;
import org.recompile.mobile.PlatformGraphics;

public class SoftwareMicro3dBackend
implements Micro3dBackend {
    private static final float CLIP_EPSILON = 1.0E-5f;
    private static final int CLIP_PLANE_NEAR = 0;
    private static final int CLIP_PLANE_FAR = 1;
    private Graphics boundGraphics;
    private Micro3dSurface surface;
    private final Rectangle clip = new Rectangle();
    private int targetWidth;
    private int targetHeight;
    private float[] depthBuffer;
    private final float[] mvp = new float[16];

    @Override
    public void bind(Object target, boolean doClip) {
        Graphics g;
        if (!(target instanceof Graphics)) {
            throw new IllegalStateException("Software backend only supports Graphics targets");
        }
        this.boundGraphics = g = (Graphics)target;
        BufferedImage canvas = SoftwareMicro3dBackend.canvasOf(g);
        this.surface = new Micro3dSurface.BufferedImageSurface(canvas);
        this.targetWidth = canvas.getWidth();
        this.targetHeight = canvas.getHeight();
        if (doClip) {
            setClip(g.getClipX(), g.getClipY(), g.getClipWidth(), g.getClipHeight());
        } else {
            setClip(0, 0, this.targetWidth, this.targetHeight);
        }
        if (this.depthBuffer == null || this.depthBuffer.length < this.targetWidth * this.targetHeight) {
            this.depthBuffer = new float[this.targetWidth * this.targetHeight];
        }
    }

    private void setClip(int x, int y, int width, int height) {
        this.clip.x = x;
        this.clip.y = y;
        this.clip.width = width;
        this.clip.height = height;
    }

    private static BufferedImage canvasOf(Graphics g) {
        PlatformGraphics pg = (PlatformGraphics)g;
        return pg.getCanvas();
    }

    @Override
    public int getTargetWidth() {
        return this.targetWidth;
    }

    @Override
    public int getTargetHeight() {
        return this.targetHeight;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void flushFrame(FrameState frame) {
        this.renderItems(frame);
    }

    @Override
    public void flushItems(FrameState frame) {
        this.renderItems(frame);
    }

    @Override
    public void release(Object target) {
        this.boundGraphics = null;
    }

    private void clearDepth() {
        if (this.depthBuffer != null) {
            for (int i = 0; i < this.depthBuffer.length; ++i) {
                this.depthBuffer[i] = Float.POSITIVE_INFINITY;
            }
        }
    }

    private void renderItems(FrameState frame) {
        if (frame.items.isEmpty()) {
            return;
        }
        this.clearDepth();
        Micro3dRasterizer r = new Micro3dRasterizer(this.surface, this.clip, this.depthBuffer);
        for (int pass = 0; pass < 2; ++pass) {
            boolean depthWrite = pass == 0;
            for (FrameState.DrawItem item : frame.items) {
                MathUtil.multiplyMM(this.mvp, item.projMatrix, item.viewMatrix);
                if (item instanceof FrameState.FigureItem) {
                    this.renderFigure(r, (FrameState.FigureItem)item, this.mvp, pass, depthWrite);
                    continue;
                }
                if (!(item instanceof FrameState.PrimitiveItem)) continue;
                this.renderPrimitive(r, (FrameState.PrimitiveItem)item, this.mvp, pass, depthWrite);
            }
        }
    }

    private void renderFigure(Micro3dRasterizer r, FrameState.FigureItem item, float[] mvp, int pass, boolean depthWrite) {
        Model model = item.model;
        if (!model.hasPolyT && !model.hasPolyC) {
            return;
        }
        FloatBuffer vertices = item.vertices;
        FloatBuffer normals = item.normals;
        vertices.position(0);
        ByteBuffer texCoords = model.texCoordArray;
        if (model.hasPolyT && item.textures != null && item.textures.length > 0) {
            this.renderFigureTextured(r, item, vertices, normals, texCoords.duplicate(), pass, depthWrite);
        }
        if (model.hasPolyC) {
            this.renderFigureColored(r, item, vertices, normals, texCoords.duplicate(), pass, depthWrite);
        }
    }

    private void renderFigureTextured(Micro3dRasterizer r, FrameState.FigureItem item, FloatBuffer vertices, FloatBuffer normals, ByteBuffer tc, int pass, boolean depthWrite) {
        boolean semiTrans = (item.attrs & 8) != 0;
        int[][][] meshes = item.model.subMeshesLengthsT;
        int length = meshes.length;
        int blendIndex = 0;
        int pos = 0;
        if (semiTrans) {
            if (pass == 0) {
                length = 1;
            } else {
                int[][] mesh;
                int[][] nArray = mesh = meshes[blendIndex++];
                int n = nArray.length;
                for (int i = 0; i < n; ++i) {
                    int[] lens;
                    for (int cnt : lens = nArray[i]) {
                        pos += cnt;
                    }
                }
            }
        } else if (pass == 1) {
            return;
        }
        while (blendIndex < length) {
            int[][] texMesh = meshes[blendIndex];
            int blendMode = semiTrans && pass == 1 ? blendIndex << 1 : 0;
            for (int face = 0; face < texMesh.length; ++face) {
                TextureImpl tex = face < item.textures.length ? item.textures[face] : null;
                TextureData texData = tex != null ? tex.image : null;
                int[] lens = texMesh[face];
                int cnt = lens[0];
                if (cnt > 0) {
                    this.drawFigureBucket(r, item, vertices, normals, tc, pos, cnt, this.mvp, texData, blendMode, true);
                    pos += cnt;
                }
                if ((cnt = lens[1]) <= 0) continue;
                this.drawFigureBucket(r, item, vertices, normals, tc, pos, cnt, this.mvp, texData, blendMode, false);
                pos += cnt;
            }
            ++blendIndex;
        }
    }

    private void renderFigureColored(Micro3dRasterizer r, FrameState.FigureItem item, FloatBuffer vertices, FloatBuffer normals, ByteBuffer materialData, int pass, boolean depthWrite) {
        int[] bucket;
        boolean semiTrans = (item.attrs & 8) != 0;
        int[][] meshes = item.model.subMeshesLengthsC;
        int length = meshes.length;
        int blendIndex = 0;
        int pos = 0;
        int startVertex = item.model.numVerticesPolyT;
        if (semiTrans) {
            if (pass == 0) {
                length = 1;
            } else {
                bucket = meshes[blendIndex++];
                pos += bucket[0] + bucket[1];
            }
        } else if (pass == 1) {
            return;
        }
        while (blendIndex < length) {
            bucket = meshes[blendIndex];
            int blendMode = semiTrans && pass == 1 ? blendIndex << 1 : 0;
            int cnt = bucket[0];
            if (cnt > 0) {
                this.drawFigureColorBucket(r, item, vertices, normals, materialData, startVertex + pos, cnt, this.mvp, blendMode, true);
                pos += cnt;
            }
            if ((cnt = bucket[1]) > 0) {
                this.drawFigureColorBucket(r, item, vertices, normals, materialData, startVertex + pos, cnt, this.mvp, blendMode, false);
                pos += cnt;
            }
            ++blendIndex;
        }
    }

    private void drawFigureBucket(Micro3dRasterizer r, FrameState.FigureItem item, FloatBuffer vertices, FloatBuffer normals, ByteBuffer texCoords, int start, int count, float[] mvp, TextureData texture, int blendMode, boolean cullBack) {
        boolean globalLight = (item.attrs & 1) != 0 && normals != null;
        boolean toon = (item.attrs & 4) != 0;
        TextureData sphereGlobal = item.specular != null ? item.specular.image : null;
        int triBase = start;
        while (triBase + 2 < start + count) {
            int tcOff = triBase * 5;
            boolean lightFlag = (texCoords.get(tcOff + 2) & 0xFF) != 0;
            boolean specularFlag = (texCoords.get(tcOff + 3) & 0xFF) != 0;
            boolean transparentFlag = (texCoords.get(tcOff + 4) & 0xFF) != 0;
            Micro3dRasterizer.Shading s = this.makeShading(texture, specularFlag ? sphereGlobal : null, item, globalLight && lightFlag, toon, blendMode, transparentFlag, cullBack, false);
            this.drawTriangle(r, vertices, normals, texCoords, triBase, mvp, item.viewMatrix, s, blendMode == 0);
            triBase += 3;
        }
    }

    private void drawFigureColorBucket(Micro3dRasterizer r, FrameState.FigureItem item, FloatBuffer vertices, FloatBuffer normals, ByteBuffer materialData, int start, int count, float[] mvp, int blendMode, boolean cullBack) {
        boolean globalLight = (item.attrs & 1) != 0 && normals != null;
        boolean toon = (item.attrs & 4) != 0;
        TextureData sphereGlobal = item.specular != null && (item.attrs & 2) != 0 ? item.specular.image : null;
        int triBase = start;
        while (triBase + 2 < start + count) {
            int tcOff = triBase * 5;
            boolean lightFlag = (materialData.get(tcOff + 3) & 0xFF) != 0;
            boolean specularFlag = (materialData.get(tcOff + 4) & 0xFF) != 0;
            Micro3dRasterizer.Shading s = this.makeShading(null, specularFlag ? sphereGlobal : null, item, globalLight && lightFlag, toon, blendMode, false, cullBack, false);
            this.drawColorTriangle(r, vertices, normals, materialData, triBase, mvp, item.viewMatrix, s, blendMode == 0);
            triBase += 3;
        }
    }

    private void renderPrimitive(Micro3dRasterizer r, FrameState.PrimitiveItem item, float[] mvp, int pass, boolean depthWrite) {
        boolean drawThisPass;
        boolean semiTrans;
        int command = item.command;
        int type = command & 0x7000000;
        if (type == 0x1000000) {
            this.renderPoints(r, item, mvp, pass, depthWrite);
            return;
        }
        if (type == 0x2000000) {
            this.renderLines(r, item, mvp, pass, depthWrite);
            return;
        }
        if (type == 0x5000000) {
            this.renderPointSprites(r, item, pass, depthWrite);
            return;
        }
        if (type != 0x3000000 && type != 0x4000000) {
            return;
        }
        int blend = item.blendMode();
        int rawBlendBits = command & 0x60;
        boolean bl = semiTrans = (item.attrs & 8) != 0;
        drawThisPass = blend == 0 ? pass == 0 : pass == 1;
        if (!drawThisPass) {
            return;
        }
        TextureData texData = item.texture != null ? item.texture.image : null;
        boolean enableLight = (item.attrs & 1) != 0 && (command & 1) != 0 && item.normals != null;
        TextureData sphere = null;
        if (enableLight && (item.attrs & 2) != 0 && (command & 2) != 0 && item.specular != null) {
            sphere = item.specular.image;
        }
        boolean toon = (item.attrs & 4) != 0;
        boolean colorKey = (command & 0x10) != 0;
        Micro3dRasterizer.Shading s = this.makeShading(texData, sphere, item, enableLight, toon, blend, colorKey, false, false);
        FloatBuffer vertices = item.vertices;
        FloatBuffer normals = item.normals;
        ByteBuffer colors = item.colors;
        ByteBuffer texCoords = item.texCoords;
        int vertexCount = vertices.capacity() / 3;
        vertices.position(0);
        boolean expandedVertexColors = colors != null && (command & 0x400) != 1024;
        int triBase = 0;
        while (triBase + 2 < vertexCount) {
            this.drawPrimitiveTriangle(r, item, vertices, normals, texCoords, colors, triBase, mvp, s, expandedVertexColors, depthWrite, command);
            triBase += 3;
        }
    }

    private void renderPoints(Micro3dRasterizer r, FrameState.PrimitiveItem item, float[] mvp, int pass, boolean depthWrite) {
        boolean expandedVertexColors;
        int blend = item.blendMode();
        boolean drawThisPass = blend == 0 ? pass == 0 : pass == 1;
        if (!drawThisPass || item.vertices == null || item.colors == null) {
            return;
        }
        FloatBuffer vertices = item.vertices;
        ByteBuffer colors = item.colors;
        int vertexCount = vertices.capacity() / 3;
        int command = item.command;
        int perCommandColor = -1;
        boolean bl2 = expandedVertexColors = (command & 0x400) != 1024;
        if (!expandedVertexColors && colors.capacity() >= 3) {
            perCommandColor = 0xFF000000 | (colors.get(0) & 0xFF) << 16 | (colors.get(1) & 0xFF) << 8 | colors.get(2) & 0xFF;
        }
        for (int i = 0; i < vertexCount; ++i) {
            Micro3dRasterizer.Vertex v = this.projectPrimitiveVertex(vertices, mvp, i);
            if (v == null) continue;
            int color = expandedVertexColors ? SoftwareMicro3dBackend.vertexColor(colors, i) : perCommandColor;
            r.rasterPoint(v, color, blend, true, depthWrite);
        }
    }

    private void renderLines(Micro3dRasterizer r, FrameState.PrimitiveItem item, float[] mvp, int pass, boolean depthWrite) {
        boolean expandedVertexColors;
        int blend = item.blendMode();
        boolean drawThisPass = blend == 0 ? pass == 0 : pass == 1;
        if (!drawThisPass || item.vertices == null || item.colors == null) {
            return;
        }
        FloatBuffer vertices = item.vertices;
        ByteBuffer colors = item.colors;
        int vertexCount = vertices.capacity() / 3;
        int command = item.command;
        int perCommandColor = -1;
        boolean bl2 = expandedVertexColors = (command & 0x400) != 1024;
        if (!expandedVertexColors && colors.capacity() >= 3) {
            perCommandColor = 0xFF000000 | (colors.get(0) & 0xFF) << 16 | (colors.get(1) & 0xFF) << 8 | colors.get(2) & 0xFF;
        }
        int i = 0;
        while (i + 1 < vertexCount) {
            Micro3dRasterizer.Vertex v0 = this.projectPrimitiveVertex(vertices, mvp, i);
            Micro3dRasterizer.Vertex v1 = this.projectPrimitiveVertex(vertices, mvp, i + 1);
            if (v0 != null && v1 != null) {
                int c0 = expandedVertexColors ? SoftwareMicro3dBackend.vertexColor(colors, i) : perCommandColor;
                int c1 = expandedVertexColors ? SoftwareMicro3dBackend.vertexColor(colors, i + 1) : perCommandColor;
                r.rasterLine(v0, v1, c0, c1, blend, true, depthWrite);
            }
            i += 2;
        }
    }

    private void renderPointSprites(Micro3dRasterizer r, FrameState.PrimitiveItem item, int pass, boolean depthWrite) {
        boolean drawThisPass;
        boolean semiTrans;
        TextureData texData;
        TextureData textureData = texData = item.texture != null ? item.texture.image : null;
        if (texData == null || item.vertices == null || item.texCoords == null) {
            return;
        }
        int blend = item.blendMode();
        boolean bl = semiTrans = (item.attrs & 8) != 0;
        drawThisPass = blend == 0 ? pass == 0 : pass == 1;
        if (!drawThisPass) {
            return;
        }
        Micro3dRasterizer.Shading s = this.makeShading(texData, null, item, false, false, blend, (item.command & 0x10) != 0, false, false, semiTrans);
        FloatBuffer vertices = item.vertices;
        ByteBuffer texCoords = item.texCoords;
        int vertexCount = vertices.capacity() / 4;
        int triBase = 0;
        while (triBase + 2 < vertexCount) {
            this.drawClipSpaceTriangle(r, vertices, texCoords, triBase, s, depthWrite);
            triBase += 3;
        }
    }

    private Micro3dRasterizer.Shading makeShading(TextureData texture, TextureData sphere, FrameState.DrawItem item, boolean enableLight, boolean toon, int blend, boolean colorKey, boolean cullBack, boolean cullFront) {
        return this.makeShading(texture, sphere, item, enableLight, toon, blend, colorKey, cullBack, cullFront, false);
    }

    private Micro3dRasterizer.Shading makeShading(TextureData texture, TextureData sphere, FrameState.DrawItem item, boolean enableLight, boolean toon, int blend, boolean colorKey, boolean cullBack, boolean cullFront, boolean useTextureAlpha) {
        return new Micro3dRasterizer.Shading(texture, sphere, item.light, enableLight, toon, item.toonThreshold, item.toonHigh, item.toonLow, blend, colorKey, false, 0, cullBack, cullFront, useTextureAlpha);
    }

    private void drawPrimitiveTriangle(Micro3dRasterizer r, FrameState.PrimitiveItem item, FloatBuffer vertices, FloatBuffer normals, ByteBuffer texCoords, ByteBuffer colors, int triBase, float[] mvp, Micro3dRasterizer.Shading s, boolean expandedVertexColors, boolean depthWrite, int command) {
        ClipVertex[] clipVertices = new ClipVertex[3];
        float[] cc = new float[4];
        for (int k = 0; k < 3; ++k) {
            int ti;
            int vi = (triBase + k) * 3;
            float x = vertices.get(vi);
            float y = vertices.get(vi + 1);
            float z = vertices.get(vi + 2);
            Mat4.transformPoint(mvp, x, y, z, cc);
            ClipVertex v = new ClipVertex();
            v.cx = cc[0];
            v.cy = cc[1];
            v.cz = cc[2];
            v.cw = cc[3];
            v.b = 255.0f;
            v.g = 255.0f;
            v.r = 255.0f;
            v.a = 255.0f;
            if (expandedVertexColors && colors != null) {
                int ci = (triBase + k) * 3;
                if (ci + 2 < colors.capacity()) {
                    colors.position(ci);
                    v.r = colors.get() & 0xFF;
                    v.g = colors.get() & 0xFF;
                    v.b = colors.get() & 0xFF;
                }
            } else if (colors != null && colors.capacity() >= 3 && (command & 0x400) == 1024) {
                v.r = colors.get(0) & 0xFF;
                v.g = colors.get(1) & 0xFF;
                v.b = colors.get(2) & 0xFF;
            }
            if (normals != null) {
                int ni = (triBase + k) * 3;
                v.nx = normals.get(ni);
                v.ny = normals.get(ni + 1);
                v.nz = normals.get(ni + 2);
                if (s.enableLighting) {
                    SoftwareMicro3dBackend.transformNormal(item.viewMatrix, v);
                }
            }
            if (texCoords != null && (ti = (triBase + k) * 2) + 1 < texCoords.capacity()) {
                v.u = texCoords.get(ti) & 0xFF;
                v.v = texCoords.get(ti + 1) & 0xFF;
            }
            clipVertices[k] = v;
        }
        this.rasterClippedTriangle(r, clipVertices, s, depthWrite);
    }

    private void drawClipSpaceTriangle(Micro3dRasterizer r, FloatBuffer vertices, ByteBuffer texCoords, int triBase, Micro3dRasterizer.Shading s, boolean depthWrite) {
        ClipVertex[] clipVertices = new ClipVertex[3];
        for (int k = 0; k < 3; ++k) {
            int vi = (triBase + k) * 4;
            ClipVertex v = new ClipVertex();
            v.cx = vertices.get(vi);
            v.cy = vertices.get(vi + 1);
            v.cz = vertices.get(vi + 2);
            v.cw = vertices.get(vi + 3);
            v.b = 255.0f;
            v.g = 255.0f;
            v.r = 255.0f;
            v.a = 255.0f;
            int ti = (triBase + k) * 2;
            if (ti + 1 < texCoords.capacity()) {
                v.u = texCoords.get(ti) & 0xFF;
                v.v = texCoords.get(ti + 1) & 0xFF;
            }
            clipVertices[k] = v;
        }
        this.rasterClippedTriangle(r, clipVertices, s, depthWrite);
    }

    private void drawTriangle(Micro3dRasterizer r, FloatBuffer vertices, FloatBuffer normals, ByteBuffer texCoords, int triBase, float[] mvp, float[] viewMatrix, Micro3dRasterizer.Shading s, boolean depthWrite) {
        ClipVertex[] clipVertices = new ClipVertex[3];
        float[] cc = new float[4];
        for (int k = 0; k < 3; ++k) {
            int vi = (triBase + k) * 3;
            float x = vertices.get(vi);
            float y = vertices.get(vi + 1);
            float z = vertices.get(vi + 2);
            Mat4.transformPoint(mvp, x, y, z, cc);
            ClipVertex v = new ClipVertex();
            v.cx = cc[0];
            v.cy = cc[1];
            v.cz = cc[2];
            v.cw = cc[3];
            v.b = 255.0f;
            v.g = 255.0f;
            v.r = 255.0f;
            v.a = 255.0f;
            if (normals != null) {
                int ni = (triBase + k) * 3;
                v.nx = normals.get(ni);
                v.ny = normals.get(ni + 1);
                v.nz = normals.get(ni + 2);
                if (s.enableLighting) {
                    SoftwareMicro3dBackend.transformNormal(viewMatrix, v);
                }
            }
            int tcOff = (triBase + k) * 5;
            v.u = texCoords.get(tcOff) & 0xFF;
            v.v = texCoords.get(tcOff + 1) & 0xFF;
            clipVertices[k] = v;
        }
        this.rasterClippedTriangle(r, clipVertices, s, depthWrite);
    }

    private void drawColorTriangle(Micro3dRasterizer r, FloatBuffer vertices, FloatBuffer normals, ByteBuffer materialData, int triBase, float[] mvp, float[] viewMatrix, Micro3dRasterizer.Shading s, boolean depthWrite) {
        ClipVertex[] clipVertices = new ClipVertex[3];
        float[] cc = new float[4];
        for (int k = 0; k < 3; ++k) {
            int vi = (triBase + k) * 3;
            float x = vertices.get(vi);
            float y = vertices.get(vi + 1);
            float z = vertices.get(vi + 2);
            Mat4.transformPoint(mvp, x, y, z, cc);
            ClipVertex v = new ClipVertex();
            v.cx = cc[0];
            v.cy = cc[1];
            v.cz = cc[2];
            v.cw = cc[3];
            int tcOff = (triBase + k) * 5;
            v.r = materialData.get(tcOff) & 0xFF;
            v.g = materialData.get(tcOff + 1) & 0xFF;
            v.b = materialData.get(tcOff + 2) & 0xFF;
            v.a = 255.0f;
            if (normals != null) {
                int ni = (triBase + k) * 3;
                v.nx = normals.get(ni);
                v.ny = normals.get(ni + 1);
                v.nz = normals.get(ni + 2);
                if (s.enableLighting) {
                    SoftwareMicro3dBackend.transformNormal(viewMatrix, v);
                }
            }
            clipVertices[k] = v;
        }
        this.rasterClippedTriangle(r, clipVertices, s, depthWrite);
    }

    private void rasterClippedTriangle(Micro3dRasterizer r, ClipVertex[] source, Micro3dRasterizer.Shading s, boolean depthWrite) {
        int i;
        ClipVertex[] tmpA = new ClipVertex[8];
        ClipVertex[] tmpB = new ClipVertex[8];
        int count = source.length;
        for (int i2 = 0; i2 < count; ++i2) {
            tmpA[i2] = source[i2];
        }
        if ((count = this.clipPolygonAgainstPlane(tmpA, count, tmpB, 0)) < 3) {
            return;
        }
        if ((count = this.clipPolygonAgainstPlane(tmpB, count, tmpA, 1)) < 3) {
            return;
        }
        Micro3dRasterizer.Vertex[] projected = new Micro3dRasterizer.Vertex[count];
        for (i = 0; i < count; ++i) {
            Micro3dRasterizer.Vertex v = new Micro3dRasterizer.Vertex();
            if (!this.projectToScreen(v, tmpA[i])) {
                return;
            }
            projected[i] = v;
        }
        i = 1;
        while (i + 1 < count) {
            r.rasterTriangle(projected[0], projected[i], projected[i + 1], s, true, depthWrite);
            ++i;
        }
    }

    private int clipPolygonAgainstPlane(ClipVertex[] input, int count, ClipVertex[] output, int plane) {
        if (count <= 0) {
            return 0;
        }
        int outCount = 0;
        ClipVertex prev = input[count - 1];
        float prevDist = this.clipDistance(prev, plane);
        boolean prevInside = prevDist >= 0.0f;
        for (int i = 0; i < count; ++i) {
            boolean currInside;
            ClipVertex curr = input[i];
            float currDist = this.clipDistance(curr, plane);
            boolean bl = currInside = currDist >= 0.0f;
            if (currInside != prevInside) {
                float denom = prevDist - currDist;
                float t = Math.abs(denom) <= 1.0E-5f ? 0.0f : prevDist / denom;
                output[outCount++] = this.interpolateClipVertex(prev, curr, t);
            }
            if (currInside) {
                output[outCount++] = curr;
            }
            prev = curr;
            prevDist = currDist;
            prevInside = currInside;
        }
        return outCount;
    }

    private float clipDistance(ClipVertex v, int plane) {
        switch (plane) {
            case 0: {
                return v.cw + v.cz;
            }
            case 1: {
                return v.cw - v.cz;
            }
        }
        return -1.0f;
    }

    private ClipVertex interpolateClipVertex(ClipVertex a, ClipVertex b, float t) {
        ClipVertex out = new ClipVertex();
        out.cx = this.lerp(a.cx, b.cx, t);
        out.cy = this.lerp(a.cy, b.cy, t);
        out.cz = this.lerp(a.cz, b.cz, t);
        out.cw = this.lerp(a.cw, b.cw, t);
        out.r = this.lerp(a.r, b.r, t);
        out.g = this.lerp(a.g, b.g, t);
        out.b = this.lerp(a.b, b.b, t);
        out.a = this.lerp(a.a, b.a, t);
        out.u = this.lerp(a.u, b.u, t);
        out.v = this.lerp(a.v, b.v, t);
        out.nx = this.lerp(a.nx, b.nx, t);
        out.ny = this.lerp(a.ny, b.ny, t);
        out.nz = this.lerp(a.nz, b.nz, t);
        return out;
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static void transformNormal(float[] viewMatrix, ClipVertex v) {
        float nx = v.nx;
        float ny = v.ny;
        float nz = v.nz;
        v.nx = nx * viewMatrix[0] + ny * viewMatrix[3] + nz * viewMatrix[6];
        v.ny = nx * viewMatrix[1] + ny * viewMatrix[4] + nz * viewMatrix[7];
        v.nz = nx * viewMatrix[2] + ny * viewMatrix[5] + nz * viewMatrix[8];
    }

    private boolean projectToScreen(Micro3dRasterizer.Vertex v, ClipVertex cv) {
        float invW;
        float cw = cv.cw;
        if (Math.abs(cw) < 1.0E-5f) {
            v.visible = false;
            return false;
        }
        v.invW = invW = 1.0f / cw;
        float ndcX = cv.cx * invW;
        float ndcY = cv.cy * invW;
        float ndcZ = cv.cz * invW;
        v.x = (float)this.clip.x + (ndcX * 0.5f + 0.5f) * (float)this.clip.width;
        v.y = (float)this.clip.y + (ndcY * 0.5f + 0.5f) * (float)this.clip.height;
        v.z = ndcZ * 0.5f + 0.5f;
        v.r = cv.r;
        v.g = cv.g;
        v.b = cv.b;
        v.a = cv.a;
        v.u = cv.u;
        v.v = cv.v;
        v.nx = cv.nx;
        v.ny = cv.ny;
        v.nz = cv.nz;
        v.visible = true;
        return true;
    }

    private Micro3dRasterizer.Vertex projectPrimitiveVertex(FloatBuffer vertices, float[] mvp, int vertexIndex) {
        int vi = vertexIndex * 3;
        float[] cc = new float[4];
        Mat4.transformPoint(mvp, vertices.get(vi), vertices.get(vi + 1), vertices.get(vi + 2), cc);
        ClipVertex cv = new ClipVertex();
        cv.cx = cc[0];
        cv.cy = cc[1];
        cv.cz = cc[2];
        cv.cw = cc[3];
        Micro3dRasterizer.Vertex out = new Micro3dRasterizer.Vertex();
        return this.projectToScreen(out, cv) ? out : null;
    }

    private static int vertexColor(ByteBuffer colors, int vertexIndex) {
        int ci = vertexIndex * 3;
        if (ci + 2 >= colors.capacity()) {
            return -1;
        }
        return 0xFF000000 | (colors.get(ci) & 0xFF) << 16 | (colors.get(ci + 1) & 0xFF) << 8 | colors.get(ci + 2) & 0xFF;
    }

    private static final class ClipVertex {
        float cx;
        float cy;
        float cz;
        float cw;
        float r;
        float g;
        float b;
        float a;
        float u;
        float v;
        float nx;
        float ny;
        float nz;

        private ClipVertex() {
        }
    }
}
