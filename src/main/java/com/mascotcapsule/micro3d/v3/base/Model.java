/*
 * Reconstructed source for the fork's bundled FreeJ2ME MascotCapsule implementation.
 * Browser backend integration changes are maintained by j2me-web contributors.
 */
package com.mascotcapsule.micro3d.v3.base;

import com.mascotcapsule.micro3d.v3.base.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class Model {
    public final int numPatterns;
    public final boolean hasPolyC;
    public final boolean hasPolyT;
    FloatBuffer vertexArray;
    FloatBuffer normalsArray;
    public final ByteBuffer texCoordArray;
    public final FloatBuffer originalVertices;
    FloatBuffer normals;
    FloatBuffer originalNormals;
    public final Polygon[] polygonsC;
    public final Polygon[] polygonsT;
    public final FloatBuffer vertices;
    public final int vertexArrayCapacity;
    public final int[][][] subMeshesLengthsT;
    public final int[][] subMeshesLengthsC;
    public final int numVerticesPolyT;
    public final int[] indices;
    public final ByteBuffer bones;

    Model(int vertices, int numBones, int patterns, int numTextures, int polyT3, int polyT4, int polyC3, int polyC4) {
        this.numPatterns = patterns;
        this.subMeshesLengthsT = new int[4][numTextures][2];
        this.subMeshesLengthsC = new int[4][2];
        this.numVerticesPolyT = polyT3 * 3 + polyT4 * 6;
        int numVertices = (polyT3 + polyC3) * 3 + (polyT4 + polyC4) * 6;
        this.indices = new int[numVertices];
        this.vertexArrayCapacity = numVertices * 3;
        this.polygonsC = new Polygon[polyC3 + polyC4];
        this.polygonsT = new Polygon[polyT3 + polyT4];
        this.hasPolyT = polyT3 + polyT4 > 0;
        this.hasPolyC = polyC3 + polyC4 > 0;
        this.texCoordArray = BufferUtils.createByteBuffer(numVertices * 5);
        this.originalVertices = BufferUtils.createFloatBuffer(vertices * 3);
        int i = vertices * 3 + 3;
        this.vertices = BufferUtils.createFloatBuffer(i);
        this.vertices.put(--i, Float.POSITIVE_INFINITY);
        this.bones = BufferUtils.createByteBuffer(numBones * 14 * 4);
    }

    public static final class Polygon {
        public static final int TRANSPARENT = 1;
        public static final int BLEND_HALF = 2;
        public static final int BLEND_ADD = 4;
        public static final int BLEND_SUB = 6;
        private static final int DOUBLE_FACE = 16;
        public static final int LIGHTING = 32;
        public static final int SPECULAR = 64;
        public final int[] indices;
        public final int blendMode;
        public final int doubleFace;
        public byte[] texCoords;
        public int face = -1;
        public int pattern;

        Polygon(int material, byte[] texCoords, int ... indices) {
            this.indices = indices;
            this.texCoords = texCoords;
            this.doubleFace = (material & 0x10) >> 4;
            this.blendMode = material & 6;
        }
    }
}



